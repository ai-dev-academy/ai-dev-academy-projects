package com.aidevacademy.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ClassifierService {

    private final ChatClient chatClient;

    public ClassifierService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String classify(String emailText) {
        String systemPrompt = """
                You are an email classification system for a software company's support team.
                
                Classify the email into EXACTLY ONE of these categories:
                BUG       - Something is broken or not working correctly
                FEATURE   - A request for new functionality
                BILLING   - Questions or issues about payments, invoices, subscriptions
                GENERAL   - Everything else (questions, feedback, compliments)
                
                Rules:
                - Reply with ONLY the category word in ALL CAPS
                - No punctuation, no explanation, no extra text
                - If ambiguous, choose the most likely category
                
                Examples:
                Email: "The login button crashes the app" → BUG
                Email: "Please add dark mode" → FEATURE
                Email: "I was charged twice this month" → BILLING
                Email: "How do I reset my password?" → GENERAL
                """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user("Classify this email: " + emailText)
                .call()
                .content()
                .trim()
                .toUpperCase();
    }

    public String classifyWithCategories(String text, String categories) {
        String systemPrompt = """
                Classify the text into EXACTLY ONE of these categories: %s
                Reply with ONLY the category name. No explanation.
                """.formatted(categories);

        return chatClient.prompt()
                .system(systemPrompt)
                .user(text)
                .call()
                .content()
                .trim();
    }
}
