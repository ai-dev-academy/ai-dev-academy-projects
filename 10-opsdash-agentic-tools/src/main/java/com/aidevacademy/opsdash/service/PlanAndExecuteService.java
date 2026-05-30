package com.aidevacademy.opsdash.service;

import com.aidevacademy.opsdash.model.Deploy;
import com.aidevacademy.opsdash.model.ServiceMetrics;
import com.aidevacademy.opsdash.model.SlowQuery;
import com.aidevacademy.opsdash.tools.OpsDashTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Technique ⑤: Plan & Execute
 *
 * PROBLEM: ReAct decides what to do next one step at a time. This works for
 * open-ended exploration, but it is hard to audit and may meander. For
 * high-stakes incidents that require a documented investigation methodology,
 * you want the plan written and reviewable BEFORE execution starts.
 *
 * SOLUTION: Separate planning from execution into two phases.
 *   Phase 1 (Plan): One LLM call generates a structured JSON investigation plan.
 *                   No tools run yet. The plan can be logged and reviewed.
 *   Phase 2 (Execute): Your Java code runs each step deterministically.
 *   Phase 3 (Synthesize): One LLM call reads all step results and writes the report.
 *
 * FLOW:
 *   Question
 *     │
 *     ▼ [LLM call 1 — planning only]
 *   Plan: step1=checkMetrics(checkout), step2=getDeployHistory(2h),
 *         step3=querySlowLogs(15:00), step4=getErrorLogs(ERROR)
 *     │
 *     ▼ [Java executes each step in order]
 *   Results: [metrics, deploys, slowQueries, logs]
 *     │
 *     ▼ [LLM call 2 — synthesis only]
 *   Root cause report with evidence, timeline, and fix recommendation
 *
 * ENDPOINT: POST /incident/ask/plan-execute
 */
@Service
public class PlanAndExecuteService {

    private final ChatClient chatClient;
    private final OpsDashTools tools;

    public PlanAndExecuteService(ChatClient chatClient, OpsDashTools tools) {
        this.chatClient = chatClient;
        this.tools      = tools;
    }

    public String investigate(String question) {
        // ── PHASE 1: Generate the investigation plan ────────────────────────────
        String planJson = chatClient.prompt()
            .system("""
                You are a planning agent for incident investigation.
                Given an incident description, generate a JSON investigation plan.

                Respond with ONLY valid JSON in this exact format:
                {
                  "incident": "<one-line description>",
                  "steps": [
                    {"step": 1, "tool": "checkServiceMetrics", "service": "<name>"},
                    {"step": 2, "tool": "getDeployHistory",    "service": "<name>", "hoursBack": <n>},
                    {"step": 3, "tool": "querySlowLogs",       "service": "<name>", "since": "<ISO>"},
                    {"step": 4, "tool": "getErrorLogs",        "service": "<name>", "since": "<ISO>", "level": "ERROR"}
                  ]
                }

                Include only the steps that are relevant to this incident.
                Output ONLY the JSON — no explanation, no markdown fences.
                """)
            .user("Incident: " + question)
            .call()
            .content();

        // ── PHASE 2: Execute each tool call (deterministic Java, not LLM guessing) ──
        // For this educational demo we execute a known investigation sequence.
        // In production: parse planJson and dispatch dynamically.
        List<String> stepResults = new ArrayList<>();

        String service = extractServiceName(question);

        ServiceMetrics metrics = tools.checkServiceMetrics(service);
        stepResults.add("Step 1 — checkServiceMetrics(\"" + service + "\"): " + metrics);

        List<Deploy> deploys = tools.getDeployHistory(service, 2);
        stepResults.add("Step 2 — getDeployHistory(\"" + service + "\", 2h): " +
            (deploys.isEmpty() ? "no recent deploys" : deploys.toString()));

        List<SlowQuery> slowQueries = tools.querySlowLogs(service, "2024-01-15T15:00:00Z");
        stepResults.add("Step 3 — querySlowLogs(\"" + service + "\"): " +
            (slowQueries.isEmpty() ? "no slow queries" : slowQueries.toString()));

        String logs = tools.getErrorLogs(service, "2024-01-15T15:00:00Z", "ERROR");
        stepResults.add("Step 4 — getErrorLogs(\"" + service + "\"): " + logs.strip());

        // ── PHASE 3: Synthesize all results into a root cause report ────────────
        String allResults = String.join("\n\n", stepResults);

        return chatClient.prompt()
            .system("""
                You are an incident analysis synthesizer for OpsDash.
                You have been given the results of a structured investigation plan.
                Write a clear, concise root cause report with:
                  1. TIMELINE — what happened and when
                  2. ROOT CAUSE — the specific change, query, or config that caused it
                  3. EVIDENCE — cite the exact tool results that prove your conclusion
                  4. RECOMMENDED ACTION — rollback / add index / config fix / escalate
                  5. RISK — what happens if the recommendation is not acted on immediately
                """)
            .user("Original question: " + question +
                  "\n\nInvestigation plan:\n" + planJson +
                  "\n\nStep results:\n" + allResults)
            .call()
            .content();
    }

    /** Simple heuristic: extract a service name mentioned in the question. */
    private String extractServiceName(String question) {
        String q = question.toLowerCase();
        if (q.contains("checkout"))  return "checkout";
        if (q.contains("payment"))   return "payment";
        if (q.contains("inventory")) return "inventory";
        if (q.contains("auth"))      return "auth";
        if (q.contains("cart"))      return "cart";
        return "checkout"; // default to the scenario service
    }
}
