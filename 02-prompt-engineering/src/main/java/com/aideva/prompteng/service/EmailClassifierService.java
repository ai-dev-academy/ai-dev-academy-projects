package com.aideva.prompteng.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailClassifierService {

    private static final String SYSTEM_PROMPT = """
            Classify the email into EXACTLY one category:
            BUG | FEATURE | BILLING | GENERAL

            Rules:
            - Reply with ONLY the category word — no punctuation, no explanation
            - If unsure, use GENERAL

            Examples:
            Email: "The login button is broken"
            Category: BUG

            Email: "Please add dark mode"
            Category: FEATURE

            Email: "I was charged twice this month"
            Category: BILLING

            Email: "How do I reset my password?"
            Category: GENERAL
            """;

    private static final String USER_TEMPLATE = "Classify this {type} message:\n{content}";

    private final ChatClient chatClient;

    public EmailClassifierService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    /**
     * Classifies an email into one of: BUG, FEATURE, BILLING, GENERAL.
     */
    public String classify(String emailBody) {
        var template = new PromptTemplate(USER_TEMPLATE);
        var prompt = template.create(Map.of("type", "support email", "content", emailBody));

        String raw = chatClient.prompt(prompt).call().content();
        String result = raw == null ? "" : raw.trim().toUpperCase();

        // Defensive: return GENERAL if LLM ignores instructions
        return java.util.Set.of("BUG", "FEATURE", "BILLING", "GENERAL").contains(result)
                ? result : "GENERAL";
    }
}
