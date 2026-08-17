package io.github.dodogamaru.prometria.query;

/**
 * Executes one kind of Prometheus query.
 *
 * <p>Handlers are self-describing: each declares the {@link Condition} type it
 * accepts ({@link #conditionType()}) and the result type it returns
 * ({@link #resultType()}). Repository dispatch and validation are driven by
 * these declarations, so supporting a new query operation only requires a new
 * handler registration.
 *
 * @param <C> condition type accepted by this handler
 * @param <R> result type returned by this handler
 * @author Daehwan Baek
 */
public interface QueryHandler<C extends Condition, R> {

    /**
     * @return the condition type this handler accepts
     */
    Class<C> conditionType();

    /**
     * @return the result type this handler returns
     */
    Class<R> resultType();

    /**
     * Executes the query.
     *
     * @param query     PromQL expression
     * @param condition query condition
     * @return the query result
     */
    R getResult(String query, C condition);
}
