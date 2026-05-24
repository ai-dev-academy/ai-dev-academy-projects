package com.aidevacademy.supportiq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Technique ②: Hybrid RAG (Dense + Sparse Search)
 *
 * Problem: Pure vector search misses exact technical terms like "NullPointerException"
 * or "OutOfMemoryError". These are rare words that don't embed well semantically.
 * Hybrid search combines semantic (dense) with keyword (sparse/BM25) to catch both.
 *
 * Flow:
 *   User Question
 *        │
 *        ├──► [Dense Embed]  ← pgvector cosine, top-5
 *        │
 *        └──► [Sparse BM25]  ← PostgreSQL full-text ts_vector, top-5
 *                  │
 *                  ▼
 *            [Merge + Deduplicate]  ← dense results take priority on tie
 *                  │
 *                  ▼
 *            [LLM Answer]
 *
 * Result: Catches both semantic matches AND exact technical term matches.
 * Limitation: Sparse search needs the exact term in the document — no synonyms.
 */
@Service
public class HybridRagService {

    private static final int DENSE_TOP_K  = 5;
    private static final int SPARSE_TOP_K = 5;

    private final ChatClient   chatClient;
    private final VectorStore  vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public HybridRagService(ChatClient chatClient, VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        this.chatClient   = chatClient;
        this.vectorStore  = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String ask(String question) {
        // 1. Dense search — semantic similarity via pgvector
        List<Document> denseResults = vectorStore.similaritySearch(
            SearchRequest.query(question).withTopK(DENSE_TOP_K)
        );

        // 2. Sparse search — keyword matching via PostgreSQL full-text search
        List<Document> sparseResults = keywordSearch(question, SPARSE_TOP_K);

        // 3. Merge results, deduplicate by document ID (dense takes priority)
        Map<String, Document> merged = new LinkedHashMap<>();
        denseResults.forEach(d  -> merged.put(d.getId(), d));
        sparseResults.forEach(d -> merged.putIfAbsent(d.getId(), d));

        if (merged.isEmpty()) {
            return "No relevant documentation found. Please ingest documents first via POST /support/ingest";
        }

        String contextText = merged.values().stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                You are SupportIQ. Answer using ONLY the provided context.
                Include specific class names, method signatures, and error types from the context.

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

    /**
     * Full-text keyword search using PostgreSQL ts_vector / ts_query.
     * The Spring AI pgvector table is named "vector_store" with a "content" column.
     */
    private List<Document> keywordSearch(String question, int topK) {
        // Build a tsquery from question words (OR logic for broader matching)
        String tsQuery = Arrays.stream(question.split("\\s+"))
            .map(w -> w.replaceAll("[^a-zA-Z0-9]", ""))
            .filter(w -> w.length() > 2)
            .collect(Collectors.joining(" | "));

        if (tsQuery.isBlank()) return List.of();

        try {
            return jdbcTemplate.query(
                "SELECT id, content FROM vector_store " +
                "WHERE to_tsvector('english', content) @@ to_tsquery('english', ?) LIMIT ?",
                (rs, rowNum) -> new Document(
                    rs.getString("id"),
                    rs.getString("content"),
                    Map.of()
                ),
                tsQuery, topK
            );
        } catch (Exception e) {
            // Graceful fallback — sparse search fails silently, dense result still used
            return List.of();
        }
    }
}
