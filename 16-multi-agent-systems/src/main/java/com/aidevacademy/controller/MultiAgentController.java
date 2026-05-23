package com.aidevacademy.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/pipeline")
@CrossOrigin(origins = "*")
public class MultiAgentController {

    private final ChatClient chatClient;

    public MultiAgentController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 3-agent pipeline: Researcher → Writer → Editor
     * POST /pipeline/run  body: "Java virtual threads"
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> run(@RequestBody String topic) {
        try {
            long start = System.currentTimeMillis();

            // Agent 1: Researcher
            String facts = chatClient.prompt()
                    .system("You are a fact-finder. Return 3 concise bullet-point facts about the topic. Facts only, no opinions.")
                    .user("Research: " + topic)
                    .call().content();

            // Agent 2: Writer (uses researcher output)
            String draft = chatClient.prompt()
                    .system("You are a technical writer. Write a clear 3-sentence explanation using the provided facts.")
                    .user("Facts:\n" + facts + "\n\nTopic: " + topic)
                    .call().content();

            // Agent 3: Editor (polishes the draft)
            String finalArticle = chatClient.prompt()
                    .system("You are a copy editor. Improve clarity, fix grammar, and make it more engaging. Return only the improved text.")
                    .user(draft)
                    .call().content();

            return ResponseEntity.ok(Map.of(
                    "topic",        topic,
                    "research",     facts,
                    "draft",        draft,
                    "final",        finalArticle,
                    "durationMs",   System.currentTimeMillis() - start
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Parallel research on multiple sub-topics
     * POST /pipeline/parallel  body: "Spring AI"
     */
    @PostMapping("/parallel")
    public ResponseEntity<Map<String, Object>> parallel(@RequestBody String topic) {
        try {
            // Run 3 research angles in parallel
            CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() ->
                chatClient.prompt().system("Research the history and origin of this topic in 2 sentences.")
                        .user(topic).call().content());
            CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() ->
                chatClient.prompt().system("Research the main features and capabilities in 2 sentences.")
                        .user(topic).call().content());
            CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() ->
                chatClient.prompt().system("Research real-world use cases and examples in 2 sentences.")
                        .user(topic).call().content());

            CompletableFuture.allOf(f1, f2, f3).join();

            String combined = f1.get() + "\n\n" + f2.get() + "\n\n" + f3.get();
            String summary = chatClient.prompt()
                    .system("Synthesize these research notes into a coherent 4-sentence summary.")
                    .user(combined).call().content();

            return ResponseEntity.ok(Map.of(
                    "topic",   topic,
                    "history", f1.get(),
                    "features", f2.get(),
                    "useCases", f3.get(),
                    "summary", summary
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 16 - Multi-Agent Systems running! POST /pipeline/run with a topic.";
    }
}
