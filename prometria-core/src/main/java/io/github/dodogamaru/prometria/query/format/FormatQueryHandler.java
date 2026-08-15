package io.github.dodogamaru.prometria.query.format;

import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.client.QueryParams;
import io.github.dodogamaru.prometria.query.AbstractQueryHandler;

/**
 * Handler for the format query API ({@code /format_query}).
 *
 * <p>The query expression comes from the repository method template
 * ({@code @PromQL}); the condition carries no parameters.
 *
 * @author Daehwan Baek
 */
public class FormatQueryHandler extends AbstractQueryHandler<FormatQueryCondition, FormatQueryResult> {

    /**
     * @param client shared HTTP client used to execute the queries
     */
    public FormatQueryHandler(PrometheusHttpClient client) {
        super(client);
    }

    @Override
    public Class<FormatQueryCondition> conditionType() {
        return FormatQueryCondition.class;
    }

    @Override
    public Class<FormatQueryResult> resultType() {
        return FormatQueryResult.class;
    }

    @Override
    protected String getPath(FormatQueryCondition condition) {
        return "/format_query";
    }

    @Override
    protected QueryParams queryParams(FormatQueryCondition condition) {
        return QueryParams.create();
    }
}
