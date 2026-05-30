package com.aidevacademy.loaniq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ②: Few-Shot CoT
 *
 * PROBLEM: Zero-Shot CoT produces reasoning, but the FORMAT varies across calls.
 * One call returns "Approve | Low Risk | Rate: A". The next returns "Approved (conditionally)".
 * Downstream code that parses the DECISION field breaks because the LLM improvises format.
 * After 800 applications a day, you have 800 different formats to parse.
 *
 * SOLUTION: Provide 2 worked examples IN the prompt. The LLM learns the exact output
 * format — 4 labeled steps + DECISION | RISK | RATE_TIER — from the examples, and
 * reproduces it faithfully every single time.
 *
 * THE IMPROVEMENT over Zero-Shot CoT:
 *   Zero-Shot: format varies → parsing is brittle, DB inserts fail
 *   Few-Shot:  format is fixed → reliable machine-parseable output, every call
 *
 * WHEN TO USE: Any production pipeline that must parse the LLM's decision into a
 * database field, API response, or workflow trigger. The two examples are a one-time
 * investment that eliminates infinite format variation downstream.
 *
 * ENDPOINT: POST /underwriting/evaluate/few-shot
 */
@Service
public class FewShotCoTService {

    private final ChatClient chatClient;

    public FewShotCoTService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String evaluate(String applicationSummary) {
        // Build the few-shot prompt with 2 worked examples
        // These examples teach the LLM the exact 4-step + decision format
        String fewShotPrompt = """
            You are a loan underwriting assistant for LoanIQ.
            Follow this EXACT format — modeled on the two examples below.
            Do not add extra sections, headers, or explanations.

            ── EXAMPLE 1 (Approved) ──────────────────────────────────────────
            Application: Income $95K | Credit 740 | DTI 28% | Employment 5yr | Payments: Clean
            Step 1 (Credit):     740 = Prime tier (default rate 2.1%). No compensating factors needed. ✅
            Step 2 (DTI):        28% — well below 43% maximum, preferred 36% threshold. ✅
            Step 3 (Payments):   Clean 5-year history. No negative marks. ✅
            Step 4 (Employment): 5 years at same employer — stable income verified. ✅
            DECISION: Approve | RISK: Low | RATE_TIER: A

            ── EXAMPLE 2 (Rejected) ─────────────────────────────────────────
            Application: Income $41K | Credit 548 | DTI 62% | Employment 8mo | Payments: 5 misses/12mo
            Step 1 (Credit):     548 = Deep Subprime (default rate 24.7%). Multiple compensating factors required.
            Step 2 (DTI):        62% — exceeds 43% maximum. Hard limit breach. ❌
            Step 3 (Payments):   5 missed payments in the last 12 months — actively deteriorating. ❌
            Step 4 (Employment): 8 months — income stability unverified.
            DECISION: Reject | RISK: High | RATE_TIER: N/A

            ── NOW EVALUATE (use EXACTLY the same format) ───────────────────
            """ + applicationSummary;

        return chatClient.prompt()
            .system("You are a loan underwriting assistant for LoanIQ. " +
                    "Follow the exact format shown in the examples. Do not deviate.")
            .user(fewShotPrompt)
            .call()
            .content();
    }
}
