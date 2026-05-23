package com.aidevacademy.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** Basic chat endpoint - send any message, get AI response */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String message) {
        try {
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("AI error: " + e.getMessage());
        }
    }

    /** Chat with custom persona via system prompt */
    @PostMapping("/chat-with-persona")
    public ResponseEntity<String> chatWithPersona(
            @RequestParam(defaultValue = "You are a helpful Java developer assistant.") String persona,
            @RequestBody String message) {
        try {
            String response = chatClient.prompt()
                    .system(persona)
                    .user(message)
                    .call()
                    .content();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 01 - Intro to LLM APIs is running! POST to /ai/chat to test.";
    }
}
