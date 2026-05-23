package com.aidevacademy.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag")
@CrossOrigin(origins = "*")
public class AdvancedRagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AdvancedRagController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    /**
     * Basic semantic search
     * GET /rag/search?q=query&topK=5
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam String q, @RequestParam(defaultValue = "5") int topK) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(q).withTopK(topK));
        return ResponseEntity.ok(results.stream()
                .map(d -> Map.of("content", (Object) d.getContent().substring(0, Math.min(200, d.getContent().length()))))
                .toList());
    }

    /**
     * HyDE search — generates hypothetical answer, then finds similar docs
     * GET /rag/hyde-search?q=query
     */
    @GetMapping("/hyde-search")
    public ResponseEntity<Map<String, Object>> hydeSearch(@RequestParam String q) {
        // Generate hypothetical document that would answer the question
        String hypothetical = chatClient.prompt()
                .user("Write a brief 2-sentence answer to this question as if from a document: " + q)
                .call()
                .content();

        // Use hypothetical answer as the search query
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(hypothetical).withTopK(5));

        String context = results.stream().map(Document::getContent)
                .collect(Collectors.joining("\n\n"));

        String answer = chatClient.prompt()
                .user("Using this context:\n" + context + "\n\nAnswer: " + q)
                .call()
                .content();

        return ResponseEntity.ok(Map.of(
                "question",    q,
                "hypothetical", hypothetical,
                "answer",      answer,
                "docsUsed",    results.size()
        ));
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 10 - Advanced RAG running! Try GET /rag/hyde-search?q=your question";
    }
}
