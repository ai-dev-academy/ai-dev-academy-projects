package com.aidevacademy.controller;

import com.aidevacademy.service.CostTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class CostController {

    private final CostTrackingService costService;

    public CostController(CostTrackingService costService) {
        this.costService = costService;
    }

    /**
     * POST /ai/chat  body: "What is Spring Boot?"
     * Returns response + exact token cost
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody String message) {
        try {
            return ResponseEntity.ok(costService.chatWithCostTracking(message, "/ai/chat"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /ai/costs — see spending breakdown per endpoint
     */
    @GetMapping("/costs")
    public ResponseEntity<Map<String, Object>> getCosts() {
        return ResponseEntity.ok(costService.getCostReport());
    }

    /**
     * DELETE /ai/costs — reset cost counters
     */
    @DeleteMapping("/costs")
    public ResponseEntity<Map<String, String>> resetCosts() {
        costService.resetCosts();
        return ResponseEntity.ok(Map.of("status", "Cost counters reset"));
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 18 - Cost Tracking running! POST /ai/chat then GET /ai/costs to see spending.";
    }
}
