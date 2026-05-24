package com.aidevacademy.supportiq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Technique ⑧: Agentic RAG
 *
 * Problem: One retrieval step can't handle multi-hop questions or questions that
 * span multiple document types (e.g. "What changed in NullPointerException handling
 * between v2 and v3, and how do I migrate?"). A single search can't get there.
 *
 * Solution: The LLM drives the retrieval loop. After each search it evaluates
 * whether it has enough information to answer — if not, it decides what to search
 * for next. Loop continues until SUFFICIENT or maxIterations reached.
 *
 * Flow:
 *   User Question
 *        │
 *        ▼ (iteration 1)
 *   [Search]  ← LLM-chosen or original query
 *        │
 *        ▼
 *   [Evaluate: do I have enough context?]
 *        ├── SUFFICIENT → break → [Synthesize Answer]
 *        └── NEED_MORE  → extract next search query → loop (max 3 iterations)
 *
 * Result: Handles complex multi-hop questions by building context across searches.
 * Limitation: Up to 3× retrieval + 3× evaluation calls — highest latency and cost.
 *             Always set a maxIterations guard to prevent infinite loops.
 */
@Service
public class AgenticRagService {

    private static final int MAX_ITERATIONS = 3;
    private static final int TOP_K          = 5;

    private final ChatClient  chatClient;
    private final VectorStore vectorStore;

    public AgenticRagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient  = chatClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        List<Document> allCollected = new ArrayList<>();
        String currentQuery = question;

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Step 1: Search with the current query
            List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(currentQuery).withTopK(TOP_K)
            );

            // Add new (non-duplicate) documents to the collection
            for (Document doc : results) {
                boolean alreadyCollected = allCollected.stream()
                    .anyMatch(d -> d.getId().equals(doc.getId()));
                if (!alreadyCollected) {
                    allCollected.add(doc);
                }
            }

            String contextSoFar = allCollected.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

            // Step 2: Ask the LLM if we have sufficient context to answer
            String evaluation = evaluateSufficiency(question, contextSoFar, iteration + 1);

            if (evaluation.startsWith("SUFFICIENT")) {
                break;
            }

            // Step 3: Extract the LLM's suggested next search query
            String nextQuery = extractNextQuery(evaluation, question);
            if (nextQuery.equalsIgnoreCase(currentQuery)) {
                break;  // avoid looping on the same query
            }
            currentQuery = nextQuery;
        }

        if (allCollected.isEmpty()) {
            return "No relevant documentation found. Please ingest documents first via POST /support/ingest";
        }

        String finalContext = allCollected.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                You are SupportIQ. Synthesize a complete, accurate answer from the context below.
                Context was gathered across multiple targeted searches — use all relevant information.

                Context:
                %s

                Question: %s
                Answer:
                """.formatted(finalContext, question);

        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }

    /**
     * Asks the LLM to evaluate whether the accumulated context is sufficient to answer.
     * Returns either "SUFFICIENT" or "NEED_MORE: <next search query>".
     */
    private String evaluateSufficiency(String question, String context, int iteration) {
        // Truncate context preview to avoid huge evaluation prompts
        String contextPreview = context.length() > 3000
            ? context.substring(0, 3000) + "\n... [truncated]"
            : context;

        String evalPrompt = """
                You are evaluating whether you have enough context to fully answer a question.

                RESPOND WITH ONE OF THESE EXACT FORMATS:
                - "SUFFICIENT" — if the context fully answers the question
                - "NEED_MORE: <specific search query>" — if key information is missing

                Iteration: %d of %d
                Question: %s

                Context collected so far:
                %s
                """.formatted(iteration, MAX_ITERATIONS, question, contextPreview);

        try {
            return chatClient.prompt()
                .user(evalPrompt)
                .call()
                .content()
                .trim();
        } catch (Exception e) {
            return "SUFFICIENT";  // fail-safe: don't loop on error
        }
    }

    private String extractNextQuery(String evaluation, String fallbackQuery) {
        if (evaluation.startsWith("NEED_MORE:")) {
            String nextQuery = evaluation.substring("NEED_MORE:".length()).trim();
            return nextQuery.isBlank() ? fallbackQuery : nextQuery;
        }
        return fallbackQuery;
    }
}
