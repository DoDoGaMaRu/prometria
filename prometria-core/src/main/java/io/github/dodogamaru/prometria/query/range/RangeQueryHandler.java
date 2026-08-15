package io.github.dodogamaru.prometria.query.range;

import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.client.QueryParams;
import io.github.dodogamaru.prometria.query.AbstractQueryHandler;

/**
 * Handler for range queries ({@code /query_range}).
 *
 * @author Daehwan Baek
 */
public class RangeQueryHandler extends AbstractQueryHandler<RangeQueryCondition, RangeQueryResult> {

    /**
     * @param client shared HTTP client used to execute the queries
     */
    public RangeQueryHandler(PrometheusHttpClient client) {
        super(client);
    }

    @Override
    public Class<RangeQueryCondition> conditionType() {
        return RangeQueryCondition.class;
    }

    @Override
    public Class<RangeQueryResult> resultType() {
        return RangeQueryResult.class;
    }

    @Override
    protected String getPath(RangeQueryCondition condition) {
        return "/query_range";
    }

    @Override
    protected QueryParams queryParams(RangeQueryCondition condition) {
        return QueryParams.create()
                .addIfPresent("start", condition.getStart())
                .addIfPresent("end", condition.getEnd())
                .addIfPresent("step", condition.getStep())
                .addIfPresent("timeout", condition.getTimeout())
                .addIfPresent("dedup", condition.getDedup())
                .addIfPresent("partialResponse", condition.getPartialResponse())
                .addIfPresent("limit", condition.getLimit())
                .addIfPresent("lookback_delta", condition.getLookbackDelta())
                .addIfPresent("stats", condition.getStats());
    }
}
