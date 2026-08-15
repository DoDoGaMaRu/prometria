package io.github.dodogamaru.prometria.model.rules;

import java.util.List;
import java.util.Map;

/**
 * A single alerting or recording rule.
 *
 * <p>Alerting and recording rules share one type: the fields that apply to only
 * one of them are {@code null} for the other (e.g. {@code alerts} and
 * {@code duration} are absent on recording rules, {@code state} is absent on
 * recording rules).
 *
 * @param name           rule name (metric name for recording rules)
 * @param query          PromQL expression of the rule
 * @param type           {@code alerting} or {@code recording}
 * @param state          alerting rule state ({@code inactive} | {@code pending} | {@code firing}); {@code null} for recording rules
 * @param duration       alerting rule {@code for} duration in seconds; {@code null} for recording rules
 * @param keepFiringFor  alerting rule {@code keep_firing_for} duration in seconds; {@code null} if not configured
 * @param labels         labels configured on the rule
 * @param annotations    annotations configured on the rule (alerting rules)
 * @param alerts         active alerts of the rule (alerting rules), unless excluded by the query
 * @param health         rule health ({@code ok} | {@code unknown} | {@code failed})
 * @param lastError      last evaluation error, or {@code null}
 * @param evaluationTime duration of the last evaluation in seconds
 * @param lastEvaluation RFC3339 timestamp of the last evaluation
 * @author Daehwan Baek
 */
public record Rule(
        String name,
        String query,
        String type,
        String state,
        Double duration,
        Double keepFiringFor,
        Map<String, String> labels,
        Map<String, String> annotations,
        List<Alert> alerts,
        String health,
        String lastError,
        double evaluationTime,
        String lastEvaluation
) {

}
