package com.aidevacademy.supportiq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Technique ⑤: Parent-Document RAG
 *
 * Problem: Small retrieved chunks lack surrounding context — a code snippet retrieved
 * without its method signature, class declaration, or surrounding explanation is
 * incomplete and hard for the LLM to use correctly.
 *
 * Solution: Index small child chunks (150 tokens) for precise matching, but when
 * retrieved, return the full parent document (e.g. the entire class or page section).
 *
 * Flow:
 *   Ingest: Split into parent docs → store in parentStore
 *           Split each parent into child chunks → embed → store in vectorStore
 *                                                  (with parentId in metadata)
 *
 *   Query:  User Question
 *                │
 *                ▼
 *           [Search child chunks]  ← small, precise, 150-token slices
 *                │
 *                ▼
 *           [Extract parentId from each chunk's metadata]
 *                │
 *                ▼
 *           [Fetch full parent documents from parentStore]
 *                │
 *                ▼
 *           [LLM Answer with full context]
 *
 * Result: LLM has complete surrounding code and explanation, not just a snippet.
 * Limitation: Parent docs use many more tokens — higher cost per call.
 *
 * NOTE: Call registerParentDocument() at ingest time to populate the parent store.
 */
@Service
public class ParentDocumentRagService {

    private static final int CHILD_TOP_K = 10;

    private final ChatClient  chatClient;
    private final VectorStore vectorStore;   // stores child chunks

    /**
     * In-memory parent document store.
     * In production: use Redis, a PostgreSQL table, or a dedicated document DB.
     * Key   = parentId (must match "parentId" metadata on child chunks at ingest)
     * Value = full parent document text
     */
    private final Map<String, String> parentDocumentStore = new HashMap<>();

    public ParentDocumentRagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient  = chatClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        // 1. Search child chunks for precise semantic matching
        List<Document> childChunks = vectorStore.similaritySearch(
            SearchRequest.query(question).withTopK(CHILD_TOP_K)
        );

        // 2. Collect unique parent IDs from child chunk metadata
        Set<String> parentIds = childChunks.stream()
            .map(doc -> (String) doc.getMetadata().getOrDefault("parentId", doc.getId()))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // 3. Fetch full parent documents for rich context
        String contextText = parentIds.stream()
            .map(id -> parentDocumentStore.getOrDefault(id, ""))
            .filter(content -> !content.isBlank())
            .collect(Collectors.joining("\n\n===\n\n"));

        // Fallback: use child chunk text if parent store is empty
        if (contextText.isBlank()) {
            contextText = childChunks.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));
        }

        if (contextText.isBlank()) {
            return "No relevant documentation found. Please ingest documents first via POST /support/ingest";
        }

        String prompt = """
                You are SupportIQ. Answer using ONLY the context below.
                The context contains full document sections — use all the relevant details.

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
     * Register a parent document so child chunks can reference it.
     * Call this at ingest time before adding child chunks to the vector store.
     *
     * Example ingest flow:
     *   String parentId = UUID.randomUUID().toString();
     *   parentDocRagService.registerParentDocument(parentId, fullDocText);
     *   // Then split fullDocText into child chunks with metadata "parentId" = parentId
     *   // and add those chunks to the vector store
     */
    public void registerParentDocument(String parentId, String fullContent) {
        parentDocumentStore.put(parentId, fullContent);
    }
}
