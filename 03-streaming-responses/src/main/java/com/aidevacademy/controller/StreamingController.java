package com.aidevacademy.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class StreamingController {

    private final ChatClient chatClient;

    public StreamingController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Streaming chat - tokens arrive one by one (Server-Sent Events)
     * GET /ai/stream?message=What is Java?
     * Open in browser or use EventSource in JavaScript
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .onErrorReturn("Stream error occurred. Please try again.");
    }

    /**
     * Non-streaming for comparison - waits for full response
     * POST /ai/chat  body: "Your question"
     */
    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 03 - Streaming Responses is running! GET /ai/stream?message=Hello to test streaming.";
    }
}
