package com.aidevacademy.controller;

import com.aidevacademy.service.RagEvaluationService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/eval")
@CrossOrigin(origins = "*")
public class EvalController {

    private final RagEvaluationService evalService;
    private final VectorStore          vectorStore;

    public EvalController(RagEvaluationService evalService, VectorStore vectorStore) {
        this.evalService = evalService;
        this.vectorStore = vectorStore;
    }

    /**
     * Seed some AI knowledge base docs for testing
     * POST /eval/seed
     */
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed() {
        List<String> docs = List.of(
            "Spring AI is a Java framework by Broadcom/VMware that simplifies LLM integration in Spring Boot applications. It provides a unified ChatClient API, VectorStore support, and RAG pipeline components.",
            "RAG stands for Retrieval-Augmented Generation. It retrieves relevant documents from a vector database and injects them as context into the LLM prompt. This reduces hallucination and enables domain-specific answers.",
            "A vector database stores text as numerical vectors (embeddings). Similarity search finds documents with similar meaning using cosine similarity. Popular options: pgvector, Pinecone, Weaviate, Chroma.",
            "LangChain4j is a Java library for LLM integration inspired by Python's LangChain. Key features: AiServices with @Tool annotations, per-user conversation memory with @MemoryId, and agent support.",
            "Prompt engineering is the practice of crafting effective instructions for LLMs. Key techniques: system/user role separation, few-shot examples, chain-of-thought reasoning, output format constraints."
        );

        List<Document> documents = docs.stream()
                .map(text -> new Document(text, Map.of("source", "seed")))
                .toList();
        vectorStore.add(documents);

        return ResponseEntity.ok(Map.of("seeded", docs.size(), "status", "Knowledge base ready for evaluation"));
    }

    /**
     * Run full RAG quality evaluation
     * GET /eval/run
     */
    @GetMapping("/run")
    public ResponseEntity<Map<String, Object>> runEval() {
        try {
            return ResponseEntity.ok(evalService.runEvaluation());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Ask a single question for manual testing
     * POST /eval/ask  body: "What is Spring AI?"
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody String question) {
        try {
            return ResponseEntity.ok(Map.of("answer", evalService.ask(question)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 20 - RAG Evaluation running! POST /eval/seed then GET /eval/run";
    }
}
