package io.github.dodogamaru.prometria.repository;

import io.github.dodogamaru.prometria.exception.PrometheusRepositoryException;
import io.github.dodogamaru.prometria.query.Condition;
import io.github.dodogamaru.prometria.query.QueryHandler;
import org.apache.commons.text.StringSubstitutor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@link InvocationHandler} that executes repository methods through
 * registered {@link QueryHandler}s.
 *
 * <p>On creation it validates every query method of the repository interface
 * and precomputes an immutable plan per method (template, condition position,
 * parameter bindings, handler). Dispatch is driven by the <em>static</em>
 * declared parameter types, never by runtime argument inspection. All
 * violations are reported together as a single
 * {@link PrometheusRepositoryException}, so a misconfigured repository fails
 * at bean creation instead of on the first query.
 *
 * @author Daehwan Baek
 */
public class PrometheusRepositoryProxy implements InvocationHandler {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");
    private final Map<Method, MethodPlan> plans;

    /**
     * @param repositoryInterface repository interface to proxy
     * @param handlers            handlers keyed by the condition type they accept
     * @throws PrometheusRepositoryException if any query method is misconfigured
     */
    public PrometheusRepositoryProxy(
            Class<?> repositoryInterface,
            Map<Class<? extends Condition>, QueryHandler<?, ?>> handlers
    ) {
        Map<Method, MethodPlan> builtPlans = new HashMap<>();
        List<String> errors = new ArrayList<>();

        for (Method method : repositoryInterface.getMethods()) {
            if (method.getDeclaringClass() == Object.class || method.isDefault() || method.isBridge() || method.isSynthetic()) {
                continue;
            }

            PromQL promql = method.getAnnotation(PromQL.class);
            if (promql == null) {
                errors.add(method.getName() + ": @PromQL annotation is required");
                continue;
            }
            if (promql.value().isBlank()) {
                errors.add(method.getName()
                        + ": @PromQL template must not be empty; repositories are for template-driven PromQL queries. "
                        + "Condition-only operations should be called through the built-in API beans "
                        + "(DiscoveryApi, TargetsApi, RulesAlertsApi, StatusApi) instead of a repository method.");
                continue;
            }

            try {
                builtPlans.put(method, buildPlan(method, promql.value(), handlers));
            } catch (IllegalArgumentException e) {
                errors.add(method.getName() + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new PrometheusRepositoryException(
                    "Invalid Prometheus repository " + repositoryInterface.getName() + ":\n  - "
                            + String.join("\n  - ", errors));
        }

        this.plans = Map.copyOf(builtPlans);
    }

    /**
     * Handles the inherited {@link Object} methods that have no plan.
     */
    private static Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "toString" -> "PrometheusRepositoryProxy@" + Integer.toHexString(System.identityHashCode(proxy));
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException("Method not supported: " + method);
        };
    }

    /**
     * Invokes the handler. The single unchecked cast is safe because the
     * condition argument type was validated against the handler at creation.
     */
    @SuppressWarnings("unchecked")
    private static <C extends Condition, R> R invokeHandler(QueryHandler<?, ?> handler, String query, Condition condition) {
        QueryHandler<C, R> typedHandler = (QueryHandler<C, R>) handler;
        return typedHandler.getResult(query, (C) condition);
    }

    /**
     * Extracts the keys of all {@code ${...}} placeholders in the template.
     */
    private static Set<String> extractPlaceholders(String template) {
        Set<String> placeholders = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(template);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }

