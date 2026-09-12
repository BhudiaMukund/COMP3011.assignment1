package au.edu.adelaide.stt.web.dto;

import java.time.Instant;

/**
 * Standard error body defined by the OpenAPI specification.
 * All five properties are required and the schema forbids extras.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path) {
}