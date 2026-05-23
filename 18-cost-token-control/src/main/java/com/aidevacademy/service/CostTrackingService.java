package com.aidevacademy.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CostTrackingService {

    private static final Logger log = LoggerFactory.getLogger(CostTrackingService.class);

    // Claude Haiku pricing (per million tokens)
    private static final double INPUT_COST_PER_TOKEN  = 1.00 / 1_000_000;
    private static final double OUTPUT_COST_PER_TOKEN = 5.00 / 1_000_000;

    private final ChatClient chatClient;
    private final MeterRegistry meterRegistry;

    // Cost tracking per endpoint
    private final Map<String, Double>     endpointCost     = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> endpointRequests = new ConcurrentHashMap<>();
    private double totalCostUsd = 0.0;

    public CostTrackingService(ChatClient chatClient, MeterRegistry meterRegistry) {
        this.chatClient  = chatClient;
        this.meterRegistry = meterRegistry;
    }

    public Map<String, Object> chatWithCostTracking(String message, String endpoint) {
        long start = System.currentTimeMillis();

        // Call AI and get full response with usage metadata
        ChatResponse response = chatClient.prompt()
                .user(message)
                .call()
                .chatResponse();

        String content    = response.getResult().getOutput().getContent();
        Usage  usage      = response.getMetadata().getUsage();
        long   inputTokens  = usage != null ? usage.getPromptTokens()     : estimateTokens(message);
        long   outputTokens = usage != null ? usage.getGenerationTokens() : estimateTokens(content);

        double callCost = (inputTokens * INPUT_COST_PER_TOKEN) + (outputTokens * OUTPUT_COST_PER_TOKEN);
        totalCostUsd   += callCost;

        // Update per-endpoint tracking
        endpointCost.merge(endpoint, callCost, Double::sum);
        endpointRequests.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();

        // Record Prometheus metrics
        Counter.builder("ai.cost.usd")
                .tag("endpoint", endpoint)
                .register(meterRegistry)
                .increment(callCost);
        Counter.builder("ai.tokens.total")
                .tag("type", "input")
                .register(meterRegistry)
                .increment(inputTokens);
        Counter.builder("ai.tokens.total")
                .tag("type", "output")
                .register(meterRegistry)
                .increment(outputTokens);

        log.info("Cost: ${} | Tokens: {}in/{}out | Endpoint: {}",
                String.format("%.6f", callCost), inputTokens, outputTokens, endpoint);

        return Map.of(
                "response",      content,
                "inputTokens",   inputTokens,
                "outputTokens",  outputTokens,
                "costUsd",       String.format("$%.6f", callCost),
                "latencyMs",     System.currentTimeMillis() - start
        );
    }

    private long estimateTokens(String text) {
        return text != null ? text.length() / 4 : 0;
    }

    public Map<String, Object> getCostReport() {
        return Map.of(
                "totalCostUsd",      String.format("$%.4f", totalCostUsd),
                "costByEndpoint",    endpointCost.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                e -> String.format("$%.6f", e.getValue()))),
                "requestsByEndpoint", endpointRequests.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().get()))
        );
    }

    public void resetCosts() {
        endpointCost.clear();
        endpointRequests.clear();
        totalCostUsd = 0.0;
    }
}
