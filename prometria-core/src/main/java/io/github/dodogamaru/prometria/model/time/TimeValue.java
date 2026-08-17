package io.github.dodogamaru.prometria.model.time;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * A duration with a unit, rendered as a Prometheus duration string
 * (e.g. {@code 30s}, {@code 5m}, {@code 2h}).
 *
 * @author Daehwan Baek
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeValue {

    private Long time;
    private TimeUnit timeUnit;

    /**
     * Creates a value from an amount and a unit.
     *
     * @param time     numeric amount
     * @param timeUnit duration unit
     * @return a new {@code TimeValue}
     */
    public static TimeValue of(Long time, TimeUnit timeUnit) {
        return new TimeValue(time, timeUnit);
    }

    /**
     * Renders the amount and unit concatenated.
     *
     * @return the duration string, e.g. {@code 30s}
     */
    @Override
    public String toString() {
        return time.toString() + timeUnit.getUnit();
    }
}
