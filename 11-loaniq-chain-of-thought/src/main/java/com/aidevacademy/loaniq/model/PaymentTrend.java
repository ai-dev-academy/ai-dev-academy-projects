package com.aidevacademy.loaniq.model;

/**
 * Payment history trend — key compensating factor for borderline credit scores.
 * An improving trend (missed 2yr ago, 18 on-time since) can offset a subprime score.
 */
public record PaymentTrend(
    String lastMissedPayment,       // "24 months ago"
    int    consecutiveOnTime,       // payments on time since last miss
    String trend,                   // "Improving" / "Stable" / "Declining"
    String underwritingNote         // how this should weight the decision
) {
    @Override
    public String toString() {
        return String.format(
            "PaymentTrend{lastMiss='%s', consecutiveOnTime=%d, trend='%s', note='%s'}",
            lastMissedPayment, consecutiveOnTime, trend, underwritingNote
        );
    }
}
