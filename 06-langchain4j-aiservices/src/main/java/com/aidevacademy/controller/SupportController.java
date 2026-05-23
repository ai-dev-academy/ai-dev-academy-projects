package com.aidevacademy.controller;

import com.aidevacademy.service.CustomerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/support")
@CrossOrigin(origins = "*")
public class SupportController {

    private final CustomerSupport support;

    public SupportController(CustomerSupport support) {
        this.support = support;
    }

    /**
     * POST /support/chat?userId=user123
     * body: "What is RAG?"
     * Each userId gets separate conversation memory
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestParam(defaultValue = "anonymous") String userId,
            @RequestBody String message) {
        try {
            String response = support.chat(userId, message);
            return ResponseEntity.ok(Map.of("response", response, "userId", userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 06 - LangChain4j AiServices running! POST to /support/chat?userId=yourId";
    }
}
