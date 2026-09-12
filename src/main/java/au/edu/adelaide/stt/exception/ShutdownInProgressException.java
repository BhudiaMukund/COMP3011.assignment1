package au.edu.adelaide.stt.exception;

/**
 * Thrown when a graceful shutdown is requested while one is already underway.
 * Mapped to HTTP 409 Conflict, as per spec.
 */
public class ShutdownInProgressException extends RuntimeException {

    public ShutdownInProgressException(String message) {
        super(message);
    }
}