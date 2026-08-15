package io.github.dodogamaru.prometria.model.targets;

import java.util.List;

/**
 * Payload of the target relabel steps API.
 *
 * @param steps one entry per configured relabel rule, in execution order
 * @author Daehwan Baek
 */
public record RelabelStepsData(
        List<RelabelStep> steps
) {

}
