package io.github.dodogamaru.prometria.query.instant;

import java.util.List;

/**
 * Result payload of an instant query.
 *
 * @param resultType result type reported by Prometheus (e.g. {@code vector})
 * @param result     returned series
 * @author Daehwan Baek
 */
public record InstantQueryData(
        String resultType,
        List<InstantQuerySeries> result
) {

}
