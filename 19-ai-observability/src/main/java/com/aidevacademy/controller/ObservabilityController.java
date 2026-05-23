package com.aidevacademy.controller;

import com.aidevacademy.service.ObservabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class ObservabilityController {

    private final ObservabilityService observabilityService;

    public ObservabilityController(ObservabilityService observabilityService) {
        this.observabilityService = observabilityService;
    }

    /**
     * POST /ai/chat  body: "What is observability?"
     * Every call is timed and tracked in Prometheus metrics
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody String message,
            @RequestParam(defaultValue = "gpt-4o-mini") String model) {
        try {
            return ResponseEntity.ok(observabilityService.trackedChat(message, model));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /ai/metrics — see request counts and success rate
     * For full Prometheus metrics: GET /actuator/prometheus
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        return ResponseEntity.ok(observabilityService.getMetricsSummary());
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 19 - Observability running! POST /ai/chat then GET /ai/metrics or /actuator/prometheus";
    }
}
