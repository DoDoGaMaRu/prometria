package io.github.dodogamaru.prometria.model.status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * One sample of a self metrics family (protobuf-JSON encoding).
 *
 * <p>Exactly one of the value fields is present per sample. The value payloads
 * are left as raw JSON because their shape varies by metric type.
 *
 * @param label     label pairs of the sample
 * @param gauge     gauge value payload, if the sample is a gauge
 * @param counter   counter value payload, if the sample is a counter
 * @param summary   summary payload, if the sample is a summary
 * @param histogram histogram payload, if the sample is a histogram
 * @author Daehwan Baek
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SelfMetricsSample(
        List<SelfMetricsLabel> label,
        JsonNode gauge,
        JsonNode counter,
        JsonNode summary,
        JsonNode histogram
) {

}
