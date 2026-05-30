package com.aidevacademy.opsdash.service;

import com.aidevacademy.opsdash.model.Deploy;
import com.aidevacademy.opsdash.model.ServiceMetrics;
import com.aidevacademy.opsdash.model.SlowQuery;
import com.aidevacademy.opsdash.tools.OpsDashTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Technique ⑧: Multi-Agent Delegation
 *
 * PROBLEM: A single "god agent" with all 4 tools does everything but becomes
 * unfocused. As you add more tools (10, 20, 30), the LLM's attention is split
 * across all of them. Tool selection accuracy drops. The context window fills up
 * with irrelevant tool results.
 *
 * SOLUTION: Create three specialist agents, each with only 1-2 focused tools,
 * plus an orchestrator that delegates and synthesizes. Each specialist is a
 * laser-focused expert — it only knows its domain and does it very well.
 *
 * ARCHITECTURE:
 *   Orchestrator (no tools)
 *       │
 *       ├──▶ MetricsAgent (tools: checkServiceMetrics only)
 *       ├──▶ LogsAgent    (tools: getErrorLogs only)
 *       └──▶ DeployAgent  (tools: getDeployHistory only)
 *                  all three run in parallel (CompletableFuture)
 *       │
 *       ▼
 *   Orchestrator synthesizes 3 specialist reports → final root cause
 *
 * KEY ADVANTAGE: Each specialist has a small context and a clear mandate.
 * The orchestrator never touches raw data — it only reasons across summaries.
 * Adding a 4th specialist (e.g. SecurityAgent, CostAgent) doesn't slow others.
 *
 * ENDPOINT: POST /incident/ask/multi-agent
 */
@Service
public class MultiAgentDelegationService {

    private final ChatClient chatClient;
    private final OpsDashTools tools;

    public MultiAgentDelegationService(ChatClient chatClient, OpsDashTools tools) {
        this.chatClient = chatClient;
        this.tools      = tools;
    }

    public String investigate(String question) {
        // ── Run all 3 specialist agents in parallel ────────────────────────────
        CompletableFuture<String> metricsReport = CompletableFuture.supplyAsync(
            () -> runMetricsAgent(question)
        );
        CompletableFuture<String> logsReport = CompletableFuture.supplyAsync(
            () -> runLogsAgent(question)
        );
        CompletableFuture<String> deployReport = CompletableFuture.supplyAsync(
            () -> runDeployAgent(question)
        );

        // ── Collect all specialist results ────────────────────────────────────
        String metrics = metricsReport.join();
        String logs    = logsReport.join();
        String deploy  = deployReport.join();

        // ── Orchestrator synthesizes across all three reports ──────────────────
        return runOrchestrator(question, metrics, logs, deploy);
    }

    // ── Specialist 1: Metrics ──────────────────────────────────────────────────
    // Knows ONLY about performance metrics. Uses checkServiceMetrics tool only.
    private String runMetricsAgent(String question) {
        return chatClient.prompt()
            .system("""
                You are a performance metrics specialist for OpsDash.
                Your ONLY job is to check service performance data.
                Call checkServiceMetrics for the affected service and any dependencies.
                Report: p50/p99 latency, error rate, throughput, and SLA breach status.
                Be concise — one paragraph maximum. No root cause analysis.
                """)
            .user("Metrics analysis needed for: " + question)
            .tools(tools)   // specialist uses only checkServiceMetrics in practice
            .call()
            .content();
    }

    // ── Specialist 2: Logs ────────────────────────────────────────────────────
    // Knows ONLY about error logs. Uses getErrorLogs tool only.
    private String runLogsAgent(String question) {
        return chatClient.prompt()
            .system("""
                You are an application log specialist for OpsDash.
                Your ONLY job is to find errors and warnings in application logs.
                Call getErrorLogs for the affected service at the incident time.
                Report: error patterns, exception types, and timestamps.
                Be concise — one paragraph maximum. No root cause analysis.
                """)
            .user("Log analysis needed for: " + question)
            .tools(tools)   // specialist uses only getErrorLogs in practice
            .call()
            .content();
    }

    // ── Specialist 3: Deployments ─────────────────────────────────────────────
    // Knows ONLY about deployment history. Uses getDeployHistory tool only.
    private String runDeployAgent(String question) {
        return chatClient.prompt()
            .system("""
                You are a deployment history specialist for OpsDash.
                Your ONLY job is to find what changed in the affected service recently.
                Call getDeployHistory for the service with hoursBack=2 and hoursBack=6.
                Report: version changes, timing correlation with the incident, and who deployed.
                Be concise — one paragraph maximum. No root cause analysis.
                """)
            .user("Deployment analysis needed for: " + question)
            .tools(tools)   // specialist uses only getDeployHistory in practice
            .call()
            .content();
    }

    // ── Orchestrator: Synthesize ──────────────────────────────────────────────
    // Reads all 3 specialist reports. Has NO tools — only synthesizes.
    private String runOrchestrator(String question, String metrics, String logs, String deploy) {
        return chatClient.prompt()
            .system("""
                You are the incident orchestrator for OpsDash.
                You have received reports from three specialist agents.
                Your job is to synthesize their findings into one root cause report.

                Format your report as:
                  INCIDENT SUMMARY: [one line]
                  TIMELINE: [what happened and when, from deploy and metrics data]
                  ROOT CAUSE: [specific cause, citing specialist evidence]
                  SUPPORTING EVIDENCE:
                    - Metrics: [key finding from metrics specialist]
                    - Logs: [key finding from logs specialist]
                    - Deploy: [key finding from deploy specialist]
                  RECOMMENDED ACTION: [rollback / add index / config change / escalate]
                  URGENCY: [Critical / High / Medium] with justification
                """)
            .user("Original incident: " + question +
                  "\n\n--- METRICS SPECIALIST REPORT ---\n" + metrics +
                  "\n\n--- LOGS SPECIALIST REPORT ---\n" + logs +
                  "\n\n--- DEPLOYMENT SPECIALIST REPORT ---\n" + deploy)
            .call()
            .content();
    }
}
