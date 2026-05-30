package com.aidevacademy.opsdash.model;

/**
 * Live performance metrics for a microservice.
 * In production: populated from Prometheus or Datadog.
 */
public record ServiceMetrics(
    String service,
    int p50Ms,
    int p99Ms,
    double errorRate,
    int throughputRpm
) {
    @Override
    public String toString() {
        return String.format(
            "ServiceMetrics{service='%s', p50=%dms, p99=%dms, errorRate=%.2f%%, throughput=%d rpm}",
            service, p50Ms, p99Ms, errorRate, throughputRpm
        );
    }
}
