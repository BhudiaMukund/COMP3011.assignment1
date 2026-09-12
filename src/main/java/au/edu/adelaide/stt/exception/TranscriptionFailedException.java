package au.edu.adelaide.stt.exception;

/**
 * Thrown when speech to text service could not produce transcript.
 */
public class TranscriptionFailedException extends RuntimeException {

    public TranscriptionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}