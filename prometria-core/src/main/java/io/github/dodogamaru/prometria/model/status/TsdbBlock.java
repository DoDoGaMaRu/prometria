package io.github.dodogamaru.prometria.model.status;

/**
 * A compacted TSDB block (one entry of {@code /status/tsdb/blocks}).
 *
 * @param ulid       unique block identifier (Crockford base32)
 * @param minTime    minimum sample time in the block (Unix ms)
 * @param maxTime    maximum sample time in the block (Unix ms)
 * @param stats      content statistics; may be absent for old blocks
 * @param compaction compaction provenance of the block
 * @param version    index format version
 * @author Daehwan Baek
 */
public record TsdbBlock(
        String ulid,
        long minTime,
        long maxTime,
        TsdbBlockStats stats,
        TsdbBlockCompaction compaction,
        int version
) {

}
