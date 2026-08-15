package io.github.dodogamaru.prometria.model;

/**
 * A single sample returned by Prometheus.
 *
 * @param timestamp observation timestamp in epoch seconds
 * @param value     observed value
 * @author Daehwan Baek
 */
public record PrometheusSample(
        double timestamp,
        double value
) {

}
