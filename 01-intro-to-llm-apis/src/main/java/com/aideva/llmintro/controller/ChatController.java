package com.aideva.llmintro.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * POST /ai/chat
     * Body: plain-text question (quoted JSON string)
     * Returns: AI-generated response
     *
     * Test with:
     *   curl -X POST http://localhost:8080/ai/chat \
     *        -H "Content-Type: application/json" \
     *        -d '"What is tokenization in LLMs?"'
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String message) {
        try {
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Never expose stack traces to clients
            String status = e.getMessage() != null && e.getMessage().contains("429")
                    ? "Rate limit reached. Please wait 60 seconds and retry."
                    : "AI service temporarily unavailable. Please try again.";
            return ResponseEntity.status(500).body(status);
        }
    }
}
