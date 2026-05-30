package com.aidevacademy.loaniq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Collections.frequency;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Technique ③: Self-Consistency CoT
 *
 * PROBLEM: Any single CoT call can "get unlucky" — a borderline application like #4821
 * might be Approved on one call, Conditional on the next. LLM outputs have variance.
 * At 800 applications/day, even a 20% decision flip rate means 160 inconsistent
 * decisions, creating legal exposure and auditor red flags.
 *
 * SOLUTION: Run the SAME prompt 5 times at temperature > 0. Each run follows a
 * different random reasoning path. Take the MAJORITY VOTE across all 5 decisions.
 * Statistical noise averages out — the majority vote is more reliable than any
 * single run, and the confidence score tells you when to escalate to a human.
 *
 * THE IMPROVEMENT over Zero-Shot / Few-Shot:
 *   Single run:  decision confidence unknown — could be a statistical outlier
 *   5-run vote:  "Conditional 4/5 → 80% confidence" — quantified, defensible
 *
 * ESCALATION RULE: If confidence < 60%, the application is genuinely on the fence.
 * Routing it to a human underwriter is the right answer, not a system failure.
 *
 * COST TRADE-OFF: 5× LLM calls per application. Use this for borderline cases only.
 * Route clear approvals and clear rejects through Zero-Shot CoT (1× call).
 *
 * ENDPOINT: POST /underwriting/evaluate/self-consistency
 */
@Service
public class SelfConsistencyCoTService {

    private static final int RUNS = 5;
    private static final double ESCALATION_THRESHOLD = 0.60;

    private final ChatClient chatClient;

    public SelfConsistencyCoTService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String evaluate(String applicationSummary) {
        // Run 5 times in parallel — each uses temperature > 0 for different reasoning paths
        // Spring AI uses the model's default temperature; for true variance you would configure
        // a separate ChatClient with temperature=0.7 via ChatOptions
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < RUNS; i++) {
            final int run = i + 1;
            futures.add(CompletableFuture.supplyAsync(() -> runOnce(applicationSummary, run)));
        }

        // Collect all decisions
        List<String> decisions = futures.stream()
            .map(CompletableFuture::join)
            .map(this::extractDecision)
            .collect(toList());

        // Majority vote
        Map<String, Long> counts = decisions.stream()
            .collect(groupingBy(identity(), counting()));

        String finalDecision = counts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Conditional Approval");

        long winnerCount = counts.getOrDefault(finalDecision, 0L);
        double confidence = (double) winnerCount / RUNS;

        // Build the full response with all runs and the final verdict
        StringBuilder result = new StringBuilder();
        result.append("SELF-CONSISTENCY ANALYSIS\n");
        result.append("=".repeat(50)).append("\n\n");
        result.append(String.format("Runs completed:    %d\n", RUNS));
        result.append(String.format("Decision counts:   %s\n", counts));
        result.append(String.format("Winning decision:  %s (%d/%d runs)\n",
            finalDecision, winnerCount, RUNS));
        result.append(String.format("Confidence:        %.0f%%\n\n", confidence * 100));

        if (confidence < ESCALATION_THRESHOLD) {
            result.append("ACTION: LOW CONFIDENCE — ESCALATE TO HUMAN UNDERWRITER\n");
            result.append(String.format("Reason: No decision reached %.0f%% agreement threshold.\n\n",
                ESCALATION_THRESHOLD * 100));
        } else {
            result.append(String.format("FINAL DECISION: %s\n\n", finalDecision));
        }

        result.append("INDIVIDUAL RUNS:\n");
        for (int i = 0; i < futures.size(); i++) {
            result.append(String.format("  Run %d: %s\n", i + 1, decisions.get(i)));
        }

        return result.toString();
    }

    private String runOnce(String applicationSummary, int runNumber) {
        return chatClient.prompt()
            .system("""
                You are a loan underwriting assistant for LoanIQ.
                Think step by step through all four risk factors.
                End with DECISION: [Approve / Conditional Approval / Reject]
                """)
            .user(applicationSummary)
            .call()
            .content();
    }

    /** Extract just the DECISION line from a CoT response */
    private String extractDecision(String response) {
        return response.lines()
            .filter(line -> line.toUpperCase().startsWith("DECISION:"))
            .findFirst()
            .map(line -> line.replaceFirst("(?i)DECISION:\\s*", "").trim())
            .map(d -> {
                // Normalize to canonical values
                String upper = d.toUpperCase();
                if (upper.contains("REJECT"))      return "Reject";
                if (upper.contains("CONDITIONAL")) return "Conditional Approval";
                if (upper.contains("APPROVE"))     return "Approve";
                return "Conditional Approval"; // default borderline
            })
            .orElse("Conditional Approval");
    }
}
