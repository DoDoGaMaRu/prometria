package io.github.dodogamaru.prometria.query.instant;

/**
 * Response of an instant query ({@code /query}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   result payload
 * @author Daehwan Baek
 */
public record InstantQueryResult(
        String status,
        InstantQueryData data
) {

}
