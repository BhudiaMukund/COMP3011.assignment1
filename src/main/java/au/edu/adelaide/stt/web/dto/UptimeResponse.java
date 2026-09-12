package au.edu.adelaide.stt.web.dto;

import java.time.Instant;

/**
 * Response body for GET /api/v1/admin/uptime.
 */
public record UptimeResponse(
        Instant utcServerStart,
        Instant utcNow,
        double serverUptimeSeconds) {
}