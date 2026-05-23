package com.aidevacademy.controller;

import com.aidevacademy.service.ClassifierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class EmailClassifierController {

    private final ClassifierService classifierService;

    public EmailClassifierController(ClassifierService classifierService) {
        this.classifierService = classifierService;
    }

    /**
     * Classify an email into BUG | FEATURE | BILLING | GENERAL
     * POST /ai/classify  body: "My payment was charged twice"
     */
    @PostMapping("/classify")
    public ResponseEntity<Map<String, String>> classify(@RequestBody String emailText) {
        try {
            String category = classifierService.classify(emailText);
            return ResponseEntity.ok(Map.of(
                    "category", category,
                    "input",    emailText.substring(0, Math.min(50, emailText.length())) + "..."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Classify with custom categories
     * POST /ai/classify-custom?categories=URGENT,NORMAL,LOW
     */
    @PostMapping("/classify-custom")
    public ResponseEntity<Map<String, String>> classifyCustom(
            @RequestParam String categories,
            @RequestBody String text) {
        try {
            String category = classifierService.classifyWithCategories(text, categories);
            return ResponseEntity.ok(Map.of("category", category));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 02 - Prompt Engineering is running! POST to /ai/classify to test.";
    }
}
