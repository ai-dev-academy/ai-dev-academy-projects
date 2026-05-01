package com.aideva.deepdive.controller;

import com.aideva.deepdive.service.ResilientAiService;
import org.springframework.web.bind.annotation.*;

/**
 * POST /ai/chat   — single message
 * GET  /ai/health — verify AI config is correct
 *
 * curl -X POST http://localhost:8080/ai/chat \
 *      -H "Content-Type: application/json" \
 *      -d '"Explain the Spring AI Advisor pattern"'
 */
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ResilientAiService aiService;

    public ChatController(ResilientAiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return aiService.chat(message);
    }

    @GetMapping("/health")
    public String health() {
        return "Spring AI is configured and ready.";
    }
}
