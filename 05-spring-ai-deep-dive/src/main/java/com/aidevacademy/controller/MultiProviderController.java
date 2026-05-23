package com.aidevacademy.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class MultiProviderController {

    private final ChatClient openAiClient;

    public MultiProviderController(ChatClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    /** Default provider (set in application.properties) */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody String message) {
        try {
            String response = openAiClient.prompt().user(message).call().content();
            return ResponseEntity.ok(Map.of("response", response, "provider", "openai"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** Advisor demonstration - logs every call */
    @PostMapping("/chat-with-logging")
    public ResponseEntity<Map<String, String>> chatWithLogging(@RequestBody String message) {
        long start = System.currentTimeMillis();
        try {
            String response = openAiClient.prompt().user(message).call().content();
            long duration = System.currentTimeMillis() - start;
            return ResponseEntity.ok(Map.of(
                    "response",   response,
                    "latencyMs",  String.valueOf(duration),
                    "model",      "gpt-4o-mini"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 05 - Spring AI Deep Dive running! POST to /ai/chat";
    }
}
