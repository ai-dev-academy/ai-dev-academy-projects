package com.aideva.prompteng.controller;

import com.aideva.prompteng.service.EmailClassifierService;
import org.springframework.web.bind.annotation.*;

/**
 * POST /classify
 * Body: plain email text (JSON-quoted string)
 * Returns: {"category":"BUG","confidence":"HIGH"}
 *
 * curl -X POST http://localhost:8080/classify \
 *      -H "Content-Type: application/json" \
 *      -d '"My payment was charged twice"'
 */
@RestController
public class ClassifierController {

    record ClassificationResult(String category, String confidence) {}

    private final EmailClassifierService classifierService;

    public ClassifierController(EmailClassifierService classifierService) {
        this.classifierService = classifierService;
    }

    @PostMapping("/classify")
    public ClassificationResult classify(@RequestBody String email) {
        String category = classifierService.classify(email);
        return new ClassificationResult(category, "HIGH");
    }
}
