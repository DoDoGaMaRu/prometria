package io.github.dodogamaru.prometria.query.instant;

import io.github.dodogamaru.prometria.model.time.DateTimeValue;
import io.github.dodogamaru.prometria.model.time.TimeValue;
import io.github.dodogamaru.prometria.query.Condition;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Condition for an instant query ({@code /api/v1/query}).
 *
 * <p>Build instances with the generated builder:
 * <pre>{@code
 * InstantQueryCondition condition = InstantQueryCondition.builder()
 *         .time(LocalDateTime.now().minusMinutes(5))
 *         .timeout(TimeValue.of(30, TimeUnit.SECONDS))
 *         .limit(100)
 *         .build();
 * }</pre>
 *
 * @author Daehwan Baek
 */
@Getter
public class InstantQueryCondition implements Condition {

    /**
     * Evaluation time, or {@code null} to evaluate at the current time.
     */
    private final DateTimeValue time;

    /**
     * Query evaluation timeout, or {@code null} to use the Prometheus default.
     */
    private final TimeValue timeout;

    /**
     * Whether to deduplicate identical series, or {@code null} to use the server default.
     */
    private final Boolean dedup;

    /**
     * Whether to accept a partial response, or {@code null} to use the server default.
     */
    private final Boolean partialResponse;

    /**
     * Maximum number of series to return, or {@code null} for no limit.
     */
    private final Integer limit;

    /**
     * Lookback period override for this query, or {@code null} to use the server default.
     */
    private final TimeValue lookbackDelta;

    /**
     * Query statistics option (e.g. {@code all}), or {@code null} to omit statistics.
     */
    private final String stats;

    /**
     * @param time            evaluation time; if {@code null}, evaluated at the current time
     * @param timeout         query evaluation timeout
     * @param dedup           whether to deduplicate identical series
     * @param partialResponse whether to accept a partial response
     * @param limit           maximum number of series to return
     * @param lookbackDelta   lookback period override for this query
     * @param stats           query statistics option
     */
    @Builder
    public InstantQueryCondition(
            LocalDateTime time,
            TimeValue timeout,
            Boolean dedup,
            Boolean partialResponse,
            Integer limit,
            TimeValue lookbackDelta,
            String stats
    ) {
        this.time = time == null ? null : DateTimeValue.of(time);
        this.timeout = timeout;
        this.dedup = dedup;
        this.partialResponse = partialResponse;
        this.limit = limit;
        this.lookbackDelta = lookbackDelta;
        this.stats = stats;
    }
}
