package au.edu.adelaide.stt.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import au.edu.adelaide.stt.web.dto.UptimeResponse;

/**
 * Tracks the UTC instant at which this server process started and reports
 * elapsed uptime.
 *
 * Thread safety: startedAt is assigned once during bean construction, before
 * the web connector begins accepting requests, and is final thereafter. There
 * is no mutable shared state, so no synchronisation is required and concurrent
 * readers cannot interfere with one another.
 */
@Service
public class UptimeService {

    private final Clock clock;
    private final Instant startedAt;

    public UptimeService(Clock clock) {
        this.clock = clock;
        // Truncated to milliseconds so emitted timestamps match the precision
        // shown in the specification examples.
        this.startedAt = clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    public UptimeResponse currentUptime() {
        Instant now = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        double seconds = Duration.between(startedAt, now).toNanos() / 1_000_000_000.0;
        return new UptimeResponse(startedAt, now, seconds);
    }

    public Instant startedAt() {
        return startedAt;
    }
}
