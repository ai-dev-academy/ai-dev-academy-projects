package com.aidevacademy.loaniq.tools;

import com.aidevacademy.loaniq.model.LoanApplication;

/**
 * Sample loan application data for the LoanIQ demo.
 *
 * Application #4821 — the borderline case used throughout all 8 techniques.
 * You can swap this with your own application data by changing the fields below.
 */
public class SampleApplications {

    /**
     * The canonical LoanIQ scenario application.
     * Run this through all 8 CoT techniques to see how the technique affects
     * the reasoning quality, consistency, and defensibility of the decision.
     */
    public static LoanApplication app4821() {
        return new LoanApplication(
            4821L,
            72_000.0,   // $72K annual income
            618,        // credit score — subprime tier (580–659)
            3,          // 3 missed payments in history
            "24 months ago",
            18_000.0,   // $18K credit card debt outstanding
            2,          // 2 years at current employer
            25_000.0    // $25K personal loan requested
        );
    }

    /**
     * A clearly approvable application — use to verify techniques give consistent "Approve".
     */
    public static LoanApplication appClearApprove() {
        return new LoanApplication(
            9001L, 95_000.0, 740, 0, "N/A", 5_000.0, 5, 20_000.0
        );
    }

    /**
     * A clearly rejectable application — use to verify techniques give consistent "Reject".
     */
    public static LoanApplication appClearReject() {
        return new LoanApplication(
            9002L, 41_000.0, 548, 6, "4 months ago", 32_000.0, 1, 30_000.0
        );
    }
}
