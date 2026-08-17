package io.github.dodogamaru.prometria.model.targets;

import java.util.Map;

/**
 * One relabel rule and the label set it produced.
 *
 * @param rule   the relabel rule configuration
 * @param output label set after applying this step (empty if the target was
 *               dropped by this or an earlier step)
 * @param keep   whether the target survives this step
 * @author Daehwan Baek
 */
public record RelabelStep(
        RelabelConfig rule,
        Map<String, String> output,
        boolean keep
) {

}
