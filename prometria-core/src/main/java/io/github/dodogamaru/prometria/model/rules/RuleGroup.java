package io.github.dodogamaru.prometria.model.rules;

import java.util.List;

/**
 * A group of rules evaluated together.
 *
 * @param name           rule group name
 * @param file           file the rules are defined in
 * @param rules          the rules of this group (alerting and recording mixed)
 * @param interval       group evaluation interval in seconds
 * @param limit          sample limit of the group ({@code 0} = unlimited)
 * @param evaluationTime duration of the last evaluation in seconds
 * @param lastEvaluation RFC3339 timestamp of the last evaluation
 * @author Daehwan Baek
 */
public record RuleGroup(
        String name,
        String file,
        List<Rule> rules,
        double interval,
        int limit,
        double evaluationTime,
        String lastEvaluation
) {

}
