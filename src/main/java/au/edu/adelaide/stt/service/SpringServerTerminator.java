package au.edu.adelaide.stt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The actual production implementation for shutting down the server.
 * We pause for half a second before actually closing the server. This gives 
 * the server just enough time to send an "OK/Accepted" response back to the 
 * client. If we shut down instantly, the client's network connection would 
 * just crash and they wouldn't know if their request succeeded
 *
 * Graceful Shutdown:
 * Combined with the "server.shutdown=graceful" setting in application.yml, 
 * this tells Spring to politely finish processing any active, in-flight 
 * requests before finally pulling the plug.
 */
@Component
public class SpringServerTerminator implements ServerTerminator {

    private static final Logger log = LoggerFactory.getLogger(SpringServerTerminator.class);

    /** Long so accepted response to reach the client. */
    private static final long RESPONSE_FLUSH_DELAY_MS = 500L;

    private final ApplicationContext applicationContext;

    public SpringServerTerminator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void terminate() {
        // Create a separate worker (thread) to handle the shutdown.
        // Making it non-daemon(false) is a safety measure that guarantes 
        // Java won't accidentally kill this worker before it has completely 
        // finished its job of turning the server off.
        Thread thread = new Thread(this::runShutdownSequence, "graceful-shutdown");
        thread.setDaemon(false);
        thread.start();
    }

    private void runShutdownSequence() {
        try {
            Thread.sleep(RESPONSE_FLUSH_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Beginning graceful shutdown sequence.");
        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }
}