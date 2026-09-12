package au.edu.adelaide.stt.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import au.edu.adelaide.stt.config.OpenAiProperties;
import au.edu.adelaide.stt.exception.TranscriptionFailedException;

/**
 * Calls external speech-to-text cloud API.
 *
 * Active in every profile except "stub", so production and TITAN runs use the
 * real service while regression tests use the in process fake.
 *
 * Security: the bearer token is read from OpenAiProperties, placed only in the
 * Authorization header of outbound request, and not logged, persisted or
 * returned. The logging below records size, duration and token counts only -
 * enough to diagnose latency and cost, nothing sensitive.
 */
@Service
@Profile("!stub")
public class OpenAiTranscriptionService implements TranscriptionService {

	private static final Logger log = LoggerFactory.getLogger(OpenAiTranscriptionService.class);

	private final RestClient restClient;
	private final OpenAiProperties properties;

	public OpenAiTranscriptionService(RestClient openAiRestClient, OpenAiProperties properties) {
		this.restClient = openAiRestClient;
		this.properties = properties;
		if (!properties.hasApiKey()) {
			log.warn("No speech-to-text credentials found in the environment. "
					+ "Transcription requests will fail until OPENAI_API_KEY is set.");
		}
	}

	@Override
	public TranscriptionResult transcribe(byte[] audio, String filename, String contentType) {
		if (audio == null || audio.length == 0) {
			throw new IllegalArgumentException("Audio payload must not be empty.");
		}
		if (!properties.hasApiKey()) {
			throw new TranscriptionFailedException("Speech-to-text credentials are not configured.", null);
		}

		long startNanos = System.nanoTime();
		try {
			OpenAiTranscription response = restClient.post().uri(properties.transcriptionsPath())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
					.contentType(MediaType.MULTIPART_FORM_DATA).body(buildMultipartBody(audio, filename)).retrieve()
					.body(OpenAiTranscription.class);

			if (response == null || response.text() == null) {
				throw new TranscriptionFailedException("Upstream returned no transcript.", null);
			}

			long inputTokens = response.usage() == null ? 0L : response.usage().inputTokens();
			long outputTokens = response.usage() == null ? 0L : response.usage().outputTokens();
			long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;

			log.info(
					"Transcription completed: bytes={}, contentType={}, durationMs={}, "
							+ "inputTokens={}, outputTokens={}",
					audio.length, contentType, elapsedMs, inputTokens, outputTokens);

			return new TranscriptionResult(response.text(), inputTokens, outputTokens);

		} catch (RestClientException ex) {
			long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
			log.warn("Transcription call failed: bytes={}, durationMs={}", audio.length, elapsedMs);
			throw new TranscriptionFailedException("Upstream speech-to-text call failed.", ex);
		}
	}

	/**
	 * Assembles the multipart/form-data body.
	 *
	 * The anonymous ByteArrayResource subclass supplies filename, which is required
	 * for the upstream service to treat the part as a file upload rather than a
	 * plain form field. Without it the request will be rejected.
	 */
	private MultiValueMap<String, Object> buildMultipartBody(byte[] audio, String filename) {
		ByteArrayResource audioPart = new ByteArrayResource(audio) {
			@Override
			public String getFilename() {
				return filename;
			}
		};

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", audioPart);
		body.add("model", properties.model());
		return body;
	}

	/**
	 * The subset of the upstream response this application needs.
	 *
	 * @JsonIgnoreProperties keeps deserialisation working if the provider adds
	 *                       fields later. @JsonProperty maps the provider's
	 *                       snake_case onto camelCase Java without applying a
	 *                       global naming strategy, that could corrupt the
	 *                       application's own camelCase API responses.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	private record OpenAiTranscription(String text, Usage usage) {

		@JsonIgnoreProperties(ignoreUnknown = true)
		private record Usage(@JsonProperty("input_tokens") long inputTokens,
				@JsonProperty("output_tokens") long outputTokens) {
		}
	}
}