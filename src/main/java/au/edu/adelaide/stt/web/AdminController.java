package au.edu.adelaide.stt.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import au.edu.adelaide.stt.service.ShutdownService;
import au.edu.adelaide.stt.service.UptimeService;
import au.edu.adelaide.stt.web.dto.ShutdownResponse;
import au.edu.adelaide.stt.web.dto.UptimeResponse;

/**
 * Administration endpoints defined in the OpenAPI specification.
 *
 * The controller is stateless: it holds only immutable references to
 * collaborating singleton services, so a single instance safely serves any
 * number of concurrent requests without locking.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UptimeService uptimeService;
    private final ShutdownService shutdownService;

    public AdminController(UptimeService uptimeService, ShutdownService shutdownService) {
        this.uptimeService = uptimeService; 
        this.shutdownService = shutdownService;
    }

    @GetMapping("/uptime")
    public ResponseEntity<UptimeResponse> getServerUptime() {
        return ResponseEntity.ok(uptimeService.currentUptime());
    }
    
    /**
     * Requests a graceful shutdown.
     * 
     * Returns 202 rather than 200 OK since work has been accepted
     * but not completed when response is written.
     *
     **/
    @PostMapping("/shutdown")
    public ResponseEntity<ShutdownResponse> shutdownServer() {
        shutdownService.requestShutdown();
        return ResponseEntity.accepted()
                .body(new ShutdownResponse("Graceful shutdown requested."));
    }
}
