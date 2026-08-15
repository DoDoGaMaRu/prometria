package io.github.dodogamaru.prometria.model.discovery;

import java.util.List;
import java.util.Map;

/**
 * Response of the metadata API ({@code /metadata}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   metric name → metadata list (collected from HELP/TYPE on scrape)
 * @author Daehwan Baek
 */
public record MetadataResult(
        String status,
        Map<String, List<MetricMetadata>> data
) {

}
