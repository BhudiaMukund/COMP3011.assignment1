package au.edu.adelaide.stt.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import au.edu.adelaide.stt.service.UptimeService;
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

    public AdminController(UptimeService uptimeService) {
        this.uptimeService = uptimeService;
    }

    @GetMapping("/uptime")
    public ResponseEntity<UptimeResponse> getServerUptime() {
        return ResponseEntity.ok(uptimeService.currentUptime());
    }
}
