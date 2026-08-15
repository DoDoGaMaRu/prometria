package io.github.dodogamaru.prometria.model.status;

/**
 * Response of the build info API ({@code /status/buildinfo}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   build information of the server
 * @author Daehwan Baek
 */
public record StatusBuildInfoResult(
        String status,
        StatusBuildInfoData data
) {

}
