package io.github.dodogamaru.prometria.query.range;

import io.github.dodogamaru.prometria.model.time.DateTimeValue;
import io.github.dodogamaru.prometria.model.time.TimeValue;
import io.github.dodogamaru.prometria.query.Condition;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Condition for a range query ({@code /api/v1/query_range}).
 *
 * <p>Both {@code start} and {@code end} are required. Build instances with the
 * generated builder:
 * <pre>{@code
 * RangeQueryCondition condition = RangeQueryCondition.builder()
 *         .start(LocalDateTime.now().minusHours(1))
 *         .end(LocalDateTime.now())
 *         .step(TimeValue.of(1, TimeUnit.MINUTES))
 *         .build();
 * }</pre>
 *
 * @author Daehwan Baek
 */
@Getter
public class RangeQueryCondition implements Condition {

    /**
     * Start of the evaluation range (required).
     */
    private final DateTimeValue start;

    /**
     * End of the evaluation range (required).
     */
    private final DateTimeValue end;

    /**
     * Query resolution step (required).
     */
    private final TimeValue step;

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
     * @param start           start of the evaluation range (required)
     * @param end             end of the evaluation range (required)
     * @param step            query resolution step (required)
     * @param timeout         query evaluation timeout
     * @param dedup           whether to deduplicate identical series
     * @param partialResponse whether to accept a partial response
     * @param limit           maximum number of series to return
     * @param lookbackDelta   lookback period override for this query
     * @param stats           query statistics option
     */
    @Builder
    RangeQueryCondition(
            LocalDateTime start,
            LocalDateTime end,
            TimeValue step,
            TimeValue timeout,
            Boolean dedup,
            Boolean partialResponse,
            Integer limit,
            TimeValue lookbackDelta,
            String stats
    ) {
        this.start = DateTimeValue.of(start);
        this.end = DateTimeValue.of(end);
        this.step = step;
        this.timeout = timeout;
        this.dedup = dedup;
        this.partialResponse = partialResponse;
        this.limit = limit;
        this.lookbackDelta = lookbackDelta;
        this.stats = stats;
    }
}
