package com.aidevacademy.controller;

import com.aidevacademy.service.RagService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;
import java.util.Map;

@RestController
@RequestMapping("/rag")
@CrossOrigin(origins = "*")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * Upload and ingest a PDF or text file
     * POST /rag/ingest  (multipart file)
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(@RequestParam("file") MultipartFile file) {
        try {
            var resource = new InputStreamResource(file.getInputStream());
            int chunks = ragService.ingest(resource);
            return ResponseEntity.ok(Map.of(
                    "filename", file.getOriginalFilename(),
                    "chunks",   chunks,
                    "status",   "ingested successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Ask a question — answered from ingested documents
     * POST /rag/ask  body: "What is the vacation policy?"
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody String question) {
        try {
            String answer = ragService.ask(question);
            return ResponseEntity.ok(Map.of("question", question, "answer", answer));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 09 - RAG Pipeline running! POST file to /rag/ingest then ask at /rag/ask";
    }
}
