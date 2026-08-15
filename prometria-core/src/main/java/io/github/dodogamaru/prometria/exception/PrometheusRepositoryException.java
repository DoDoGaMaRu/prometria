package io.github.dodogamaru.prometria.exception;

/**
 * Thrown when a {@code @PrometheusRepository} interface is misconfigured and
 * cannot be proxied.
 *
 * <p>One exception carries all detected violations, so the failure is reported
 * at repository creation (Spring bean creation) instead of on the first query.
 *
 * @author Daehwan Baek
 */
public class PrometheusRepositoryException extends PrometriaException {

    /**
     * @param message description of all detected violations
     */
    public PrometheusRepositoryException(String message) {
        super(message);
    }
}
