package io.github.dodogamaru.prometria.model.status;

/**
 * Short description of a block that was used as a compaction parent.
 *
 * @param ulid    block identifier (Crockford base32)
 * @param minTime minimum sample time (Unix ms)
 * @param maxTime maximum sample time (Unix ms)
 * @author Daehwan Baek
 */
public record TsdbBlockDesc(
        String ulid,
        long minTime,
        long maxTime
) {

}
