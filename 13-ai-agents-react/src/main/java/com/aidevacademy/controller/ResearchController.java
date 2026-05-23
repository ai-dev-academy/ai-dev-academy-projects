package com.aidevacademy.controller;

import com.aidevacademy.service.ResearchAgent;
import com.aidevacademy.tools.ResearchTools;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/agent")
@CrossOrigin(origins = "*")
public class ResearchController {

    private final ResearchAgent agent;
    private final ResearchTools tools;

    public ResearchController(ResearchAgent agent, ResearchTools tools) {
        this.agent = agent;
        this.tools = tools;
    }

    /**
     * POST /agent/research  body: "Spring AI vs LangChain4j"
     * The agent will search, save notes, and produce a report autonomously
     */
    @PostMapping("/research")
    public ResponseEntity<Map<String, String>> research(@RequestBody String topic) {
        try {
            tools.clearNotes();
            long start = System.currentTimeMillis();
            String result = agent.research(topic);
            long duration = System.currentTimeMillis() - start;
            return ResponseEntity.ok(Map.of(
                    "topic",    topic,
                    "report",   result,
                    "timeMs",   String.valueOf(duration)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 13 - AI Agents (ReAct) running! POST /agent/research with a topic to research.";
    }
}
