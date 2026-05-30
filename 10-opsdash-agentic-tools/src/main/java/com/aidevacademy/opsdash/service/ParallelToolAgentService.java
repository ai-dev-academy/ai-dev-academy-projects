package com.aidevacademy.opsdash.service;

import com.aidevacademy.opsdash.tools.OpsDashTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ③: Parallel Tool Calling
 *
 * PROBLEM: ReAct gathers data sequentially — one tool at a time. For an incident
 * with a known blast radius, you know upfront that you need metrics, logs, and
 * deploy history simultaneously. Running them one by one adds up:
 *   checkMetrics(400ms) → getDeployHistory(350ms) → getErrorLogs(300ms) = 1,050ms
 *
 * SOLUTION: Tell the LLM to call ALL independent tools in one round. Spring AI
 * returns multiple ToolCall objects from a single LLM response, executes them
 * in parallel threads, and sends all results back together.
 *   All three tools run simultaneously → total ≈ 400ms (not 1,050ms)
 *
 * FLOW:
 *   LLM response: [ToolCall("checkMetrics"), ToolCall("getDeployHistory"), ToolCall("getErrorLogs")]
 *        │
 *        ├──▶ checkServiceMetrics("checkout")   runs in thread-1
 *        ├──▶ getDeployHistory("checkout", 2)   runs in thread-2
 *        └──▶ getErrorLogs("checkout", ...)     runs in thread-3
 *                  all three complete
 *        │
 *        ▼
 *   LLM receives all 3 results → generates final answer
 *
 * WHEN TO USE vs ReAct:
 *   - Use Parallel when you know ALL the data sources you need upfront
 *   - Use ReAct when each result determines what to investigate next
 *
 * ENDPOINT: POST /incident/ask/parallel
 */
@Service
public class ParallelToolAgentService {

    private final ChatClient chatClient;
    private final OpsDashTools tools;

    public ParallelToolAgentService(ChatClient chatClient, OpsDashTools tools) {
        this.chatClient = chatClient;
        this.tools      = tools;
    }

    public String investigate(String question) {
        return chatClient.prompt()
            .system("""
                You are an incident investigator for OpsDash.

                SPEED IS CRITICAL. When a question requires data from multiple independent
                sources, call ALL relevant tools simultaneously in a single response.
                Do NOT call tools one at a time if they are independent of each other.

                For an incident report:
                  → Call checkServiceMetrics AND getDeployHistory AND getErrorLogs
                     in the SAME response round, not sequentially.

                After all tool results arrive, provide a complete incident report with:
                  1. Current state (metrics summary)
                  2. Timeline (what changed and when)
                  3. Root cause (evidence-based, from your tool results)
                  4. Recommended action (rollback / hotfix / escalate)
                """)
            .user(question)
            .tools(tools)
            .call()
            .content();
    }
}
