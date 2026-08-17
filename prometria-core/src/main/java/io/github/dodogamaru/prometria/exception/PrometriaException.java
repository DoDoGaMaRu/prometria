package io.github.dodogamaru.prometria.exception;

/**
 * Base type of all unchecked exceptions thrown by Prometria.
 *
 * @author Daehwan Baek
 */
public class PrometriaException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message human-readable description of the failure
     */
    public PrometriaException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message human-readable description of the failure
     * @param cause   underlying cause
     */
    public PrometriaException(String message, Throwable cause) {
        super(message, cause);
    }
}
