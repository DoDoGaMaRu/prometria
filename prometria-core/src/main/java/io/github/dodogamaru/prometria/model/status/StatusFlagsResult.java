package io.github.dodogamaru.prometria.model.status;

import java.util.Map;

/**
 * Response of the flags API ({@code /status/flags}).
 *
 * <p>{@code data} is the flag map itself (flag name to string value).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   flag name to flag value
 * @author Daehwan Baek
 */
public record StatusFlagsResult(
        String status,
        Map<String, String> data
) {

}
