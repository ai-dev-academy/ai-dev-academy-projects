package com.aidevacademy.loaniq.model;

/**
 * A loan application submitted to LoanIQ for underwriting.
 * In production: loaded from the applications database.
 */
public record LoanApplication(
    long   applicationId,
    double annualIncomeDollars,
    int    creditScore,
    int    missedPaymentsCount,
    String lastMissedPayment,      // e.g. "24 months ago"
    double totalCreditCardDebt,
    int    employmentYears,
    double requestedLoanAmount
) {
    /** Formatted summary for LLM prompts */
    public String toPromptSummary() {
        return String.format("""
            Application ID:     #%d
            Annual Income:      $%,.0f  (monthly: $%,.0f)
            Credit Score:       %d
            Missed Payments:    %d  (last: %s)
            Credit Card Debt:   $%,.0f
            Employment:         %d years at current employer
            Requested Amount:   $%,.0f
            """,
            applicationId,
            annualIncomeDollars, annualIncomeDollars / 12,
            creditScore,
            missedPaymentsCount, lastMissedPayment,
            totalCreditCardDebt,
            employmentYears,
            requestedLoanAmount
        );
    }
}
