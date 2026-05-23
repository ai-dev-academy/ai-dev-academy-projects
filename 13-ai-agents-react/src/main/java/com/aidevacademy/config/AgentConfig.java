package com.aidevacademy.config;

import com.aidevacademy.service.ResearchAgent;
import com.aidevacademy.tools.ResearchTools;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Bean
    public OpenAiChatModel model() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .maxRetries(3)
                .build();
    }

    @Bean
    public ResearchAgent researchAgent(OpenAiChatModel model, ResearchTools tools) {
        return AiServices.builder(ResearchAgent.class)
                .chatLanguageModel(model)
                .tools(tools)
                .maxSequentialToolsInvocations(10) // safety limit
                .build();
    }
}
