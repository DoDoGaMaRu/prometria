package io.github.dodogamaru.prometria.model.rules;

/**
 * Response of the alerts API ({@code /alerts}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   the active alerts
 * @author Daehwan Baek
 */
public record AlertsResult(
        String status,
        AlertsData data
) {

}
