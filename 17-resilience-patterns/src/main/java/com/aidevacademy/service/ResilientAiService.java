package com.aidevacademy.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResilientAiService {

    private static final Logger log = LoggerFactory.getLogger(ResilientAiService.class);

    private final ChatClient chatClient;

    // Simple semantic cache: exact-match for demo (use Redis + embeddings in production)
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private int cacheHits = 0;
    private int cacheMisses = 0;

    public ResilientAiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Resilient chat with retry + circuit breaker + cache
     */
    @CircuitBreaker(name = "ai", fallbackMethod = "fallback")
    @Retry(name = "ai")
    public String chat(String message) {
        // Check cache first
        String cacheKey = message.toLowerCase().trim();
        if (cache.containsKey(cacheKey)) {
            cacheHits++;
            log.info("Cache HIT for: {}", message.substring(0, Math.min(30, message.length())));
            return "[CACHED] " + cache.get(cacheKey);
        }

        cacheMisses++;
        log.info("Cache MISS — calling AI for: {}", message.substring(0, Math.min(30, message.length())));
        String response = chatClient.prompt().user(message).call().content();
        cache.put(cacheKey, response);
        return response;
    }

    /**
     * Fallback method called when circuit breaker opens or all retries fail
     */
    public String fallback(String message, Exception e) {
        log.warn("Fallback activated for: {} — reason: {}", message, e.getMessage());
        return "The AI service is temporarily unavailable. " +
               "Your question was: \"" + message + "\". Please try again in a moment.";
    }

    public Map<String, Object> getCacheStats() {
        return Map.of(
                "hits",      cacheHits,
                "misses",    cacheMisses,
                "cacheSize", cache.size(),
                "hitRate",   cacheHits + cacheMisses > 0
                        ? Math.round((double) cacheHits / (cacheHits + cacheMisses) * 100) + "%"
                        : "0%"
        );
    }

    public void clearCache() {
        cache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }
}
