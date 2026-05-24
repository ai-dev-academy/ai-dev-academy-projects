package com.aidevacademy.supportiq.controller;

import com.aidevacademy.supportiq.service.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * SupportIQ REST API — All 8 RAG techniques exposed as endpoints.
 *
 * Quick start:
 *   1. docker-compose up -d
 *   2. export OPENAI_API_KEY=sk-...
 *   3. mvn spring-boot:run
 *   4. POST /support/ingest  (upload a PDF or text file)
 *   5. POST /support/ask/basic  (body: your question as plain text)
 *      POST /support/ask/hybrid
 *      POST /support/ask/multi-query
 *      POST /support/ask/hyde
 *      POST /support/ask/parent-doc
 *      POST /support/ask/compression
 *      POST /support/ask/reranking
 *      POST /support/ask/agentic
 */
@RestController
@RequestMapping("/support")
@CrossOrigin(origins = "*")
public class SupportController {

    private final BasicRagService basic;
    private final HybridRagService hybrid;
    private final MultiQueryRagService multiQuery;
    private final HydeRagService hyde;
    private final ParentDocumentRagService parentDoc;
    private final ContextualCompressionService compression;
    private final RerankingRagService reranking;
    private final AgenticRagService agentic;
    private final VectorStore vectorStore;

    public SupportController(
            BasicRagService basic,
            HybridRagService hybrid,
            MultiQueryRagService multiQuery,
            HydeRagService hyde,
            ParentDocumentRagService parentDoc,
            ContextualCompressionService compression,
            RerankingRagService reranking,
            AgenticRagService agentic,
            VectorStore vectorStore) {
        this.basic       = basic;
        this.hybrid      = hybrid;
        this.multiQuery  = multiQuery;
        this.hyde        = hyde;
        this.parentDoc   = parentDoc;
        this.compression = compression;
        this.reranking   = reranking;
        this.agentic     = agentic;
        this.vectorStore = vectorStore;
    }

    /**
     * Ask a question using any of the 8 RAG techniques.
     *
     * POST /support/ask/{technique}
     * Body: plain text question (e.g. "I'm getting a NullPointerException when the LLM returns empty...")
     *
     * Technique values: basic | hybrid | multi-query | hyde | parent-doc | compression | reranking | agentic
     */
    @PostMapping("/ask/{technique}")
    public ResponseEntity<Map<String, String>> ask(
            @PathVariable String technique,
            @RequestBody String question) {
        try {
            String answer = switch (technique.toLowerCase()) {
                case "basic"       -> basic.ask(question);
                case "hybrid"      -> hybrid.ask(question);
                case "multi-query" -> multiQuery.ask(question);
                case "hyde"        -> hyde.ask(question);
                case "parent-doc"  -> parentDoc.ask(question);
                case "compression" -> compression.ask(question);
                case "reranking"   -> reranking.ask(question);
                case "agentic"     -> agentic.ask(question);
                default            -> "Unknown technique. Valid values: basic, hybrid, multi-query, hyde, parent-doc, compression, reranking, agentic";
            };
            return ResponseEntity.ok(Map.of(
                "technique", technique,
                "question",  question,
                "answer",    answer
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Ingest a document into the shared vector store.
     * Supports PDF, txt, docx (via Apache Tika).
     *
     * POST /support/ingest
     * Form field: file (multipart)
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(@RequestParam("file") MultipartFile file) {
        try {
            var resource = new InputStreamResource(file.getInputStream());
            List<Document> docs   = new TikaDocumentReader(resource).get();
            List<Document> chunks = new TokenTextSplitter(800, 100, 5, 10000, true).apply(docs);
            vectorStore.add(chunks);
            return ResponseEntity.ok(Map.of(
                "filename", file.getOriginalFilename(),
                "chunks",   chunks.size(),
                "status",   "ingested successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all available techniques with short descriptions.
     * GET /support/techniques
     */
    @GetMapping("/techniques")
    public ResponseEntity<List<Map<String, String>>> techniques() {
        return ResponseEntity.ok(List.of(
            Map.of("id", "basic",       "stars", "⭐⭐",     "name", "① Basic RAG",                  "description", "Embed question → similarity search → answer. Fastest, cheapest."),
            Map.of("id", "hybrid",      "stars", "⭐⭐⭐",    "name", "② Hybrid RAG",                 "description", "Dense vector + sparse keyword (BM25) search merged. Catches exact terms."),
            Map.of("id", "multi-query", "stars", "⭐⭐⭐",    "name", "③ Multi-Query RAG",            "description", "Generates 4 query variations → union of results. Wider coverage."),
            Map.of("id", "hyde",        "stars", "⭐⭐⭐⭐",   "name", "④ HyDE RAG",                   "description", "Generates hypothetical answer → embeds that → searches. Better for short queries."),
            Map.of("id", "parent-doc",  "stars", "⭐⭐⭐⭐",   "name", "⑤ Parent-Document RAG",        "description", "Searches small child chunks, returns full parent docs. Preserves context."),
            Map.of("id", "compression", "stars", "⭐⭐⭐⭐",   "name", "⑥ Contextual Compression RAG", "description", "Retrieves 8 docs, compresses each to only relevant sentences. Reduces noise."),
            Map.of("id", "reranking",   "stars", "⭐⭐⭐⭐⭐",  "name", "⑦ Re-ranking RAG",             "description", "Retrieves 20 candidates, LLM re-ranks to top 5 by relevance. Highest precision."),
            Map.of("id", "agentic",     "stars", "⭐⭐⭐⭐⭐",  "name", "⑧ Agentic RAG",                "description", "LLM drives multi-step retrieval loop until sufficient context is gathered.")
        ));
    }

    /** GET /support/health */
    @GetMapping("/health")
    public String health() {
        return "SupportIQ is running! POST /support/ingest to add docs, then POST /support/ask/{technique} to query. GET /support/techniques for the full list.";
    }
}
