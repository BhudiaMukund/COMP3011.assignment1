package au.edu.adelaide.stt.web;

import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import au.edu.adelaide.stt.exception.TranscriptionFailedException;
import au.edu.adelaide.stt.service.TokenUsageService;
import au.edu.adelaide.stt.service.TranscriptionService;
import au.edu.adelaide.stt.web.dto.TranscriptionResponse;

/**
 * Receives recorded audio from the browser and returns its transcript.
 *
 * Thread safety: controller is stateless and holds only immutable references to
 * singleton services, so one instance safely serves any number of concurrent
 * requests. All request-scoped data lives in method parameters and local
 * variables, which are per invocation and therefore per thread.
 *
 * The controller contains no business logic and no try/catch for upstream
 * failures: it validates the request shape, delegates, and lets
 * ApiExceptionHandler convert any failure into the specified error body.
 */
@RestController
public class TranscriptionController {

	private final TranscriptionService transcriptionService;
	private final TokenUsageService tokenUsageService;

	public TranscriptionController(TranscriptionService transcriptionService, TokenUsageService tokenUsageService) {
		this.transcriptionService = transcriptionService;
		this.tokenUsageService = tokenUsageService;
	}

	@PostMapping(value = "/api/v1/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TranscriptionResponse> transcribe(@RequestParam("audio") MultipartFile audio) {

		if (audio.isEmpty()) {
			throw new IllegalArgumentException("No audio was supplied in the 'audio' part.");
		}

		byte[] bytes;
		try {
			bytes = audio.getBytes();
		} catch (IOException e) {
			throw new TranscriptionFailedException("Could not read the uploaded audio.", e);
		}

		String filename = audio.getOriginalFilename() == null ? "recording.webm" : audio.getOriginalFilename();

		TranscriptionService.TranscriptionResult result = transcriptionService.transcribe(bytes, filename,
				audio.getContentType());

		// Usage is recorded only after a successful call, so failed attempts
		// cannot populate the global counters reported by /api/v1/global/stats.
		tokenUsageService.record(result.inputTokens(), result.outputTokens());

		return ResponseEntity.ok(new TranscriptionResponse(result.text()));
	}
}