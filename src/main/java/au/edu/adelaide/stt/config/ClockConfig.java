package au.edu.adelaide.stt.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes the system clock as an injectable bean.
 *
 * Rationale: services that need the current time take a Clock rather than
 * calling Instant.now() directly. This lets tests substitute a fixed clock and
 * assert exact values, instead of asserting fuzzy "roughly N seconds" ranges..
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}