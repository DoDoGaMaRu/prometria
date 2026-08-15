package io.github.dodogamaru.prometria.model.rules;

/**
 * An Alertmanager endpoint discovered by Prometheus.
 *
 * @param url the alert receiver URL of the endpoint
 * @author Daehwan Baek
 */
public record AlertManager(
        String url
) {

}
