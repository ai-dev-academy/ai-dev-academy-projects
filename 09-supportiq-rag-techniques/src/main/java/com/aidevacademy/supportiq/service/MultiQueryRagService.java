package com.aidevacademy.supportiq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Technique ③: Multi-Query RAG
 *
 * Problem: A single query phrasing may miss relevant documents because the user
 * asked "NullPointerException in LLM" but the doc says "null response handling".
 * Different phrasings retrieve different documents.
 *
 * Flow:
 *   User Question
 *        │
 *        ▼
 *   [LLM: Generate 4 query variations]
 *        │
 *        ├──► Search variation 1 → docs
 *        ├──► Search variation 2 → docs
 *        ├──► Search variation 3 → docs
 *        └──► Search variation 4 → docs
 *                  │
 *                  ▼
 *            [Union + Deduplicate by ID]
 *                  │
 *                  ▼
 *            [LLM Answer]
 *
 * Result: Much wider retrieval coverage — catches synonyms and related concepts.
 * Limitation: 5× more LLM calls for variation generation adds latency and cost.
 */
@Service
public class MultiQueryRagService {

    private static final int TOP_K_PER_QUERY  = 3;
    private static final int VARIATION_COUNT  = 4;

    private final ChatClient  chatClient;
    private final VectorStore vectorStore;

    public MultiQueryRagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient  = chatClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        // 1. Generate diverse query variations with the LLM
        List<String> queries = generateQueryVariations(question);

        // 2. Search with every variation, deduplicate by document ID
        Map<String, Document> uniqueDocs = new LinkedHashMap<>();
        for (String q : queries) {
            vectorStore.similaritySearch(SearchRequest.query(q).withTopK(TOP_K_PER_QUERY))
                .forEach(doc -> uniqueDocs.putIfAbsent(doc.getId(), doc));
        }

        if (uniqueDocs.isEmpty()) {
            return "No relevant documentation found. Please ingest documents first via POST /support/ingest";
        }

        String contextText = uniqueDocs.values().stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                You are SupportIQ. Answer using ONLY the context below.

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

    private List<String> generateQueryVariations(String question) {
        String variationPrompt = """
                Generate %d different phrasings of this question to improve document search coverage.
                Use technical Java/Spring Boot terminology in some variants, and plain English in others.
                Return ONLY the questions, one per line. No numbering, no bullet points.

                Original question: %s
                """.formatted(VARIATION_COUNT, question);

        String response = chatClient.prompt()
            .user(variationPrompt)
            .call()
            .content();

        List<String> variations = Arrays.stream(response.split("\n"))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .limit(VARIATION_COUNT)
            .collect(Collectors.toCollection(ArrayList::new));

        // Always include the original question
        variations.add(0, question);
        return variations;
    }
}
