package io.github.dodogamaru.prometria.query.range;

/**
 * Response of a range query ({@code /query_range}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   result payload
 * @author Daehwan Baek
 */
public record RangeQueryResult(
        String status,
        RangeQueryData data
) {

}
