package au.edu.adelaide.stt.service;

/**
 * Converts recorded audio into text.
 *
 * Controllers depend on this rather than on a concrete implementation. That
 * single decision allows the cloud service to be replaced by an in process stub
 * during regression testing and offline development, with no change to a
 * calling code.
 */
public interface TranscriptionService {

	/**
	 * @param audio       raw audio bytes as uploaded by the client
	 * @param filename    original filename, used to hint the container format
	 * @param contentType MIME type reported by the browser
	 * @return the transcript together with the token usage it consumed
	 */
	TranscriptionResult transcribe(byte[] audio, String filename, String contentType);

	/** Immutable result carrier, safe to share across threads. */
	record TranscriptionResult(String text, long inputTokens, long outputTokens) {
	}
}