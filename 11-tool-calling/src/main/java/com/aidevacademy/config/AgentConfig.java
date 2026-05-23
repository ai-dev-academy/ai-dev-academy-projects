package com.aidevacademy.config;

import com.aidevacademy.service.OrderAgent;
import com.aidevacademy.tools.OrderTools;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

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
    public OrderAgent orderAgent(OpenAiChatModel model, OrderTools tools) {
        return AiServices.builder(OrderAgent.class)
                .chatLanguageModel(model)
                .tools(tools)
                .build();
    }
}
