package com.aidevacademy.opsdash.tools;

import com.aidevacademy.opsdash.model.Deploy;
import com.aidevacademy.opsdash.model.ServiceMetrics;
import com.aidevacademy.opsdash.model.SlowQuery;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OpsDash tool library — all @Tool-annotated methods the LLM can invoke.
 *
 * KEY LEARNING: The @Tool description string is what the LLM reads to decide
 * WHEN to call each tool. Write it like a function signature + docstring.
 * Vague descriptions lead to wrong tool selection; precise descriptions lead
 * to accurate, targeted calls.
 *
 * In production: replace stub return values with real Prometheus, CloudWatch,
 * GitHub API, and RDS Performance Insights calls.
 */
@Component
public class OpsDashTools {

    /**
     * Tool 1 of 4 — Performance metrics
     * The LLM calls this first when any latency or throughput question appears.
     */
    @Tool("Get live performance metrics for a microservice: p50 and p99 latency in ms, " +
          "error rate as a percentage, and requests per minute. " +
          "Call this for any service whose performance you need to verify.")
    public ServiceMetrics checkServiceMetrics(String serviceName) {
        // In production: prometheusClient.queryRange("histogram_quantile(0.99,...)", serviceName)
        return switch (serviceName.toLowerCase()) {
            case "checkout"  -> new ServiceMetrics("checkout",  850, 1240, 0.4,  1200);
            case "payment"   -> new ServiceMetrics("payment",   320,  680, 0.8,   800);
            case "inventory" -> new ServiceMetrics("inventory",  45,  120, 0.0,  3400);
            case "auth"      -> new ServiceMetrics("auth",       38,   95, 0.0,  8900);
            case "cart"      -> new ServiceMetrics("cart",       62,  180, 0.1,  4200);
            default          -> new ServiceMetrics(serviceName,  98,  210, 0.1,  2100);
        };
    }

    /**
     * Tool 2 of 4 — Deployment history
     * The LLM calls this to correlate latency spikes with recent deploys.
     */
    @Tool("List all deployments for a microservice in the last N hours, " +
          "including version, timestamp, commit hash, and who deployed it. " +
          "Use hoursBack=2 for recent incidents, hoursBack=24 for broader investigation.")
    public List<Deploy> getDeployHistory(String serviceName, int hoursBack) {
        // In production: githubActionsClient.listWorkflowRuns(serviceName, since)
        if ("checkout".equalsIgnoreCase(serviceName)) {
            return List.of(
                new Deploy("checkout", "v2.4.1", "2024-01-15T15:02:00Z", "abc123f", "alice@company.com"),
                new Deploy("checkout", "v2.4.0", "2024-01-14T09:15:00Z", "def456a", "bob@company.com")
            );
        }
        if ("payment".equalsIgnoreCase(serviceName)) {
            return List.of(
                new Deploy("payment", "v1.8.3", "2024-01-15T11:30:00Z", "ghi789b", "carol@company.com")
            );
        }
        return List.of();
    }

    /**
     * Tool 3 of 4 — Slow query log
     * The LLM calls this when checkServiceMetrics returns high latency, to find
     * whether the slowdown is in the application or in the database layer.
     */
    @Tool("Get the 10 slowest database queries for a service since a given ISO-8601 timestamp. " +
          "Returns each query's SQL, average duration in ms, and total call count. " +
          "Use sinceTimestamp in format: 2024-01-15T15:00:00Z")
    public List<SlowQuery> querySlowLogs(String serviceName, String sinceTimestamp) {
        // In production: rdsInsights.getTopSqlByAverageLatency(serviceName, since, 10)
        if ("checkout".equalsIgnoreCase(serviceName)) {
            return List.of(
                new SlowQuery(
                    "SELECT o.*, li.* FROM orders o " +
                    "JOIN line_items li ON li.order_id = o.id " +
                    "WHERE o.status = 'PENDING' ORDER BY o.created_at DESC",
                    820, 340
                ),
                new SlowQuery(
                    "UPDATE inventory SET quantity = quantity - ? " +
                    "WHERE product_id = ? AND warehouse_id = ?",
                    415, 1100
                )
            );
        }
        return List.of();
    }

    /**
     * Tool 4 of 4 — Error logs
     * The LLM calls this to read application-level error messages and exceptions.
     */
    @Tool("Fetch application error and warning logs for a microservice in a time range. " +
          "Returns raw log lines including timestamps, log levels, and messages. " +
          "Parameters: serviceName, sinceTimestamp (ISO-8601), level (ERROR or WARN).")
    public String getErrorLogs(String serviceName, String sinceTimestamp, String level) {
        // In production: cloudwatchClient.filterLogEvents(logGroupName, startTime, filterPattern)
        if ("checkout".equalsIgnoreCase(serviceName)) {
            return """
                2024-01-15T15:05:12Z ERROR CheckoutService - HikariPool-1 connection timeout after 30s
                2024-01-15T15:05:44Z ERROR CheckoutService - Connection pool exhausted: active=50/50, waiting=23
                2024-01-15T15:05:45Z ERROR CheckoutService - Unable to acquire JDBC connection
                2024-01-15T15:06:01Z WARN  CheckoutService - Retry 1/3 for order #89234 (OrderProcessingException)
                2024-01-15T15:06:10Z ERROR CheckoutService - Transaction rollback: order #89234 failed
                """;
        }
        return "No " + level + " logs found for service: " + serviceName + " since " + sinceTimestamp;
    }
}
