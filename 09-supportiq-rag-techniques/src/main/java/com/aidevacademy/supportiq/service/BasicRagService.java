package com.aidevacademy.supportiq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Technique ①: Basic RAG
 *
 * Problem: Without RAG, the LLM answers from training data only — it hallucinates
 * specific APIs, configs, and version details that differ in your codebase.
 *
 * Flow:
 *   User Question
 *        │
 *        ▼
 *   [Embed Question]  ← text-embedding-3-small
 *        │
 *        ▼
 *   [Similarity Search]  ← pgvector cosine distance, top-5
 *        │
 *        ▼
 *   [Build Prompt]  ← inject retrieved chunks as context
 *        │
 *        ▼
 *   [LLM Answer]  ← gpt-4o-mini
 *
 * Result: Grounded answer with specific class names and methods from your docs.
 * Limitation: Misses docs using different words for the same concept.
 */
@Service
public class BasicRagService {

    private static final int TOP_K = 5;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public BasicRagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient  = chatClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        // 1. Embed the question and retrieve top-K similar chunks
        List<Document> context = vectorStore.similaritySearch(
            SearchRequest.query(question).withTopK(TOP_K)
        );

        if (context.isEmpty()) {
            return "No relevant documentation found. Please ingest documents first via POST /support/ingest";
        }

        // 2. Concatenate retrieved chunks as context
        String contextText = context.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n---\n\n"));

        // 3. Prompt the LLM with grounded context
        String prompt = """
                You are SupportIQ, an AI assistant for a Java/Spring Boot SaaS platform.
                Answer using ONLY the context provided below.
                If the answer is not in the context, say "I don't have that information in the knowledge base."
                Be specific — reference class names, method signatures, and error types from the context.

                Context:
                %s

                Question: %s
                Answer:
                """.formatted(contextText, question);

        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }
}
