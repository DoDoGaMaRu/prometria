package io.github.dodogamaru.prometria.model.targets;

/**
 * Response of the targets API ({@code /targets}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   target discovery state
 * @author Daehwan Baek
 */
public record TargetsResult(
        String status,
        TargetsData data
) {

}
