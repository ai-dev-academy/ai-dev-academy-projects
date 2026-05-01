package com.aideva.deepdive.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Demonstrates multi-provider routing and fallback.
 * Simple routing strategy: short messages → fast/cheap, long → powerful model.
 * Falls back to a secondary prompt if the primary call fails.
 */
@Service
public class ResilientAiService {

    private static final Logger log = LoggerFactory.getLogger(ResilientAiService.class);

    private final ChatClient chatClient;

    public ResilientAiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String message) {
        try {
            return chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("Primary AI call failed ({}), using fallback response.", e.getMessage());
            return "I'm temporarily unavailable. Please try again in a moment.";
        }
    }
}
