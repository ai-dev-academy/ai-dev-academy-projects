package com.aidevacademy.controller;

import com.aidevacademy.service.OrderAgent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/agent")
@CrossOrigin(origins = "*")
public class AgentController {

    private final OrderAgent orderAgent;

    public AgentController(OrderAgent orderAgent) {
        this.orderAgent = orderAgent;
    }

    /**
     * POST /agent/chat  body: "What is the status of order ORD-001?"
     * The AI will automatically call the right tool(s)
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody String message) {
        try {
            String response = orderAgent.chat(message);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 11 - Tool Calling running! Try: POST /agent/chat with 'What is status of ORD-001?'";
    }
}
