package io.github.dodogamaru.prometria.query.format;

/**
 * Response of the format query API ({@code /format_query}).
 *
 * <p>{@code data} is the prettified query expression itself (a plain string).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   the formatted query expression
 * @author Daehwan Baek
 */
public record FormatQueryResult(
        String status,
        String data
) {

}
