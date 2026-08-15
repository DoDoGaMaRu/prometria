package io.github.dodogamaru.prometria.query.range;

import java.util.List;

/**
 * Result payload of a range query.
 *
 * @param resultType result type reported by Prometheus (e.g. {@code matrix})
 * @param result     returned series
 * @author Daehwan Baek
 */
public record RangeQueryData(
        String resultType,
        List<RangeQuerySeries> result
) {

}
