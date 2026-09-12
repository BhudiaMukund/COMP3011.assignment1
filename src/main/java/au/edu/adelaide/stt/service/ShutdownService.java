package au.edu.adelaide.stt.service;

import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import au.edu.adelaide.stt.exception.ShutdownInProgressException;

/**
 * Ensures the server only starts its shutdown process only once.
 *
 * The race condition Problem:
 * If two shutdown requests arrive at the exact same millisecond, a normal 
 * "if (not shutting down) { start shutdown }" check might let BOTH requests through. 
 * This happens because both requests check the rule before either has a chance to 
 * update it to true.
 *
 * The Atomic Solution:
 * We use AtomicBoolean's, compareAndSet(). This will act as a single, unbreakable step, 
 * it checks the current value AND changes it to true at the exact same time. Only 
 * the first request wins and proceeds. Any other simultaneous requests instantly 
 * see that the shutdown has started and get safely rejected.
 */
@Service
public class ShutdownService {

    private static final Logger log = LoggerFactory.getLogger(ShutdownService.class);

    private final AtomicBoolean shutdownStarted = new AtomicBoolean(false);
    private final ServerTerminator terminator;

    public ShutdownService(ServerTerminator terminator) {
        this.terminator = terminator;
    }

    public void requestShutdown() {
        if (!shutdownStarted.compareAndSet(false, true)) {
            throw new ShutdownInProgressException("Graceful shutdown is already in progress.");
        }
        log.info("Graceful shutdown accepted.");
        terminator.terminate();
    }

    public boolean isShutdownStarted() {
        return shutdownStarted.get();
    }
}