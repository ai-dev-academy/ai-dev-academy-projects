package com.aidevacademy.opsdash.model;

/**
 * A slow database query captured from RDS Performance Insights or CloudWatch.
 */
public record SlowQuery(
    String sql,
    long avgDurationMs,
    int callCount
) {
    @Override
    public String toString() {
        return String.format(
            "SlowQuery{avgDuration=%dms, callCount=%d, sql='%s'}",
            avgDurationMs, callCount, sql.length() > 80 ? sql.substring(0, 80) + "..." : sql
        );
    }
}
