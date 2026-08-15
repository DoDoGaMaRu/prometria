package io.github.dodogamaru.prometria.model.rules;

/**
 * Response of the alertmanagers API ({@code /alertmanagers}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   the discovered Alertmanager endpoints
 * @author Daehwan Baek
 */
public record AlertmanagersResult(
        String status,
        AlertmanagersData data
) {

}
