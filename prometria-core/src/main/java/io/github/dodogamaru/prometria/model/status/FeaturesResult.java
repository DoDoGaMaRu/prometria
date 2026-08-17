package io.github.dodogamaru.prometria.model.status;

import java.util.Map;

/**
 * Response of the features API ({@code /features}).
 *
 * @param status query status ({@code success} or {@code error})
 * @param data   feature flag map: flag group ({@code api}, {@code promql}, {@code tsdb}, ...)
 *               to flag name to enabled state
 * @author Daehwan Baek
 */
public record FeaturesResult(
        String status,
        Map<String, Map<String, Boolean>> data
) {

}
