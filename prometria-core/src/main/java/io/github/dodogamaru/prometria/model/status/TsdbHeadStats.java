package io.github.dodogamaru.prometria.model.status;

/**
 * Statistics of the TSDB head block.
 *
 * @param numSeries     number of series in the head
 * @param numLabelPairs number of label pairs in the head
 * @param chunkCount    number of chunks in the head
 * @param minTime       current minimum sample timestamp in milliseconds
 * @param maxTime       current maximum sample timestamp in milliseconds
 * @author Daehwan Baek
 */
public record TsdbHeadStats(
        long numSeries,
        int numLabelPairs,
        long chunkCount,
        long minTime,
        long maxTime
) {

}
