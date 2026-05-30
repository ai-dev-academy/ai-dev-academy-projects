package com.aidevacademy.loaniq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Technique ⑤: Tree of Thoughts (ToT)
 *
 * PROBLEM: Standard CoT follows one reasoning path. For genuinely ambiguous
 * applications like #4821 — subprime credit but improving payments, borderline DTI
 * but stable employment — a single reasoning path may anchor on the first factor
 * it evaluates and weight everything else relative to that anchor.
 *
 * SOLUTION: Launch 3 reasoning branches, each starting from a different angle.
 * Score each branch's reasoning quality (1-10). Select the branch with the
 * highest score as the final reasoning path. The winning branch's conclusions
 * are used for the final decision.
 *
 * THE IMPROVEMENT:
 *   Linear CoT:  one fixed reasoning path → anchoring bias on first factor
 *   ToT:         3 independent paths, evaluated → the most complete reasoning wins
 *
 * SCORING: A second LLM call evaluates each branch for completeness, logical
 * consistency, and adherence to underwriting principles. The numeric score (1-10)
 * provides an objective comparison across branches.
 *
 * WHEN TO USE: High-value loans, borderline applications, or cases where your
 * compliance team flags that single-path reasoning is not defensible enough.
 *
 * COST TRADE-OFF: 3 branch calls + 3 scoring calls + 1 synthesis = 7 LLM calls.
 * Reserve for applications above a risk threshold (RISK_SCORE 5-6 from Technique ④).
 *
 * ENDPOINT: POST /underwriting/evaluate/tree-of-thoughts
 */
@Service
public class TreeOfThoughtsService {

    private final ChatClient chatClient;

    private static final List<String> BRANCHES = List.of(
        "Start your analysis by evaluating the CREDIT SCORE and historical default risk first.",
        "Start your analysis by evaluating the DEBT-TO-INCOME RATIO and repayment capacity first.",
        "Start your analysis by evaluating the PAYMENT HISTORY TREND and behavioral risk first."
    );

    public TreeOfThoughtsService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String evaluate(String applicationSummary) {
        // Phase 1: Run all 3 branches in parallel
        List<CompletableFuture<String>> branchFutures = BRANCHES.stream()
            .map(startingPoint -> CompletableFuture.supplyAsync(
                () -> runBranch(applicationSummary, startingPoint)
            ))
            .toList();

        List<String> branches = branchFutures.stream()
            .map(CompletableFuture::join)
            .toList();

        // Phase 2: Score each branch in parallel
        List<CompletableFuture<Integer>> scoreFutures = branches.stream()
            .map(reasoning -> CompletableFuture.supplyAsync(
                () -> scoreBranch(reasoning)
            ))
            .toList();

        List<Integer> scores = scoreFutures.stream()
            .map(CompletableFuture::join)
            .toList();

        // Phase 3: Select highest-scoring branch
        int bestIdx = 0;
        for (int i = 1; i < scores.size(); i++) {
            if (scores.get(i) > scores.get(bestIdx)) bestIdx = i;
        }

        String bestReasoning = branches.get(bestIdx);
        int    bestScore     = scores.get(bestIdx);

        // Phase 4: Generate final decision from the winning branch
        String finalDecision = synthesize(applicationSummary, bestReasoning);

        // Build full response showing all branches and why the winner won
        StringBuilder result = new StringBuilder();
        result.append("TREE OF THOUGHTS ANALYSIS\n");
        result.append("=".repeat(50)).append("\n\n");

        for (int i = 0; i < branches.size(); i++) {
            result.append(String.format("── Branch %d (score: %d/10) — %s\n",
                i + 1, scores.get(i), BRANCHES.get(i)));
            result.append(branches.get(i)).append("\n\n");
        }

        result.append(String.format("── WINNING BRANCH: %d (score: %d/10)\n\n",
            bestIdx + 1, bestScore));
        result.append("── FINAL DECISION (from winning branch):\n");
        result.append(finalDecision);

        return result.toString();
    }

    private String runBranch(String applicationSummary, String startingPoint) {
        return chatClient.prompt()
            .system("""
                You are a loan underwriting assistant for LoanIQ.
                Follow the starting angle specified by the user, then evaluate
                ALL remaining risk factors thoroughly.
                End with DECISION: [Approve / Conditional Approval / Reject]
                """)
            .user(startingPoint + "\n\nApplication:\n" + applicationSummary)
            .call()
            .content();
    }

    private int scoreBranch(String reasoning) {
        String scoreResponse = chatClient.prompt()
            .system("""
                You are a loan underwriting quality evaluator.
                Score the following reasoning 1-10 based on:
                  - Completeness: were all 4 risk factors (credit, DTI, payments, employment) addressed?
                  - Logical consistency: does the conclusion follow from the stated evidence?
                  - Adherence to lending principles: DTI thresholds, credit tier defaults cited correctly?
                Return ONLY a single integer from 1 to 10. No explanation.
                """)
            .user("Reasoning to score:\n" + reasoning)
            .call()
            .content();

        try {
            return Integer.parseInt(scoreResponse.trim().replaceAll("[^0-9]", "").substring(0, 1));
        } catch (Exception e) {
            return 5; // default if parsing fails
        }
    }

    private String synthesize(String applicationSummary, String bestReasoning) {
        return chatClient.prompt()
            .system("""
                You are a loan underwriting assistant for LoanIQ.
                Using the reasoning trace provided, generate the final underwriting decision.
                Format:
                  DECISION:   [Approve / Conditional Approval / Reject]
                  CONFIDENCE: [High / Medium / Low]
                  SUMMARY:    [one sentence for the applicant]
                  CONDITIONS: [any required conditions for Conditional Approval, or "None"]
                """)
            .user("Application:\n" + applicationSummary +
                  "\n\nBest reasoning branch:\n" + bestReasoning)
            .call()
            .content();
    }
}
