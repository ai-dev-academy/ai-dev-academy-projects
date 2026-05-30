package com.aidevacademy.loaniq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ⑧: CoT + Self-Critique
 *
 * PROBLEM: Even thorough step-by-step reasoning can contain subtle errors.
 * In Application #4821, a Zero-Shot CoT reasoned:
 *   "Step 3 (Payments): 3 missed payments — minor concern."
 *   "DECISION: Approve"
 * But Step 3 contradicts the decision: 3 missed payments for a subprime borrower
 * is NOT minor — it's the exact condition that triggers the compensating factor
 * requirement. The model's conclusion didn't match its own stated reasoning.
 *
 * SOLUTION: Run three passes:
 *   Pass 1 (Generate):  Standard CoT — full reasoning trace + decision
 *   Pass 2 (Critique):  A reviewer LLM reads the reasoning and finds:
 *                       - Logical errors between steps and conclusion
 *                       - Risk factors that were underweighted or overlooked
 *                       - Conclusions that contradict the stated reasoning
 *   Pass 3 (Correct):   The original reasoning + the critique → regenerate correctly
 *
 * THE IMPROVEMENT:
 *   Single CoT:      self-contradictions go undetected → wrong decision filed
 *   Self-Critique:   contradiction caught in Pass 2 → corrected before filing
 *
 * COMPLIANCE VALUE: All three passes are stored as the audit log. Regulators see
 * not just the final decision but the full internal quality-control process.
 *
 * ENDPOINT: POST /underwriting/evaluate/self-critique
 */
@Service
public class SelfCritiqueCoTService {

    private final ChatClient chatClient;

    public SelfCritiqueCoTService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String evaluate(String applicationSummary) {
        // Pass 1: Initial CoT reasoning
        String initialResponse = chatClient.prompt()
            .system("""
                You are a loan underwriting assistant for LoanIQ.
                Think step by step. Evaluate ALL four risk factors thoroughly.
                Provide a complete reasoning trace and final decision.

                Format:
                REASONING:
                  Step 1 (Credit score):  [analysis]
                  Step 2 (DTI):           [analysis]
                  Step 3 (Payments):      [analysis]
                  Step 4 (Employment):    [analysis]
                DECISION:   [Approve / Conditional Approval / Reject]
                CONFIDENCE: [High / Medium / Low]
                """)
            .user(applicationSummary)
            .call()
            .content();

        // Pass 2: Self-critique — look for errors in the reasoning trace
        String critique = chatClient.prompt()
            .system("""
                You are a loan underwriting quality control reviewer for LoanIQ.
                Your role is to find errors in underwriting reasoning — not to make decisions.

                Review the reasoning trace below and identify ALL of the following:
                  1. LOGICAL ERRORS — steps that contradict each other
                  2. CONCLUSION MISMATCH — a DECISION that does not follow from the stated steps
                  3. UNDERWEIGHTED RISKS — risk factors mentioned but not properly weighted
                  4. OVERLOOKED FACTORS — relevant factors not addressed at all
                  5. RULE MISAPPLICATION — thresholds or guidelines applied incorrectly

                Be specific: reference the exact step number where each error occurs.
                If the reasoning is correct, say "No errors found — reasoning is sound."
                """)
            .user("Reasoning to review:\n\n" + initialResponse)
            .call()
            .content();

        // Pass 3: Corrected response — apply critique and regenerate
        String correctedResponse = chatClient.prompt()
            .system("""
                You are a loan underwriting assistant for LoanIQ.
                You are given an initial reasoning trace and a quality-control critique.
                Apply ALL corrections identified in the critique and produce a corrected
                reasoning trace with the final decision.

                If the critique says "No errors found", reproduce the original decision.

                Format:
                CORRECTED REASONING:
                  Step 1 (Credit score):  [corrected analysis]
                  Step 2 (DTI):           [corrected analysis]
                  Step 3 (Payments):      [corrected analysis]
                  Step 4 (Employment):    [corrected analysis]
                CORRECTIONS APPLIED: [list what was changed from the original, or "None"]
                DECISION:   [Approve / Conditional Approval / Reject]
                CONFIDENCE: [High / Medium / Low]
                SUMMARY:    [one sentence for the applicant]
                """)
            .user("Original reasoning:\n" + initialResponse +
                  "\n\nQC critique and required corrections:\n" + critique +
                  "\n\nApplication:\n" + applicationSummary)
            .call()
            .content();

        // Return all 3 passes — full audit trail
        return "── PASS 1: INITIAL REASONING ──────────────────────────────\n" +
               initialResponse + "\n\n" +
               "── PASS 2: QC CRITIQUE ─────────────────────────────────────\n" +
               critique + "\n\n" +
               "── PASS 3: CORRECTED DECISION ──────────────────────────────\n" +
               correctedResponse;
    }
}
