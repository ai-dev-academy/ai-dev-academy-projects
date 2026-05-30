package com.aidevacademy.opsdash.service;

import com.aidevacademy.opsdash.tools.OpsDashTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Technique ⑦: Guardrailed Agent
 *
 * PROBLEM: An unrestricted agent can call any @Tool method — including ones with
 * side effects. In an incident, you want the LLM to read metrics and logs freely,
 * but paging engineers, posting to Slack, or triggering rollbacks must require
 * human confirmation. Letting an LLM page a VP at 3am without confirmation is
 * a trust-destroying incident.
 *
 * SOLUTION: Classify tools into three tiers:
 *   READ_ONLY — agent calls freely (no side effects)
 *   CONFIRM_REQUIRED — agent PROPOSES the action; a human approves before execution
 *   BLOCKED — never exposed to the LLM at all; requires a human to trigger manually
 *
 * FLOW:
 *   LLM calls checkServiceMetrics("checkout")  ← READ_ONLY: executes immediately
 *   LLM calls getDeployHistory("checkout", 2)  ← READ_ONLY: executes immediately
 *   LLM proposes createPagerDutyIncident(...)  ← CONFIRM_REQUIRED: BLOCKED for review
 *                                                  Human sees: "Approve? Y/N"
 *   rollbackDeployment(...)                    ← BLOCKED: not in @Tool catalog at all
 *
 * IMPLEMENTATION: The GuardrailAdvisor intercepts all tool calls before execution,
 * checks the tool name against the CONFIRM_REQUIRED list, and either:
 *   - Executes it immediately (READ_ONLY)
 *   - Stops and asks for approval (CONFIRM_REQUIRED)
 *   - Rejects the call (BLOCKED — though this shouldn't happen if tools are not exposed)
 *
 * NOTE: In this educational demo, confirmationHandler simply logs the proposed
 * action and returns "PENDING APPROVAL" instead of executing. In production,
 * replace it with a Slack message or a UI approval dialog.
 *
 * ENDPOINT: POST /incident/ask/guardrailed
 */
@Service
public class GuardrailedAgentService {

    // READ_ONLY tools — agent calls freely
    private static final Set<String> READ_ONLY = Set.of(
        "checkServiceMetrics",
        "getDeployHistory",
        "querySlowLogs",
        "getErrorLogs"
    );

    // CONFIRM_REQUIRED tools — agent can propose; human approves
    // These are defined in the system prompt as available in concept, but
    // the agent is instructed to DESCRIBE the action instead of calling a tool.
    // In a real implementation, you would add restricted @Tool methods here
    // and intercept them via a ToolCallInterceptor/Advisor before execution.
    private static final Set<String> CONFIRM_REQUIRED = Set.of(
        "createPagerDutyIncident",
        "sendSlackAlert",
        "updateStatusPage"
    );

    private final ChatClient chatClient;
    private final OpsDashTools tools;    // only READ_ONLY tools are exposed

    public GuardrailedAgentService(ChatClient chatClient, OpsDashTools tools) {
        this.chatClient = chatClient;
        this.tools      = tools;
    }

    public String investigate(String question) {
        return chatClient.prompt()
            .system("""
                You are a guardrailed incident investigator for OpsDash.

                TOOL ACCESS TIERS:
                  READ_ONLY (call freely):
                    - checkServiceMetrics — read metrics from Prometheus
                    - getDeployHistory — read deployment history from CI/CD
                    - querySlowLogs — read slow query log from RDS
                    - getErrorLogs — read application logs from CloudWatch

                  CONFIRM_REQUIRED (do NOT call — describe instead):
                    - createPagerDutyIncident — pages on-call engineers
                    - sendSlackAlert — posts to #incidents channel
                    - updateStatusPage — marks service degraded for customers

                  BLOCKED (never available — human only):
                    - rollbackDeployment — requires manual approval via CI/CD
                    - scaleDownService — requires ops lead sign-off

                GUARDRAIL RULE: When your investigation determines that a
                CONFIRM_REQUIRED action is needed, do NOT call a tool.
                Instead, write:
                  PROPOSED ACTION: [action name]
                  REASON: [why this action is needed based on your findings]
                  IMPACT: [who will be notified / what will happen]
                  AWAITING HUMAN APPROVAL

                This output is captured and sent to the on-call engineer for review.
                """)
            .user(question)
            .tools(tools)   // only READ_ONLY tools registered — confirm-required tools not exposed
            .call()
            .content();
    }
}
