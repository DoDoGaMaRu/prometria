package io.github.dodogamaru.prometria.model.status;

import java.util.List;

/**
 * TSDB cardinality statistics returned by the TSDB stats API.
 *
 * <p>Each {@code *CountBy*} list is limited by the {@code limit} query
 * parameter (default 10, max 10000).
 *
 * @param headStats                   statistics of the TSDB head block
 * @param seriesCountByMetricName     series count per metric name
 * @param labelValueCountByLabelName  distinct label value count per label name
 * @param memoryInBytesByLabelName    memory used per label name, in bytes
 * @param seriesCountByLabelValuePair series count per label pair
 * @author Daehwan Baek
 */
public record StatusTsdbData(
        TsdbHeadStats headStats,
        List<TsdbStat> seriesCountByMetricName,
        List<TsdbStat> labelValueCountByLabelName,
        List<TsdbStat> memoryInBytesByLabelName,
        List<TsdbStat> seriesCountByLabelValuePair
) {

}
