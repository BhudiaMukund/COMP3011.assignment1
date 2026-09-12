package au.edu.adelaide.stt.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import au.edu.adelaide.stt.service.TokenUsageService;
import au.edu.adelaide.stt.web.dto.GlobalStatsResponse;

/**
 * Aggregate usage counters defined in the OpenAPI specification.
 * Stateless, like other controllers in this app.
 */
@RestController
@RequestMapping("/api/v1/global")
public class GlobalStatsController {

    private final TokenUsageService tokenUsageService;

    public GlobalStatsController(TokenUsageService tokenUsageService) {
        this.tokenUsageService = tokenUsageService;
    }

    @GetMapping("/stats")
    public ResponseEntity<GlobalStatsResponse> getGlobalStats() {
        return ResponseEntity.ok(tokenUsageService.snapshot());
    }
}