package com.aidevacademy.opsdash.service;

import com.aidevacademy.opsdash.tools.OpsDashTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ⑥: Self-Correcting Agent
 *
 * PROBLEM: Production tool calls fail. Prometheus is flaky under load. CloudWatch
 * rate-limits. The DB query log API times out. A basic agent gives up or hallucinates
 * when a tool returns empty, times out, or throws an exception.
 *
 * SOLUTION: Encode self-correction rules directly in the system message. When a tool
 * fails or returns unexpected data, the LLM's instructions tell it exactly how to
 * recover: retry with a wider time window, try a different log level, or fall back
 * to a different tool that covers the same data need.
 *
 * FLOW (success path):
 *   getDeployHistory("checkout", 1h) → [v2.4.1 at 15:02]  ← OK, continue
 *
 * FLOW (failure path — LLM self-corrects):
 *   getErrorLogs("checkout", "15:00Z", "ERROR") → "" (empty — maybe rate limited)
 *     │ LLM notes: "Tool returned empty. Retry with WARN level and wider window."
 *     ▼
 *   getErrorLogs("checkout", "14:00Z", "WARN")  → logs found
 *     │ Continue investigation with this data
 *
 * KEY INSIGHT: The self-correction instructions are in the system prompt —
 * no Java retry logic needed. The LLM handles the recovery strategy.
 *
 * ENDPOINT: POST /incident/ask/self-correcting
 */
@Service
public class SelfCorrectingAgentService {

    private final ChatClient chatClient;
    private final OpsDashTools tools;

    public SelfCorrectingAgentService(ChatClient chatClient, OpsDashTools tools) {
        this.chatClient = chatClient;
        this.tools      = tools;
    }

    public String investigate(String question) {
        return chatClient.prompt()
            .system("""
                You are a resilient incident investigator for OpsDash.
                Your tools may be slow, return empty results, or fail during an incident.

                SELF-CORRECTION RULES — apply them automatically:
                  1. If a tool returns empty results or an error:
                     - Note the failure explicitly: "Tool X returned empty — retrying"
                     - Retry with adjusted parameters:
                         * checkServiceMetrics: try a downstream dependency service instead
                         * getDeployHistory: increase hoursBack from 2 to 6
                         * getErrorLogs: widen time window by 1 hour, or switch level WARN→ERROR
                         * querySlowLogs: try an earlier sinceTimestamp

                  2. If a tool fails 2 times with the same parameters:
                     - Switch to a different tool that covers the same data need
                     - E.g. if getErrorLogs fails → use querySlowLogs to find DB evidence

                  3. Give up on a specific data source only after 3 total attempts.
                     Proceed with available data and note what is missing.

                NEVER guess or hallucinate data values.
                NEVER say "the error rate is probably X" — only use verified tool results.
                """)
            .user(question)
            .tools(tools)
            .call()
            .content();
    }
}
