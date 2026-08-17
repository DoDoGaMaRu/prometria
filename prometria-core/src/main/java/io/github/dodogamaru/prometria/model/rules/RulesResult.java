package io.github.dodogamaru.prometria.model.rules;

/**
 * Response of the rules API ({@code /rules}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   loaded rule groups
 * @author Daehwan Baek
 */
public record RulesResult(
        String status,
        RulesData data
) {

}
