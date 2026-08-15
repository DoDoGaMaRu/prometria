package io.github.dodogamaru.prometria.model.status;

/**
 * Response of the TSDB blocks API ({@code /status/tsdb/blocks}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   block metadata list
 * @author Daehwan Baek
 */
public record StatusTsdbBlocksResult(
        String status,
        StatusTsdbBlocksData data
) {

}
