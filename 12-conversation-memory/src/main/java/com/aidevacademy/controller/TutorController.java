package com.aidevacademy.controller;

import com.aidevacademy.service.TutorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/tutor")
@CrossOrigin(origins = "*")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    /**
     * POST /tutor/ask?studentId=alice  body: "What is a variable?"
     * Each studentId has isolated memory - alice and bob never share context
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(
            @RequestParam(defaultValue = "default") String studentId,
            @RequestBody String question) {
        try {
            String answer = tutorService.teach(studentId, question);
            return ResponseEntity.ok(Map.of(
                    "answer",       answer,
                    "studentId",    studentId,
                    "messageCount", tutorService.getMessageCount(studentId)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /tutor/memory?studentId=alice
     * Clear conversation history for a student
     */
    @DeleteMapping("/memory")
    public ResponseEntity<Map<String, String>> clearMemory(
            @RequestParam String studentId) {
        tutorService.clearMemory(studentId);
        return ResponseEntity.ok(Map.of("status", "Memory cleared for " + studentId));
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 12 - Conversation Memory running! POST /tutor/ask?studentId=alice";
    }
}
