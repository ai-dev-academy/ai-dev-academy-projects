package com.aidevacademy.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class VectorDbController {

    private final VectorStore vectorStore;

    public VectorDbController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Add documents to the vector store
     * POST /ai/store  body: ["Doc 1 text", "Doc 2 text"]
     */
    @PostMapping("/store")
    public ResponseEntity<Map<String, Object>> store(@RequestBody List<String> texts) {
        try {
            List<Document> docs = texts.stream()
                    .map(text -> new Document(text, Map.of("source", "api")))
                    .toList();
            vectorStore.add(docs);
            return ResponseEntity.ok(Map.of("stored", texts.size(), "status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search for similar documents
     * GET /ai/search?q=your query&topK=5
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int topK) {
        try {
            List<Document> results = vectorStore.similaritySearch(
                    SearchRequest.query(q).withTopK(topK));
            List<Map<String, Object>> response = results.stream()
                    .map(doc -> Map.of(
                            "content",  (Object) doc.getContent(),
                            "metadata", doc.getMetadata()
                    ))
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 08 - Vector Databases running! Make sure Docker is running with pgvector.";
    }
}
