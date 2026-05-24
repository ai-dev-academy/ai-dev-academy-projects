package com.aidevacademy.supportiq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Technique ④: HyDE (Hypothetical Document Embeddings) RAG
 *
 * Problem: Short, terse queries under-embed — "NullPointerException LLM fix" has
 * very different embedding density than the documentation that answers it.
 * HyDE bridges this gap by embedding a hypothetical answer instead of the question.
 *
 * Flow:
 *   User Question
 *        │
 *        ▼
 *   [LLM: Write a hypothetical answer]  ← "Imagine you're a senior dev writing the answer"
 *        │
 *        ▼
 *   [Embed the hypothesis]  ← now embedding matches doc vocabulary
 *        │
 *        ▼
 *   [Similarity Search with hypothesis embedding]
 *        │
 *        ▼
 *   [Retrieve real documents]
 *        │
 *        ▼
 *   [LLM: Answer from real context]
 *
 * Result: Much better retrieval for short or vague questions.
 * Limitation: If the hypothesis is wrong, retrieval goes in the wrong direction.
 */
@Service
public class HydeRagService {

    private static final int TOP_K = 5;

    private final ChatClient  chatClient;
    private final VectorStore vectorStore;

    public HydeRagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient  = chatClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        // 1. Generate a hypothetical answer to use as the search query
        String hypothesis = generateHypotheticalAnswer(question);

        // 2. Search using the hypothesis embedding (not the original question)
        List<Document> context = vectorStore.similaritySearch(
            SearchRequest.query(hypothesis).withTopK(TOP_K)
        );

        if (context.isEmpty()) {
            return "No relevant documentation found. Please ingest documents first via POST /support/ingest";
        }

        String contextText = context.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n---\n\n"));

        // 3. Answer with real retrieved context
        String prompt = """
                You are SupportIQ. Answer using ONLY the context below.
                Be precise about Java class names, Spring Boot APIs, and configuration.

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

    private String generateHypotheticalAnswer(String question) {
        String hydePrompt = """
                You are a senior Java/Spring Boot engineer.
                Write a concise technical paragraph that directly answers the question below.
                Use Java terminology, class names, and Spring Boot patterns.
                This paragraph will be used to search documentation — write it as if it were a doc page.

                Question: %s
                Technical answer:
                """.formatted(question);

        return chatClient.prompt()
            .user(hydePrompt)
            .call()
            .content();
    }
}
