package io.github.dodogamaru.prometria.query;

import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.client.QueryParams;

/**
 * Common execution flow shared by all Prometheus query handlers.
 *
 * <p>{@link #getResult} sends the {@code query} parameter (when present)
 * together with the parameters from {@link #queryParams(Condition)} to the
 * path from {@link #getPath(Condition)} through the shared
 * {@link PrometheusHttpClient}.
 *
 * @param <C> condition type accepted by the handler
 * @param <R> result type returned by the handler
 * @author Daehwan Baek
 */
public abstract class AbstractQueryHandler<C extends Condition, R> implements QueryHandler<C, R> {

    private final PrometheusHttpClient client;

    /**
     * @param client shared HTTP client used to execute the queries
     */
    protected AbstractQueryHandler(PrometheusHttpClient client) {
        this.client = client;
    }

    /**
     * Assembles the query parameters and executes the query.
     */
    @Override
    public final R getResult(String query, C condition) {
        QueryParams params = queryParams(condition);
        if (query != null && !query.isEmpty()) {
            params.add("query", query);
        }
        return client.get(getPath(condition), params, resultType());
    }

    /**
     * @param condition query condition
     * @return API path appended to the base URL (e.g. {@code /query}); may be derived from the condition
     */
    protected abstract String getPath(C condition);

    /**
     * @param condition query condition
     * @return the query parameters of this operation; use
     * {@link QueryParams#create()} to build them
     */
    protected abstract QueryParams queryParams(C condition);
}