    /**
     * Formats the registered condition type names for error messages.
     */
    private static String handlerTypes(Map<Class<? extends Condition>, QueryHandler<?, ?>> handlers) {
        return handlers.keySet().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Explains a condition/result type mismatch: a handler binds one condition type to
     * exactly one result type, so the method must declare that result type or use the
     * condition whose handler returns the declared type.
     *
     * @param declaredType return type declared by the repository method
     * @param handler      handler resolved from the method's condition parameter
     * @param handlers     all registered handlers
     * @return error message describing the mismatch and how to fix it
     */
    private static String resultMismatchMessage(
            Class<?> declaredType,
            QueryHandler<?, ?> handler,
            Map<Class<? extends Condition>, QueryHandler<?, ?>> handlers
    ) {
        StringBuilder message = new StringBuilder("return type ")
                .append(declaredType.getSimpleName())
                .append(" does not match the result type ")
                .append(handler.resultType().getSimpleName())
                .append(" of the handler for ")
                .append(handler.conditionType().getSimpleName())
                .append("; each condition type is bound to exactly one result type, so declare ")
                .append(handler.resultType().getSimpleName());

        Class<? extends Condition> alternative = conditionTypeReturning(declaredType, handlers);
        if (alternative != null) {
            message.append(" or use ")
                    .append(alternative.getSimpleName())
                    .append(" instead, whose handler returns ")
                    .append(declaredType.getSimpleName());
        }
        return message.append('.').toString();
    }

    /**
     * Finds the condition type whose registered handler returns the given result type.
     *
     * @param resultType result type to look up
     * @param handlers   all registered handlers
     * @return the matching condition type, or {@code null} if no handler returns it
     */
    private static Class<? extends Condition> conditionTypeReturning(
            Class<?> resultType,
            Map<Class<? extends Condition>, QueryHandler<?, ?>> handlers
    ) {
        return handlers.entrySet().stream()
                .filter(entry -> entry.getValue().resultType().equals(resultType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Resolves the precomputed plan and delegates to its handler.
     *
     * @param proxy  the proxy instance
     * @param method the repository method invoked on the proxy
     * @param args   method arguments
     * @return the query result mapped from the Prometheus response
     * @throws UnsupportedOperationException if the method is not a supported {@link Object} method
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        MethodPlan plan = plans.get(method);
        if (plan == null) {
            return invokeObjectMethod(proxy, method, args);
        }

        Map<String, Object> values = new HashMap<>();
        for (ParamBinding binding : plan.bindings()) {
            values.put(binding.key(), args[binding.index()]);
        }

        String promql = new StringSubstitutor(values).replace(plan.template());
        return invokeHandler(plan.handler(), promql, (Condition) args[plan.conditionIndex()]);
    }

    /**
     * Builds the invocation plan of one method, validating its signature
     * against the registered handlers.
     *
     * @param method   repository method
     * @param template PromQL template
     * @param handlers handlers keyed by condition type
     * @return the precomputed plan
     * @throws IllegalArgumentException if the method is misconfigured
     */
    private MethodPlan buildPlan(
            Method method,
            String template,
            Map<Class<? extends Condition>, QueryHandler<?, ?>> handlers
    ) {
        Parameter[] parameters = method.getParameters();

        int conditionIndex = -1;
        QueryHandler<?, ?> handler = null;
        for (int i = 0; i < parameters.length; i++) {
            QueryHandler<?, ?> candidate = handlers.get(parameters[i].getType());
            if (candidate == null) continue;
            if (conditionIndex != -1) {
                throw new IllegalArgumentException(
                        "must contain exactly one condition parameter, but '" + parameters[i].getName()
                                + "' is a second one");
            }
            conditionIndex = i;
            handler = candidate;
        }
        if (conditionIndex == -1) {
            throw new IllegalArgumentException(
                    "must contain exactly one condition parameter of a registered handler type ("
                            + handlerTypes(handlers) + ")");
        }
        if (!method.getReturnType().equals(handler.resultType())) {
            throw new IllegalArgumentException(resultMismatchMessage(method.getReturnType(), handler, handlers));
        }

        List<ParamBinding> bindings = new ArrayList<>();
        Map<String, Integer> boundKeys = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            if (i == conditionIndex) continue;

            Parameter parameter = parameters[i];
            Param param = parameter.getAnnotation(Param.class);
            String key = (param != null) ? param.value() : parameter.getName();
            if (boundKeys.put(key, i) != null) {
                throw new IllegalArgumentException("duplicate binding for placeholder key '" + key + "'");
            }
            bindings.add(new ParamBinding(i, key));
        }

        Set<String> placeholders = extractPlaceholders(template);
        placeholders.removeAll(boundKeys.keySet());
        if (!placeholders.isEmpty()) {
            throw new IllegalArgumentException(
                    "template placeholders without a matching parameter: " + placeholders);
        }

        return new MethodPlan(template, conditionIndex, List.copyOf(bindings), handler);
    }

    /**
     * Binds one method argument to a {@code ${...}} template placeholder.
     *
     * @param index argument index
     * @param key   placeholder key
     */
    private record ParamBinding(int index, String key) {
    }

    /**
     * Precomputed invocation plan for one repository method.
     *
     * @param template       PromQL template
     * @param conditionIndex index of the condition argument
     * @param bindings       parameter to placeholder bindings
     * @param handler        handler matching the condition type
     */
    private record MethodPlan(
            String template,
            int conditionIndex,
            List<ParamBinding> bindings,
            QueryHandler<?, ?> handler
    ) {
    }
}
