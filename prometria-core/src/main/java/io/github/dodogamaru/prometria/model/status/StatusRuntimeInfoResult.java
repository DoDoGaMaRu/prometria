package io.github.dodogamaru.prometria.model.status;

/**
 * Response of the runtime info API ({@code /status/runtimeinfo}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   runtime information of the server
 * @author Daehwan Baek
 */
public record StatusRuntimeInfoResult(
        String status,
        StatusRuntimeInfoData data
) {

}
