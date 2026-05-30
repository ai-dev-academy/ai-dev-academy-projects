package com.aidevacademy.opsdash.service;

import com.aidevacademy.opsdash.tools.OpsDashTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ②: ReAct Agent (Reason + Act)
 *
 * PROBLEM: Basic @Tool calls one tool and answers. But real incidents require
 * a chain of investigation: "latency is high" → "which deploy caused it?" →
 * "what query changed?" → "is it the DB or the app?". One tool call cannot
 * answer multi-hop questions.
 *
 * SOLUTION: The ReAct pattern instructs the LLM to Think → Act → Observe in
 * a loop. Each tool result becomes the input for the NEXT reasoning step.
 * The LLM keeps going until it reaches a conclusive root cause or exhausts
 * its options.
 *
 * FLOW:
 *   Question: "Checkout latency 850ms since 3pm. Root cause?"
 *        │
 *        ▼ Think: "Need current metrics first"
 *   checkServiceMetrics("checkout")  → {p50:850ms, p99:1240ms}
 *        │
 *        ▼ Think: "High p99. Is there a deploy near 3pm?"
 *   getDeployHistory("checkout", 2)  → [v2.4.1 deployed at 15:02]
 *        │
 *        ▼ Think: "Deploy at exactly 3pm. Check DB queries since then"
 *   querySlowLogs("checkout", "15:00:00Z")  → [820ms N+1 query]
 *        │
 *        ▼ Conclude: "v2.4.1 introduced a missing JOIN index causing N+1"
 *
 * ENDPOINT: POST /incident/ask/react
 */
@Service
public class ReActAgentService {

    private final ChatClient chatClient;
    private final OpsDashTools tools;

    public ReActAgentService(ChatClient chatClient, OpsDashTools tools) {
        this.chatClient = chatClient;
        this.tools      = tools;
    }

    public String investigate(String question) {
        return chatClient.prompt()
            .system("""
                You are an incident investigator for OpsDash using the ReAct reasoning pattern.

                For every investigation, follow this loop:
                  THINK:   Reason about what you know so far and what you need next.
                  ACT:     Call ONE tool to get that specific piece of information.
                  OBSERVE: Read the tool result and update your reasoning.
                  REPEAT:  Continue until you have identified the root cause.

                Show your THINK / ACT / OBSERVE steps explicitly in your response.
                Stop only when you have:
                  - Identified the root cause with supporting evidence
                  - Named the specific service, deploy, query, or config that caused it
                  - Suggested a concrete fix (rollback, index, config change, etc.)

                Do NOT answer before completing at least 3 investigation steps.
                Do NOT guess any metric — call a tool to verify every data point.
                """)
            .user(question)
            .tools(tools)
            .call()
            .content();
    }
}
