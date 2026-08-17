package io.github.dodogamaru.prometria.model.rules;

import java.util.List;

/**
 * Loaded rule groups returned by the rules API.
 *
 * @param groups         the rule groups; each group contains one or more rules
 * @param groupNextToken pagination token when {@code group_limit} was used and
 *                       more groups remain; {@code null} on the final page
 * @author Daehwan Baek
 */
public record RulesData(
        List<RuleGroup> groups,
        String groupNextToken
) {

}
