package io.github.dodogamaru.prometria.model.rules;

import java.util.Map;

/**
 * An active alert fired by an alerting rule.
 *
 * @param labels          labels of the alert, including {@code alertname}
 * @param annotations     annotations of the alert
 * @param state           alert state ({@code pending} | {@code firing})
 * @param activeAt        RFC3339 timestamp when the alert became active; {@code null} if not set
 * @param keepFiringSince RFC3339 timestamp when the alert started being kept firing; {@code null} if not set
 * @param value           the current alert value as a string (e.g. {@code 1e+00})
 * @author Daehwan Baek
 */
public record Alert(
        Map<String, String> labels,
        Map<String, String> annotations,
        String state,
        String activeAt,
        String keepFiringSince,
        String value
) {

}
