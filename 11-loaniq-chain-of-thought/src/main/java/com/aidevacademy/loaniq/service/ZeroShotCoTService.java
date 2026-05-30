package com.aidevacademy.loaniq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ①: Zero-Shot CoT
 *
 * PROBLEM: Without any prompting strategy, the LLM gives a one-line decision
 * with no reasoning trace. This fails compliance — regulators require a documented
 * rationale for every approval or rejection. The answer also fluctuates: the same
 * application can get "Approve" on one call and "Conditional" on the next.
 *
 * SOLUTION: Add exactly four words to the system message: "THINK STEP BY STEP."
 * This single instruction triggers the LLM to reason through DTI, credit, payment
 * history, and employment in order — before giving a decision.
 *
 * THE IMPROVEMENT:
 *   Without CoT:   "Conditional approval based on borderline credit."
 *   With Zero-Shot CoT: A 4-step reasoning trace covering every risk factor,
 *                       a decision, a one-line reason, and an audit-ready log entry.
 *
 * LIMITATION: The LLM ESTIMATES DTI from the application summary (~9% — wrong).
 * The actual verified DTI is 32.3%. See Technique ⑦ for the grounded version.
 *
 * ENDPOINT: POST /underwriting/evaluate/zero-shot
 */
@Service
public class ZeroShotCoTService {

    private final ChatClient chatClient;

    public ZeroShotCoTService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String evaluate(String applicationSummary) {
        return chatClient.prompt()
            .system("""
                You are a loan underwriting assistant for LoanIQ.
                THINK STEP BY STEP before giving your decision.
                Write each reasoning step explicitly — do not skip steps.

                Format your response EXACTLY as:
                REASONING:
                Step 1 (DTI):        [analyze debt-to-income ratio]
                Step 2 (Credit):     [analyze credit score and tier]
                Step 3 (Payments):   [analyze missed payment history]
                Step 4 (Employment): [analyze employment stability]

                DECISION:   [Approve / Conditional Approval / Reject]
                REASON:     [one sentence — the key factor that determined the decision]
                CONFIDENCE: [High / Medium / Low]
                """)
            .user(applicationSummary)
            .call()
            .content();
    }
}
