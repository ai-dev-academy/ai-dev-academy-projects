package com.aidevacademy.controller;

import com.aidevacademy.service.ResilientAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class ResilientController {

    private final ResilientAiService aiService;

    public ResilientController(ResilientAiService aiService) {
        this.aiService = aiService;
    }

    /**
     * POST /ai/chat  body: "What is Spring Boot?"
     * Has retry, circuit breaker, and caching built in
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody String message) {
        try {
            String response = aiService.chat(message);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("error", "Service unavailable: " + e.getMessage()));
        }
    }

    /** GET /ai/cache-stats — see hit rate and cache size */
    @GetMapping("/cache-stats")
    public ResponseEntity<Map<String, Object>> cacheStats() {
        return ResponseEntity.ok(aiService.getCacheStats());
    }

    /** DELETE /ai/cache — clear the cache */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, String>> clearCache() {
        aiService.clearCache();
        return ResponseEntity.ok(Map.of("status", "Cache cleared"));
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 17 - Resilience Patterns running! POST /ai/chat — includes retry + circuit breaker + cache.";
    }
}
