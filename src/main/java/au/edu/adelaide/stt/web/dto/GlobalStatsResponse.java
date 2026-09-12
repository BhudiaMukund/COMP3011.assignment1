package au.edu.adelaide.stt.web.dto;

/**
 * Response body for GET /api/v1/global/stats.
 * Counters will be cumulative since UTC server start, as the specification states.
 */
public record GlobalStatsResponse(long inputTokens, long outputTokens) {
}
