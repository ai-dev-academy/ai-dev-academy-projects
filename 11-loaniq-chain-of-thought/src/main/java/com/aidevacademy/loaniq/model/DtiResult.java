package com.aidevacademy.loaniq.model;

/**
 * The verified DTI ratio calculated from database records.
 * KEY LESSON: The LLM estimated 9% DTI from the salary alone — this tool returns
 * the VERIFIED 32.3% that includes all monthly obligations. That 23-point gap
 * is exactly why grounded CoT (Technique ⑦) outperforms Zero-Shot.
 */
public record DtiResult(
    double monthlyDebtObligations,   // verified from credit bureau + DB
    double verifiedMonthlyIncome,    // verified from payroll records
    double dtiRatio,                 // monthlyDebt / monthlyIncome
    String dtiTier                   // "Acceptable" / "Borderline" / "Exceeds Limit"
) {
    @Override
    public String toString() {
        return String.format(
            "DTI{monthly_debt=$%.0f, monthly_income=$%.0f, dti=%.1f%%, tier='%s'}",
            monthlyDebtObligations, verifiedMonthlyIncome, dtiRatio * 100, dtiTier
        );
    }
}
