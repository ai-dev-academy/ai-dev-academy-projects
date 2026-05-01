package com.aideva.llmintro.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * ChatClient.Builder is auto-injected by Spring AI auto-configuration.
     * The defaultSystem message defines the AI persona for every request.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You are a helpful assistant.")
                .build();
    }
}
