package com.aidevacademy.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ObservabilityService {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityService.class);

    private final ChatClient chatClient;
    private final MeterRegistry meterRegistry;

    // Custom metrics
    private final Counter totalRequests;
    private final Counter successRequests;
    private final Counter failedRequests;

    public ObservabilityService(ChatClient chatClient, MeterRegistry meterRegistry) {
        this.chatClient   = chatClient;
        this.meterRegistry = meterRegistry;

        this.totalRequests   = Counter.builder("ai.requests.total")
                .description("Total AI requests").register(meterRegistry);
        this.successRequests = Counter.builder("ai.requests.success")
                .description("Successful AI requests").register(meterRegistry);
        this.failedRequests  = Counter.builder("ai.requests.failed")
                .description("Failed AI requests").register(meterRegistry);
    }

    public Map<String, Object> trackedChat(String message, String model) {
        totalRequests.increment();

        Timer timer = Timer.builder("ai.call.latency")
                .tag("model", model)
                .description("AI call latency")
                .register(meterRegistry);

        long startNano = System.nanoTime();
        try {
            String response = chatClient.prompt().user(message).call().content();
            long latencyMs  = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);

            timer.record(Duration.ofMillis(latencyMs));
            successRequests.increment();

            log.info("AI call OK | model={} latency={}ms msg_len={}", model, latencyMs, message.length());

            return Map.of(
                    "response",  response,
                    "latencyMs", latencyMs,
                    "model",     model,
                    "status",    "success"
            );
        } catch (Exception e) {
            long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
            timer.record(Duration.ofMillis(latencyMs));
            failedRequests.increment();
            log.error("AI call FAILED | model={} latency={}ms error={}", model, latencyMs, e.getMessage());
            throw e;
        }
    }

    public Map<String, Object> getMetricsSummary() {
        return Map.of(
                "totalRequests",   (long) totalRequests.count(),
                "successRequests", (long) successRequests.count(),
                "failedRequests",  (long) failedRequests.count(),
                "successRate",     totalRequests.count() > 0
                        ? Math.round(successRequests.count() / totalRequests.count() * 100) + "%" : "N/A",
                "prometheusUrl",   "http://localhost:8080/actuator/prometheus"
        );
    }
}
