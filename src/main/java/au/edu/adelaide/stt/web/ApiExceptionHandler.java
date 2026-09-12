package au.edu.adelaide.stt.web;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import au.edu.adelaide.stt.exception.ShutdownInProgressException;
import au.edu.adelaide.stt.exception.TranscriptionFailedException;
import au.edu.adelaide.stt.web.dto.ErrorResponse;

/**
 * Translate exceptions thrown anywhere in the controller layer into the
 * ErrorResponse shape specified by the OpenAPI specs.
 *
 * Spring selects the most specific matching handler, so the catch-all below
 * only fires for exceptions no other method claims.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final Clock clock;

    public ApiExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(ShutdownInProgressException.class)
    public ResponseEntity<ErrorResponse> handleShutdownInProgress(
            ShutdownInProgressException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE,
                "Uploaded audio exceeds the maximum permitted size.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(TranscriptionFailedException.class)
    public ResponseEntity<ErrorResponse> handleTranscriptionFailure(
            TranscriptionFailedException ex, HttpServletRequest request) {
        // Log cause for diagnosis; return generic message so upstream
        // detail is never exposed over the network.
        log.error("Transcription failed for {}", request.getRequestURI(), ex);
        return build(HttpStatus.BAD_GATEWAY,
                "The speech-to-text service could not process the request.", request);
    }

    /**
     * Catch-all. Guarantees no stack trace and no default Spring error
     * body will reach a client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred.", request);
    }

    /**
     * Assembles the five-property body. getReasonPhrase() produces exactly the
     * strings used in the specification examples ("Conflict",
     * "Internal Server Error").
     */
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                Instant.now(clock).truncatedTo(ChronoUnit.SECONDS),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }
}
