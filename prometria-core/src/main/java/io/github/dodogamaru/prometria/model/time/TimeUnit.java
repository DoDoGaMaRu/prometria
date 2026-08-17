package io.github.dodogamaru.prometria.model.time;

import lombok.Getter;

/**
 * Prometheus duration units used to render {@link TimeValue} instances.
 *
 * @author Daehwan Baek
 */
@Getter
public enum TimeUnit {

    /**
     * Seconds.
     */
    SECONDS("s"),
    /**
     * Minutes.
     */
    MINUTES("m"),
    /**
     * Hours.
     */
    HOURS("h");

    /**
     * Suffix appended to the numeric amount.
     */
    private final String unit;

    /**
     * @param unit suffix appended to the numeric amount
     */
    TimeUnit(String unit) {
        this.unit = unit;
    }
}
