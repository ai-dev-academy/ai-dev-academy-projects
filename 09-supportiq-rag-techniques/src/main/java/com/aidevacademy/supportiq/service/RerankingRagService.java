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
 * Technique ⑦: Re-ranking RAG
 *
 * Problem: Vector similarity scores measure geometric distance in embedding space —
 * not actual answer quality. The #1 result by cosine similarity might be a doc that
 * uses the same words but answers a different question.
 *
 * Solution: Retrieve a large candidate pool (20 docs), then ask the LLM to re-rank
 * them by how useful each is for answering the specific question. Keep only top 5.
 *
 * Flow:
 *   User Question
 *        │
 *        ▼
 *   [Similarity Search]  ← large pool: top-20 by cosine
 *        │
 *        ▼
 *   [LLM Re-ranking]  ← "rank these 20 docs by answer relevance, return ordered list"
 *        │
 *        ▼
 *   [Keep top-5 re-ranked docs]
 *        │
 *        ▼
 *   [LLM Answer]
 *
 * Result: The LLM answers from the most relevant docs, not just the closest embeddings.
 * Limitation: Re-ranking requires one extra LLM call with up to 20 doc previews — slower.
 */
@Service
public class RerankingRagService {

    private static final int INITIAL_POOL = 20;
    private static final int FINAL_TOP_K  = 5;

    private final ChatClient  chatClient;
    private final VectorStore vectorStore;

    public RerankingRagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient  = chatClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        // 1. Retrieve a large candidate pool
        List<Document> candidates = vectorStore.similaritySearch(
            SearchRequest.query(question).withTopK(INITIAL_POOL)
        );

        if (candidates.isEmpty()) {
            return "No relevant documentation found. Please ingest documents first via POST /support/ingest";
        }

        // 2. Re-rank candidates by answer relevance using the LLM
        List<Document> reranked = rerank(candidates, question, FINAL_TOP_K);

        String contextText = reranked.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                You are SupportIQ. Answer using ONLY the context below.
                These documents were re-ranked for maximum relevance to your question.

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
     * Ask the LLM to rank documents by relevance to the question.
     * Returns the top-K documents in re-ranked order.
     * Falls back to original order if LLM response cannot be parsed.
     */
    private List<Document> rerank(List<Document> docs, String question, int topK) {
        if (docs.size() <= topK) return docs;

        StringBuilder sb = new StringBuilder();
        sb.append("Rank these documents by how useful they are for answering the question.\n");
        sb.append("Return ONLY a comma-separated list of document numbers in order of relevance (e.g. 3,1,7,2,5).\n");
        sb.append("Question: ").append(question).append("\n\n");

        for (int i = 0; i < docs.size(); i++) {
            String preview = docs.get(i).getContent();
            if (preview.length() > 250) preview = preview.substring(0, 250) + "...";
            sb.append("Document ").append(i + 1).append(": ").append(preview).append("\n\n");
        }

        try {
            String rankingResponse = chatClient.prompt()
                .user(sb.toString())
                .call()
                .content();

            List<Document> rerankedDocs = new ArrayList<>();
            for (String token : rankingResponse.split("[,\\s]+")) {
                try {
                    int idx = Integer.parseInt(token.trim()) - 1;
                    if (idx >= 0 && idx < docs.size()) {
                        rerankedDocs.add(docs.get(idx));
                    }
                    if (rerankedDocs.size() >= topK) break;
                } catch (NumberFormatException ignored) {
                    // skip non-numeric tokens in LLM response
                }
            }
            if (!rerankedDocs.isEmpty()) return rerankedDocs;
        } catch (Exception ignored) {
            // fall through to default ordering
        }

        // Fallback: return first topK in original similarity order
        return docs.subList(0, Math.min(topK, docs.size()));
    }
}
