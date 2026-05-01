package com.aideva.streaming.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Streaming endpoint using Server-Sent Events (SSE).
 *
 * Test in browser: http://localhost:8080/ai/stream?message=Tell+me+about+Java
 *
 * Or with curl:
 *   curl -N "http://localhost:8080/ai/stream?message=Hello"
 *
 * Frontend (JavaScript):
 *   const es = new EventSource('/ai/stream?message=Hello');
 *   es.onmessage = e => console.log(e.data);
 */
@RestController
@RequestMapping("/ai")
public class StreamController {

    private final ChatClient chatClient;

    public StreamController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .onErrorResume(e -> Flux.just("[Error: " + e.getMessage() + "]"));
    }
}
