package au.edu.adelaide.stt.service;

import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Service;
import au.edu.adelaide.stt.web.dto.GlobalStatsResponse;

/**
 * Accumulates the speech-to-text tokn usage for the lifetime. Counters start at zero on every start, 
 * as the specification requires, because the state lives only in these "in-memory" fields.
 *
* Thread Safety: 
 * We use LongAdder instead of AtomicLong or synchronized because it handles high traffic much better. 
 * When hundreds of requests try to update the counter at the exact same time, LongAdder prevents a 
 * "traffic jam" by letting threads record their numbers independently without waiting in a single line.
 *
 * Known Limitation:
 * The snapshot() method reads the input and output totals one after the other, not simultaneously. 
 * If a new update happens in the split-second between these two reads, the final pair might be 
 * slightly out of sync. We accept this minor inaccuracy because keeping the app fast is more 
 * important than locking up the system just to get a perfectly synced reading.
 */
@Service
public class TokenUsageService {

    private final LongAdder inputTokens = new LongAdder();
    private final LongAdder outputTokens = new LongAdder();

    public void record(long input, long output) {
        if (input < 0 || output < 0) {
            throw new IllegalArgumentException("Token counts must not be negative.");
        }
        inputTokens.add(input);
        outputTokens.add(output);
    }

    public GlobalStatsResponse snapshot() {
        return new GlobalStatsResponse(inputTokens.sum(), outputTokens.sum());
    }
}