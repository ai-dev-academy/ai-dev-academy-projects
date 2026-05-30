package com.aidevacademy.loaniq.model;

/**
 * Credit tier data from the underwriting reference table.
 * Includes historical default rates and whether compensating factors are required.
 */
public record CreditTier(
    int    score,
    String tier,              // "Prime" / "Near-Prime" / "Subprime" / "Deep Subprime"
    double historicalDefaultRate,
    boolean requiresCompensating,
    String  compensatingNote  // what factors can offset this tier
) {
    @Override
    public String toString() {
        return String.format(
            "CreditTier{score=%d, tier='%s', defaultRate=%.1f%%, requiresCompensating=%b, note='%s'}",
            score, tier, historicalDefaultRate, requiresCompensating, compensatingNote
        );
    }
}
