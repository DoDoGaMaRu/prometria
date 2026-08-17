package io.github.dodogamaru.prometria.query.instant;

import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.client.QueryParams;
import io.github.dodogamaru.prometria.query.AbstractQueryHandler;

/**
 * Handler for instant queries ({@code /query}).
 *
 * @author Daehwan Baek
 */
public class InstantQueryHandler extends AbstractQueryHandler<InstantQueryCondition, InstantQueryResult> {

    /**
     * @param client shared HTTP client used to execute the queries
     */
    public InstantQueryHandler(PrometheusHttpClient client) {
        super(client);
    }

    @Override
    public Class<InstantQueryCondition> conditionType() {
        return InstantQueryCondition.class;
    }

    @Override
    public Class<InstantQueryResult> resultType() {
        return InstantQueryResult.class;
    }

    @Override
    protected String getPath(InstantQueryCondition condition) {
        return "/query";
    }

    @Override
    protected QueryParams queryParams(InstantQueryCondition condition) {
        return QueryParams.create()
                .addIfPresent("time", condition.getTime())
                .addIfPresent("timeout", condition.getTimeout())
                .addIfPresent("dedup", condition.getDedup())
                .addIfPresent("partialResponse", condition.getPartialResponse())
                .addIfPresent("limit", condition.getLimit())
                .addIfPresent("lookback_delta", condition.getLookbackDelta())
                .addIfPresent("stats", condition.getStats());
    }
}
