package io.github.dodogamaru.prometria.spring;

import io.github.dodogamaru.prometria.query.Condition;
import io.github.dodogamaru.prometria.query.QueryHandler;
import io.github.dodogamaru.prometria.repository.PrometheusRepositoryFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link FactoryBean} that exposes a proxied repository instance as a Spring bean.
 *
 * <p>All {@link QueryHandler} beans registered in the context are collected and
 * keyed by their {@link QueryHandler#conditionType()}, so custom handlers are
 * picked up automatically.
 *
 * @param <T> type of the repository interface
 * @author Daehwan Baek
 */
public class PrometheusRepositoryFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    private final Class<T> repositoryInterface;
    private ApplicationContext applicationContext;

    /**
     * @param repositoryInterface repository interface to proxy
     * @param applicationContext  context used to look up the query handlers
     */
    public PrometheusRepositoryFactoryBean(Class<T> repositoryInterface, ApplicationContext applicationContext) {
        this.repositoryInterface = repositoryInterface;
        this.applicationContext = applicationContext;
    }

    /**
     * @return proxy instance of the repository interface
     */
    @Override
    public T getObject() {
        return PrometheusRepositoryFactory.createRepository(repositoryInterface, getHandlers());
    }

    /**
     * @return the repository interface class
     */
    @Override
    public Class<?> getObjectType() {
        return repositoryInterface;
    }

    /**
     * Collects all {@code QueryHandler} beans keyed by their condition type.
     */
    @SuppressWarnings("unchecked")
    private Map<Class<? extends Condition>, QueryHandler<?, ?>> getHandlers() {
        Map<String, QueryHandler<?, ?>> handlerBeans = (Map<String, QueryHandler<?, ?>>) (Map<?, ?>)
                applicationContext.getBeansOfType(QueryHandler.class);

        Map<Class<? extends Condition>, QueryHandler<?, ?>> handlers = new HashMap<>();
        for (QueryHandler<?, ?> handler : handlerBeans.values()) {
            handlers.put(handler.conditionType(), handler);
        }
        return handlers;
    }

    /**
     * @param applicationContext the application context
     * @throws BeansException if the context cannot be set
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
