package com.aidevacademy.opsdash.controller;

import com.aidevacademy.opsdash.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OpsDash REST API — All 8 Agentic Tool-Use techniques exposed as endpoints.
 *
 * Quick start:
 *   1. export OPENAI_API_KEY=sk-...
 *   2. mvn spring-boot:run
 *   3. POST /incident/ask/basic-tool   (body: plain text question)
 *
 * All endpoints accept plain text in the request body.
 * All endpoints return JSON: { "technique": "...", "question": "...", "answer": "..." }
 *
 * Try this question against all 8 techniques to see the difference:
 *   "Checkout service latency spiked to 850ms after the 3pm deploy. SLA is 200ms. Root cause?"
 */
@RestController
@RequestMapping("/incident")
@CrossOrigin(origins = "*")
public class IncidentController {

    private final BasicToolAgentService     basicTool;
    private final ReActAgentService         react;
    private final ParallelToolAgentService  parallel;
    private final MemoryAgentService        memory;
    private final PlanAndExecuteService     planExecute;
    private final SelfCorrectingAgentService selfCorrecting;
    private final GuardrailedAgentService   guardrailed;
    private final MultiAgentDelegationService multiAgent;

    public IncidentController(
            BasicToolAgentService basicTool,
            ReActAgentService react,
            ParallelToolAgentService parallel,
            MemoryAgentService memory,
            PlanAndExecuteService planExecute,
            SelfCorrectingAgentService selfCorrecting,
            GuardrailedAgentService guardrailed,
            MultiAgentDelegationService multiAgent) {
        this.basicTool      = basicTool;
        this.react          = react;
        this.parallel       = parallel;
        this.memory         = memory;
        this.planExecute    = planExecute;
        this.selfCorrecting = selfCorrecting;
        this.guardrailed    = guardrailed;
        this.multiAgent     = multiAgent;
    }

    /**
     * Investigate an incident using any of the 8 agentic techniques.
     *
     * POST /incident/ask/{technique}
     * Body: plain text incident description
     *
     * Technique values:
     *   basic-tool | react | parallel | plan-execute | self-correcting | guardrailed | multi-agent
     *
     * Note: "memory" technique has its own endpoints below (stateful multi-turn).
     */
    @PostMapping("/ask/{technique}")
    public ResponseEntity<Map<String, String>> ask(
            @PathVariable String technique,
            @RequestBody String question) {
        try {
            String answer = switch (technique.toLowerCase()) {
                case "basic-tool"      -> basicTool.investigate(question);
                case "react"           -> react.investigate(question);
                case "parallel"        -> parallel.investigate(question);
                case "plan-execute"    -> planExecute.investigate(question);
                case "self-correcting" -> selfCorrecting.investigate(question);
                case "guardrailed"     -> guardrailed.investigate(question);
                case "multi-agent"     -> multiAgent.investigate(question);
                default                -> "Unknown technique. Valid values: " +
                    "basic-tool, react, parallel, plan-execute, self-correcting, guardrailed, multi-agent. " +
                    "For memory technique use POST /incident/memory/start";
            };
            return ResponseEntity.ok(Map.of(
                "technique", technique,
                "question",  question,
                "answer",    answer
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Memory Agent — start a new multi-turn investigation session.
     * Returns a sessionId. Use it for follow-up questions.
     *
     * POST /incident/memory/start
     * Body: plain text — your first question
     * Response: { "sessionId": "uuid", "answer": "..." }
     */
    @PostMapping("/memory/start")
    public ResponseEntity<Map<String, String>> memoryStart(@RequestBody String question) {
        try {
            return ResponseEntity.ok(memory.startSession(question));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Memory Agent — continue an existing investigation session.
     * The agent remembers all prior findings from this session.
     *
     * POST /incident/memory/{sessionId}
     * Body: plain text — your follow-up question
     */
    @PostMapping("/memory/{sessionId}")
    public ResponseEntity<Map<String, String>> memoryContinue(
            @PathVariable String sessionId,
            @RequestBody String question) {
        try {
            String answer = memory.continueSession(sessionId, question);
            return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "question",  question,
                "answer",    answer
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all available techniques with short descriptions.
     * GET /incident/techniques
     */
    @GetMapping("/techniques")
    public ResponseEntity<List<Map<String, String>>> techniques() {
        return ResponseEntity.ok(List.of(
            Map.of("technique", "basic-tool",      "stars", "⭐⭐",     "cost", "💰",
                   "description", "Single @Tool call — LLM picks the right tool automatically"),
            Map.of("technique", "react",           "stars", "⭐⭐⭐",   "cost", "💰💰",
                   "description", "Think→Act→Observe loop — chains tools to follow the evidence"),
            Map.of("technique", "parallel",        "stars", "⭐⭐⭐⭐", "cost", "💰💰",
                   "description", "All independent tools called simultaneously — 3× faster data gathering"),
            Map.of("technique", "memory/start",    "stars", "⭐⭐⭐",   "cost", "💰💰",
                   "description", "Stateful multi-turn investigation — no re-calling tools already fetched"),
            Map.of("technique", "plan-execute",    "stars", "⭐⭐⭐⭐", "cost", "💰💰💰",
                   "description", "Plan first, then execute — auditable investigation methodology"),
            Map.of("technique", "self-correcting", "stars", "⭐⭐⭐⭐", "cost", "💰💰💰",
                   "description", "Retries with adjusted params when tools fail or return empty"),
            Map.of("technique", "guardrailed",     "stars", "⭐⭐⭐⭐⭐","cost", "💰💰",
                   "description", "Read-only free, write actions require human approval"),
            Map.of("technique", "multi-agent",     "stars", "⭐⭐⭐⭐⭐","cost", "💰💰💰💰",
                   "description", "3 specialist agents in parallel + orchestrator synthesis")
        ));
    }
}
