package com.aidevacademy.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You are a helpful Java and Spring Boot assistant. Give concise, practical answers with code examples. Keep responses under 300 words unless asked for more.")
                .build();
    }
}
