package io.github.dodogamaru.prometria.model.status;

/**
 * Response of the config API ({@code /status/config}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   the currently loaded configuration
 * @author Daehwan Baek
 */
public record StatusConfigResult(
        String status,
        StatusConfigData data
) {

}
