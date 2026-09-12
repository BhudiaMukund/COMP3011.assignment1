package au.edu.adelaide.stt.web.dto;

/** Response body returned to the browser after successful transcription. */
public record TranscriptionResponse(String text) {
}