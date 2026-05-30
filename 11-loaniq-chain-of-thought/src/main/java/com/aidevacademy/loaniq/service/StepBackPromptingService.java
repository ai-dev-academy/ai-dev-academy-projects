package com.aidevacademy.loaniq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ⑥: Step-Back Prompting
 *
 * PROBLEM: The LLM evaluates Application #4821 with the specific numbers in front of it.
 * It may over-anchor on the eye-catching facts (credit score 618, 3 missed payments)
 * and under-apply the nuanced guidelines (e.g. an improving payment trend after 24 months
 * is a compensating factor under standard lending guidelines).
 * The model "knows" these principles from training, but doesn't recall them
 * when distracted by specific application data.
 *
 * SOLUTION: Ask the LLM the GENERAL PRINCIPLE question first — before it sees any
 * application data. This "step back" retrieves accurate domain knowledge uncontaminated
 * by the specific numbers. Then feed that knowledge as ground truth into the second call.
 *
 * FLOW:
 *   Call 1 (no application data):
 *     "What are the key principles of responsible consumer loan underwriting?"
 *     → Returns: credit tier thresholds, DTI limits, compensating factor rules, etc.
 *
 *   Call 2 (application + principles):
 *     System: [principles as authoritative reference]
 *     User:   [evaluate Application #4821 using the above principles]
 *     → LLM applies correct rules to specific numbers, citing each principle by number
 *
 * THE IMPROVEMENT:
 *   Direct evaluation:  LLM may misremember DTI threshold (uses 50% instead of 43%)
 *   Step-Back:          Principles are stated explicitly before evaluation → correct thresholds used
 *
 * ENDPOINT: POST /underwriting/evaluate/step-back
 */
@Service
public class StepBackPromptingService {

    private final ChatClient chatClient;

    public StepBackPromptingService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String evaluate(String applicationSummary) {
        // Step 1: Retrieve general underwriting principles (no application data)
        String principles = chatClient.prompt()
            .system("You are an expert consumer lending consultant with 20 years of underwriting experience.")
            .user("""
                What are the key principles and decision criteria for responsible
                consumer loan underwriting? Provide a numbered reference guide covering:
                  1. Credit score tier thresholds, what each tier means, and historical default rates
                  2. Debt-to-income (DTI) ratio — what is acceptable, borderline, and disqualifying
                  3. How payment history should be interpreted — recency and trend weighting
                  4. Employment stability requirements and what constitutes sufficient tenure
                  5. Compensating factors — what can offset a negative indicator and by how much
                  6. When to approve conditionally vs. outright vs. reject

                Write this as a numbered reference guide, not as advice for a specific applicant.
                """)
            .call()
            .content();

        // Step 2: Apply the principles to the specific application
        return chatClient.prompt()
            .system("""
                You are a loan underwriting assistant for LoanIQ.
                Apply the lending principles below to evaluate the application.
                When making each reasoning step, CITE THE PRINCIPLE NUMBER that applies.
                Do NOT deviate from the stated principles — they are authoritative.

                ── LENDING PRINCIPLES ──────────────────────────────────────
                """ + principles + """
                ──────────────────────────────────────────────────────────────

                Format your response as:
                PRINCIPLE-BASED REASONING:
                  [Step 1: cite principle, apply to application data]
                  [Step 2: cite principle, apply to application data]
                  [continue for all relevant principles]

                DECISION:   [Approve / Conditional Approval / Reject]
                CONFIDENCE: [High / Medium / Low]
                SUMMARY:    [one sentence for the applicant]
                """)
            .user("Evaluate this application:\n\n" + applicationSummary)
            .call()
            .content();
    }
}
