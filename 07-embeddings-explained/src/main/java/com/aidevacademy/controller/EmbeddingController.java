package com.aidevacademy.controller;

import com.aidevacademy.service.EmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * Semantic product search
     * GET /ai/search?q=shoes for running&topK=3
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "3") int topK) {
        try {
            return ResponseEntity.ok(embeddingService.search(q, topK));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 07 - Embeddings running! GET /ai/search?q=running shoes";
    }
}
