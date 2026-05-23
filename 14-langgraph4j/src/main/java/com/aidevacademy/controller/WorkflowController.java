package com.aidevacademy.controller;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// Topic 14 placeholder - LangGraph4j requires separate dependency
// Full workflow implementation shown here as service-pattern demo
@RestController @RequestMapping("/workflow") @CrossOrigin(origins = "*")
public class WorkflowController {
    private final ChatClient chatClient;
    public WorkflowController(ChatClient chatClient) { this.chatClient = chatClient; }

    /** Simulates a 3-step content approval workflow: draft → review → publish */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runWorkflow(@RequestBody String topic) {
        try {
            // Step 1: Draft
            String draft = chatClient.prompt()
                    .system("You are a technical writer. Write a concise 2-sentence tweet about the topic.")
                    .user(topic).call().content();

            // Step 2: Review
            String review = chatClient.prompt()
                    .system("You are an editor. Rate this tweet 1-10 for clarity. Reply with just a number.")
                    .user(draft).call().content().trim();

            int score = Integer.parseInt(review.replaceAll("[^0-9]", ""));

            // Step 3: Conditional publish or revise
            String finalContent = draft;
            String status = "published";
            if (score < 7) {
                finalContent = chatClient.prompt()
                        .system("You are a writer. Improve this tweet to be clearer and more engaging.")
                        .user(draft).call().content();
                status = "revised_and_published";
            }

            return ResponseEntity.ok(Map.of(
                    "topic", topic, "draft", draft,
                    "reviewScore", score, "finalContent", finalContent, "status", status
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public String health() { return "Topic 14 - LangGraph4j Workflow running! POST /workflow/run"; }
}
