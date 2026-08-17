package io.github.dodogamaru.prometria.exception;

/**
 * Unchecked exception thrown when a Prometheus query fails.
 *
 * <p>Thrown on HTTP errors, I/O failures, interrupted requests, and responses
 * that are not valid JSON.
 *
 * @author Daehwan Baek
 */
public class PrometheusQueryException extends PrometriaException {

    /**
     * Creates an exception with a message.
     *
     * @param message human-readable description of the failure
     */
    public PrometheusQueryException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message human-readable description of the failure
     * @param cause   underlying cause
     */
    public PrometheusQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
