package com.aidevacademy.loaniq.tools;

import com.aidevacademy.loaniq.model.CreditTier;
import com.aidevacademy.loaniq.model.DtiResult;
import com.aidevacademy.loaniq.model.PaymentTrend;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * LoanIQ underwriting tool library — @Tool-annotated methods for verified data retrieval.
 *
 * KEY LESSON (Technique ⑦ — CoT + Tool Calls):
 *   Zero-Shot CoT ESTIMATED the DTI for Application #4821 as ~9%
 *   (it divided $18K credit card debt by $72K annual income).
 *
 *   This tool returns the VERIFIED DTI: 32.3%
 *   (total monthly obligations including car loan, student loan, and minimum payments
 *   divided by verified net monthly income from payroll records).
 *
 *   That 23-point gap is the exact reason grounded CoT exists.
 *   Unverified reasoning on financial data is a compliance liability.
 *
 * In production: replace stubs with real database queries to your
 * applications DB, credit bureau API, and payment history table.
 */
@Component
public class UnderwritingTools {

    /**
     * The most important tool — computes DTI from VERIFIED records.
     *
     * Zero-Shot CoT will estimate this from salary alone and get it wrong.
     * This tool queries the actual monthly obligations from the DB.
     */
    @Tool("Calculate the exact debt-to-income ratio for a loan application " +
          "using verified monthly obligations from the database and payroll records. " +
          "Returns DTI ratio, monthly debt amount, verified income, and DTI tier. " +
          "Use this instead of estimating DTI from the application summary.")
    public DtiResult calculateDTI(long applicationId) {
        // In production: applicationRepo.findById(applicationId).getTotalMonthlyObligations()
        if (applicationId == 4821L) {
            // Breakdown: $500 car, $250 student loan, $195 credit card minimum, $250 new loan payment
            double monthlyDebt   = 1940.0;
            double monthlyIncome = 6000.0;  // verified net (not gross)
            double dti = monthlyDebt / monthlyIncome;  // 0.323 = 32.3%
            return new DtiResult(monthlyDebt, monthlyIncome, dti, "Borderline");
            // Zero-Shot estimated ~9% (18000/72000/12 — wrong formula, wrong denominator)
            // Actual verified: 32.3% — still under 43% limit but changes the risk profile significantly
        }
        // Default: healthy application
        return new DtiResult(1200.0, 5500.0, 0.218, "Acceptable");
    }

    /**
     * Credit tier lookup — returns the tier, historical default rate, and
     * whether compensating factors are REQUIRED to approve at this score.
     */
    @Tool("Look up the credit score tier, historical default rate, and compensating factor " +
          "requirements for a given credit score. " +
          "Returns tier name (Prime/Near-Prime/Subprime/Deep Subprime), " +
          "the historical default rate for this tier, and what compensating factors are acceptable.")
    public CreditTier getCreditTier(int score) {
        // In production: creditTierTable.lookup(score)
        if (score >= 720) {
            return new CreditTier(score, "Prime", 2.1, false,
                "No compensating factors required");
        } else if (score >= 660) {
            return new CreditTier(score, "Near-Prime", 5.8, false,
                "Compensating factors strengthen approval but not required");
        } else if (score >= 580) {
            return new CreditTier(score, "Subprime", 12.4, true,
                "REQUIRED: stable employment 2+ years OR improving payment trend OR DTI below 36%");
        } else {
            return new CreditTier(score, "Deep Subprime", 24.7, true,
                "REQUIRED: multiple strong compensating factors; conditional approval only");
        }
    }

    /**
     * Payment history trend — the most underweighted compensating factor.
     * 18 consecutive on-time payments after a miss carries significant weight
     * in FHA and conventional underwriting guidelines.
     */
    @Tool("Get the payment history trend for a loan application: " +
          "when the last missed payment occurred, how many consecutive on-time payments " +
          "have been made since, and the overall trend direction. " +
          "Also returns an underwriting note on how this should weight the decision.")
    public PaymentTrend getPaymentTrend(long applicationId) {
        // In production: paymentHistoryRepo.getTrend(applicationId)
        if (applicationId == 4821L) {
            return new PaymentTrend(
                "24 months ago",
                18,
                "Improving",
                "18 consecutive on-time payments since last miss. " +
                "Per guideline §4.3: improving trend with 12+ consecutive on-time payments " +
                "qualifies as a compensating factor for subprime scores. " +
                "Weight: POSITIVE — offsets subprime tier requirement."
            );
        }
        return new PaymentTrend("36 months ago", 24, "Stable",
            "Clean recent history. No negative trend.");
    }
}
