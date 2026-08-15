package io.github.dodogamaru.prometria.spring.testapp;

import io.github.dodogamaru.prometria.query.format.FormatQueryCondition;
import io.github.dodogamaru.prometria.query.format.FormatQueryResult;
import io.github.dodogamaru.prometria.query.instant.InstantQueryCondition;
import io.github.dodogamaru.prometria.query.instant.InstantQueryResult;
import io.github.dodogamaru.prometria.query.range.RangeQueryCondition;
import io.github.dodogamaru.prometria.query.range.RangeQueryResult;
import io.github.dodogamaru.prometria.repository.Param;
import io.github.dodogamaru.prometria.repository.PromQL;
import io.github.dodogamaru.prometria.repository.PrometheusRepository;

@PrometheusRepository
public interface PrometheusIntegrationRepository {

    @PromQL("up{job=\"${job}\"}")
    InstantQueryResult up(@Param("job") String job, InstantQueryCondition condition);

    @PromQL("sum by (mode) (rate(node_cpu_seconds_total{job=\"${job}\"}[2m]))")
    RangeQueryResult cpuUsageByMode(@Param("job") String job, RangeQueryCondition condition);

    @PromQL("avg without (mode)(node_load1)")
    FormatQueryResult formatQuery(FormatQueryCondition condition);
}
