package io.github.dodogamaru.prometria.model.status;

/**
 * Response of the TSDB stats API ({@code /status/tsdb}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   TSDB cardinality statistics
 * @author Daehwan Baek
 */
public record StatusTsdbResult(
        String status,
        StatusTsdbData data
) {

}
