package com.aideva.deepdive.config;

import com.aideva.deepdive.advisor.LoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public InMemoryChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    /**
     * ChatClient with:
     *  - Logging advisor  (logs every prompt/response)
     *  - Memory advisor   (maintains conversation history per session)
     *  - SimpleLogger     (Spring AI built-in debug logger)
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 InMemoryChatMemory chatMemory,
                                 LoggingAdvisor loggingAdvisor) {
        return builder
                .defaultSystem("You are a helpful Java and AI engineering assistant.")
                .defaultAdvisors(
                        loggingAdvisor,
                        new MessageChatMemoryAdvisor(chatMemory),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
}
