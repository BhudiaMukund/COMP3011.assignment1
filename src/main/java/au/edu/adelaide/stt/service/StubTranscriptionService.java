package au.edu.adelaide.stt.service;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * In-process replacement for the cloud speech-to-text service.
 *
 * Why it exists: regression tests need to exercise the controllers, error
 * handling, token accounting and concurrent load deterministically, with no
 * network dependency, no API credentials and no per-request cost. The
 * configurable delay lets load tests simulate realistic upstream latency, which
 * is what makes the thread-model assertions in ConcurrentLoadTest meaningful -
 * without a blocking delay the test would prove nothing.
 *
 * Active only under the "stub" profile. OpenAiTranscriptionService is
 * annotated @Profile("!stub"), so exactly one implementation exists at any time
 * and Spring can inject unambiguously.
 */
@Service
@Profile("stub")
public class StubTranscriptionService implements TranscriptionService {

	private static final Logger log = LoggerFactory.getLogger(StubTranscriptionService.class);

	private final Duration delay;
	private final String text;
	private final long inputTokens;
	private final long outputTokens;

	public StubTranscriptionService(@Value("${stt.stub.delay:300ms}") Duration delay,
			@Value("${stt.stub.text:This is a stub transcription.}") String text,
			@Value("${stt.stub.input-tokens:100}") long inputTokens,
			@Value("${stt.stub.output-tokens:10}") long outputTokens) {

		this.delay = delay;
		this.text = text;
		this.inputTokens = inputTokens;
		this.outputTokens = outputTokens;
		log.info("Stub transcription service active with simulated delay {}", delay);
	}

	@Override
	public TranscriptionResult transcribe(byte[] audio, String filename, String contentType) {
		if (audio == null || audio.length == 0) {
			throw new IllegalArgumentException("Audio payload must not be empty.");
		}
		try {
			// Blocking sleep on purpose: it models the blocking upstream HTTP
			// call, which is exactly what the concurrency test needs to stress.
			Thread.sleep(delay.toMillis());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return new TranscriptionResult(text, inputTokens, outputTokens);
	}
}