package com.aidevacademy.loaniq.service;

import com.aidevacademy.loaniq.model.CreditTier;
import com.aidevacademy.loaniq.model.DtiResult;
import com.aidevacademy.loaniq.model.PaymentTrend;
import com.aidevacademy.loaniq.tools.UnderwritingTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ⑦: CoT + Tool Calls (Grounded CoT)
 *
 * PROBLEM: All previous techniques reason from the numbers IN the application summary.
 * The summary says "$18K credit card debt" and "$72K annual income".
 * Zero-Shot CoT estimates DTI as: $18,000 / $72,000 = 25% — wrong formula, wrong answer.
 * The ACTUAL verified DTI (total monthly obligations / net monthly income) is 32.3%.
 *
 * This gap matters:
 *   - 25% DTI → "Well within 43% limit, approve without question"
 *   - 32.3% DTI → "Borderline — qualifies but needs compensating factors for this credit tier"
 *
 * On a $25K loan, that reasoning gap is a compliance liability and a credit risk difference.
 *
 * SOLUTION: At each reasoning STEP, the LLM calls a @Tool to VERIFY the number before
 * drawing a conclusion. It doesn't estimate DTI — it calls calculateDTI(4821) and gets 32.3%.
 * It doesn't guess the default rate for score 618 — it calls getCreditTier(618) and gets 12.4%.
 *
 * FLOW:
 *   Step 1 (Credit):  getCreditTier(618)       → Subprime, 12.4% default, compensating required
 *   Step 2 (DTI):     calculateDTI(4821)        → 32.3% (NOT the 25% estimate) — Borderline
 *   Step 3 (Payments): getPaymentTrend(4821)    → Improving, 18 consecutive on-time — compensating ✅
 *   Step 4 (Decision): All verified → Conditional Approval (compensating factor satisfies requirement)
 *
 * ENDPOINT: POST /underwriting/evaluate/grounded
 */
@Service
public class GroundedCoTService {

    private final ChatClient chatClient;
    private final UnderwritingTools tools;

    public GroundedCoTService(ChatClient chatClient, UnderwritingTools tools) {
        this.chatClient = chatClient;
        this.tools      = tools;
    }

    public String evaluate(String applicationSummary, long applicationId, int creditScore) {
        // Pre-fetch verified data via tools BEFORE building the reasoning prompt
        // This ensures the LLM reasons from facts, not estimates
        DtiResult    dti     = tools.calculateDTI(applicationId);
        CreditTier   tier    = tools.getCreditTier(creditScore);
        PaymentTrend trend   = tools.getPaymentTrend(applicationId);

        // Build grounded context — verified numbers replace estimated ones
        String groundedContext = String.format("""
            VERIFIED DATA (from database — use these numbers, not the application summary estimates):

            Credit Tier (score %d):
              - Tier: %s
              - Historical default rate: %.1f%%
              - Compensating factors required: %s
              - Compensating note: %s

            DTI (verified from DB):
              - Monthly debt obligations: $%.0f
              - Verified monthly income: $%.0f
              - DTI ratio: %.1f%%
              - DTI tier: %s
              NOTE: Application summary may show a different DTI estimate — use this verified value.

            Payment History:
              - Last missed payment: %s
              - Consecutive on-time payments since: %d
              - Trend: %s
              - Underwriting note: %s
            """,
            creditScore,
            tier.tier(), tier.historicalDefaultRate(),
            tier.requiresCompensating() ? "YES" : "No",
            tier.compensatingNote(),
            dti.monthlyDebtObligations(), dti.verifiedMonthlyIncome(),
            dti.dtiRatio() * 100, dti.dtiTier(),
            trend.lastMissedPayment(), trend.consecutiveOnTime(),
            trend.trend(), trend.underwritingNote()
        );

        return chatClient.prompt()
            .system("""
                You are a loan underwriting assistant for LoanIQ.
                You have access to VERIFIED DATA retrieved from the database.
                Use ONLY the verified data for your reasoning — do not re-estimate
                any number that appears in the verified data section.

                Think step by step. At each step, cite which verified data point
                you are applying and what conclusion it supports.

                Format:
                GROUNDED REASONING:
                  Step 1 (Credit tier): [cite tier + default rate → risk conclusion]
                  Step 2 (DTI):         [cite verified DTI% → capacity conclusion]
                  Step 3 (Payments):    [cite trend + consecutive count → behavior conclusion]
                  Step 4 (Employment):  [evaluate from application summary]
                  Step 5 (Synthesis):   [combine all 4 steps → final determination]

                DECISION:   [Approve / Conditional Approval / Reject]
                CONFIDENCE: [High / Medium / Low]
                CONDITIONS: [conditions for conditional approval, or "None"]
                SUMMARY:    [one sentence for the applicant]
                """)
            .user("VERIFIED DATA:\n" + groundedContext +
                  "\n\nAPPLICATION SUMMARY:\n" + applicationSummary)
            .call()
            .content();
    }
}
