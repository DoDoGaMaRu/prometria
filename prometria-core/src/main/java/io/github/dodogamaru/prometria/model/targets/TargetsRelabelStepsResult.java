package io.github.dodogamaru.prometria.model.targets;

/**
 * Response of the target relabel steps API ({@code /targets/relabel_steps}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   relabel steps with their per-step output
 * @author Daehwan Baek
 */
public record TargetsRelabelStepsResult(
        String status,
        RelabelStepsData data
) {

}
