package io.github.dodogamaru.prometria.model.status;

/**
 * Content statistics of a TSDB block. All fields are optional in the JSON
 * ({@code omitempty}), so absent values map to {@code null}.
 *
 * @param numSamples          total number of samples
 * @param numFloatSamples     number of float samples
 * @param numHistogramSamples number of histogram samples
 * @param numSeries           number of series
 * @param numChunks           number of chunks
 * @param numTombstones       number of tombstones
 * @author Daehwan Baek
 */
public record TsdbBlockStats(
        Long numSamples,
        Long numFloatSamples,
        Long numHistogramSamples,
        Long numSeries,
        Long numChunks,
        Long numTombstones
) {

}
