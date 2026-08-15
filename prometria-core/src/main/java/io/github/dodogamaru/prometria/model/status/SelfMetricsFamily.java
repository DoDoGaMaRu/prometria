package io.github.dodogamaru.prometria.model.status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * One metric family of the self metrics response (protobuf-JSON encoding).
 *
 * <p>{@code type} is the protobuf enum name: {@code COUNTER}, {@code GAUGE},
 * {@code SUMMARY}, {@code HISTOGRAM} or {@code UNGROUPED}.
 *
 * @param name   metric name
 * @param help   HELP text
 * @param type   metric type enum name
 * @param metric samples of the family (empty for some types)
 * @author Daehwan Baek
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SelfMetricsFamily(
        String name,
        String help,
        String type,
        List<SelfMetricsSample> metric
) {

}
