package com.aidevacademy.supportiq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Technique ⑥: Contextual Compression RAG
 *
 * Problem: Retrieved documents contain noise — tables of contents, unrelated
 * paragraphs, boilerplate code comments. Sending the full doc to the LLM wastes
 * tokens, dilutes the answer, and increases cost.
 *
 * Solution: For each retrieved chunk, ask a fast LLM call to extract only the
 * sentences directly relevant to the question. Discard irrelevant chunks entirely.
 *
 * Flow:
 *   User Question
 *        │
 *        ▼
 *   [Similarity Search]  ← retrieve 8 docs (more than usual)
 *        │
 *        ▼
 *   [Compress each doc]  ← LLM: "extract only relevant sentences"
 *        │                  returns "NOT_RELEVANT" if nothing matches
 *        ▼
 *   [Filter: drop NOT_RELEVANT chunks]
 *        │
 *        ▼
 *   [LLM Answer]  ← leaner, signal-dense context
 *
 * Result: Answers are more precise — no noise diluting the context window.
 * Limitation: Compression adds N extra LLM calls (one per retrieved doc).
 */
@Service
public class ContextualCompressionService {

    private static final int TOP_K = 8;  // retrieve more since some will be filtered out

    private final ChatClient  chatClient;
    private final VectorStore vectorStore;

    public ContextualCompressionService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient  = chatClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        // 1. Retrieve more docs than usual (compression will discard irrelevant ones)
        List<Document> rawDocs = vectorStore.similaritySearch(
            SearchRequest.query(question).withTopK(TOP_K)
        );

        // 2. Compress each document — keep only relevant sentences
        List<String> compressedChunks = rawDocs.stream()
            .map(doc -> compress(doc.getContent(), question))
            .filter(Objects::nonNull)
            .filter(s -> !s.isBlank() && !s.equalsIgnoreCase("NOT_RELEVANT"))
            .collect(Collectors.toList());

        if (compressedChunks.isEmpty()) {
            return "No relevant information found for your question in the knowledge base.";
        }

        String contextText = String.join("\n\n---\n\n", compressedChunks);

        String prompt = """
                You are SupportIQ. Answer using ONLY the compressed context below.
                Each section has been pre-filtered to contain only sentences relevant to the question.

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
     * Extracts only the sentences from a document chunk that are relevant to the question.
     * Returns "NOT_RELEVANT" if nothing in the chunk relates to the question.
     */
    private String compress(String documentContent, String question) {
        String compressionPrompt = """
                Extract ONLY the sentences from the document below that directly help answer the question.
                If none of the sentences are relevant, return exactly: NOT_RELEVANT
                Do not add any explanation — return only the extracted sentences.

                Question: %s

                Document:
                %s

                Relevant sentences:
                """.formatted(question, documentContent);

        try {
            return chatClient.prompt()
                .user(compressionPrompt)
                .call()
                .content();
        } catch (Exception e) {
            return documentContent;  // fallback: use uncompressed content
        }
    }
}
