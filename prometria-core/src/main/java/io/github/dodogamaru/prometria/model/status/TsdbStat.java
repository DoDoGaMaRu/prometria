package io.github.dodogamaru.prometria.model.status;

/**
 * A name to count pair of the TSDB stats API.
 *
 * @param name  metric name, label name, or label pair (e.g. {@code job=prometheus})
 * @param value the count or byte size for that name
 * @author Daehwan Baek
 */
public record TsdbStat(
        String name,
        long value
) {

}
