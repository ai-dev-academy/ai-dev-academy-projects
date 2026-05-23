package com.aidevacademy.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagEvaluationService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // Golden Q&A test set — add more as your knowledge base grows
    private static final List<Map<String, String>> GOLDEN_SET = List.of(
        Map.of("question", "What is Spring AI?",           "expectedKeyword", "framework"),
        Map.of("question", "What is RAG?",                 "expectedKeyword", "retrieval"),
        Map.of("question", "What is a vector database?",   "expectedKeyword", "vector"),
        Map.of("question", "What is LangChain4j?",         "expectedKeyword", "java"),
        Map.of("question", "What is prompt engineering?",  "expectedKeyword", "prompt")
    );

    public RagEvaluationService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient  = chatClient;
        this.vectorStore = vectorStore;
    }

    /** Answer a question using RAG */
    public String ask(String question) {
        List<Document> context = vectorStore.similaritySearch(
                SearchRequest.query(question).withTopK(5));

        if (context.isEmpty()) {
            return chatClient.prompt().user(question).call().content();
        }

        String contextText = context.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n"));

        return chatClient.prompt()
                .user("Context:\n" + contextText + "\n\nQuestion: " + question +
                      "\n\nAnswer based only on the context above.")
                .call().content();
    }

    /** Score faithfulness: does the answer use only context info? */
    public double scoreFaithfulness(String context, String answer) {
        if (context.isEmpty()) return 0.5;
        String prompt = """
                Context: %s
                Answer: %s
                
                Does this answer contain ONLY information from the context?
                Score 0.0 (completely hallucinated) to 1.0 (fully grounded in context).
                Reply with ONLY a decimal number like 0.85
                """.formatted(context.substring(0, Math.min(500, context.length())), answer);

        try {
            String score = chatClient.prompt().user(prompt).call().content().trim();
            return Double.parseDouble(score.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0.5;
        }
    }

    /** Run full evaluation on golden test set */
    public Map<String, Object> runEvaluation() {
        List<Map<String, Object>> results = new ArrayList<>();
        double totalFaithfulness = 0;
        int    passed            = 0;

        for (Map<String, String> qa : GOLDEN_SET) {
            String question       = qa.get("question");
            String expectedWord   = qa.get("expectedKeyword");
            String answer         = ask(question);

            // Keyword check (simple baseline)
            boolean keywordFound = answer.toLowerCase().contains(expectedWord.toLowerCase());

            // Faithfulness score
            List<Document> context = vectorStore.similaritySearch(
                    SearchRequest.query(question).withTopK(3));
            String contextText = context.stream().map(Document::getContent)
                    .collect(Collectors.joining(" "));
            double faithfulness = scoreFaithfulness(contextText, answer);

            totalFaithfulness += faithfulness;
            if (keywordFound) passed++;

            results.add(Map.of(
                    "question",     question,
                    "answer",       answer.substring(0, Math.min(100, answer.length())) + "...",
                    "keywordFound", keywordFound,
                    "faithfulness", String.format("%.2f", faithfulness)
            ));
        }

        double avgFaithfulness = totalFaithfulness / GOLDEN_SET.size();
        double keywordAccuracy = (double) passed / GOLDEN_SET.size();

        return Map.of(
                "totalQuestions",    GOLDEN_SET.size(),
                "passed",            passed,
                "keywordAccuracy",   String.format("%.0f%%", keywordAccuracy * 100),
                "avgFaithfulness",   String.format("%.2f", avgFaithfulness),
                "meetsThreshold",    avgFaithfulness >= 0.70,
                "threshold",         "0.70",
                "results",           results
        );
    }
}
