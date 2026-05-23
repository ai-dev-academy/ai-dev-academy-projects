package com.aidevacademy.config;

import com.aidevacademy.service.CustomerSupport;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiServicesConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiKey;

    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiKey)
                .modelName("gpt-4o-mini")
                .build();
    }

    @Bean
    public CustomerSupport customerSupport(OpenAiChatModel model) {
        return AiServices.builder(CustomerSupport.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }
}
