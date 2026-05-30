package com.aidevacademy.loaniq.controller;

import com.aidevacademy.loaniq.model.LoanApplication;
import com.aidevacademy.loaniq.service.*;
import com.aidevacademy.loaniq.tools.SampleApplications;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * LoanIQ REST API — All 8 Chain-of-Thought techniques exposed as endpoints.
 *
 * Quick start:
 *   1. export OPENAI_API_KEY=sk-...
 *   2. mvn spring-boot:run
 *   3. GET  /underwriting/techniques       — list all techniques
 *   4. POST /underwriting/evaluate/zero-shot  — evaluate with default demo application
 *
 * All POST endpoints accept a plain text application summary.
 * Or use GET endpoints (no body) to run against the built-in Application #4821 demo case.
 *
 * Try the SAME question against all 8 techniques to see the reasoning quality difference:
 *   "Application #4821: salary $72K, credit 618, 3 missed payments 2yr ago,
 *    $18K credit card debt, 2yr employment. Approve, conditional, or reject?"
 */
@RestController
@RequestMapping("/underwriting")
@CrossOrigin(origins = "*")
public class UnderwritingController {

    private final ZeroShotCoTService      zeroShot;
    private final FewShotCoTService       fewShot;
    private final SelfConsistencyCoTService selfConsistency;
    private final StructuredCoTService    structured;
    private final TreeOfThoughtsService   treeOfThoughts;
    private final StepBackPromptingService stepBack;
    private final GroundedCoTService      grounded;
    private final SelfCritiqueCoTService  selfCritique;

    public UnderwritingController(
            ZeroShotCoTService zeroShot,
            FewShotCoTService fewShot,
            SelfConsistencyCoTService selfConsistency,
            StructuredCoTService structured,
            TreeOfThoughtsService treeOfThoughts,
            StepBackPromptingService stepBack,
            GroundedCoTService grounded,
            SelfCritiqueCoTService selfCritique) {
        this.zeroShot        = zeroShot;
        this.fewShot         = fewShot;
        this.selfConsistency = selfConsistency;
        this.structured      = structured;
        this.treeOfThoughts  = treeOfThoughts;
        this.stepBack        = stepBack;
        this.grounded        = grounded;
        this.selfCritique    = selfCritique;
    }

    /**
     * Evaluate a loan application using any of the 8 CoT techniques.
     *
     * POST /underwriting/evaluate/{technique}
     * Body: plain text application summary
     *
     * Technique values:
     *   zero-shot | few-shot | self-consistency | structured |
     *   tree-of-thoughts | step-back | grounded | self-critique
     */
    @PostMapping("/evaluate/{technique}")
    public ResponseEntity<Map<String, String>> evaluate(
            @PathVariable String technique,
            @RequestBody String applicationSummary) {
        try {
            String answer = dispatchTechnique(technique, applicationSummary);
            return ResponseEntity.ok(Map.of(
                "technique",   technique,
                "application", applicationSummary,
                "answer",      answer
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Run the built-in Application #4821 demo case through any technique.
     * No request body needed — uses the canonical LoanIQ scenario.
     *
     * GET /underwriting/demo/{technique}
     */
    @GetMapping("/demo/{technique}")
    public ResponseEntity<Map<String, String>> demo(@PathVariable String technique) {
        try {
            LoanApplication app = SampleApplications.app4821();
            String answer = dispatchTechnique(technique, app.toPromptSummary());
            return ResponseEntity.ok(Map.of(
                "technique",      technique,
                "applicationId",  String.valueOf(app.applicationId()),
                "answer",         answer
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all available CoT techniques with descriptions.
     * GET /underwriting/techniques
     */
    @GetMapping("/techniques")
    public ResponseEntity<List<Map<String, String>>> techniques() {
        return ResponseEntity.ok(List.of(
            Map.of("technique", "zero-shot",        "stars", "⭐⭐",
                   "cost", "💰",      "llmCalls", "1",
                   "description", "Add 'think step by step' — 4 words, 20-40% accuracy boost"),
            Map.of("technique", "few-shot",         "stars", "⭐⭐⭐",
                   "cost", "💰",      "llmCalls", "1",
                   "description", "2 worked examples → fixed parseable format every time"),
            Map.of("technique", "self-consistency", "stars", "⭐⭐⭐⭐",
                   "cost", "💰💰💰", "llmCalls", "5",
                   "description", "Run 5× and majority vote → 80% confidence or escalate"),
            Map.of("technique", "structured",       "stars", "⭐⭐⭐",
                   "cost", "💰",      "llmCalls", "1",
                   "description", "Rigid schema → maps directly to DB fields, no parsing heuristics"),
            Map.of("technique", "tree-of-thoughts", "stars", "⭐⭐⭐⭐⭐",
                   "cost", "💰💰💰💰", "llmCalls", "7",
                   "description", "3 branches, scored, best path wins → eliminates anchoring bias"),
            Map.of("technique", "step-back",        "stars", "⭐⭐⭐",
                   "cost", "💰💰",   "llmCalls", "2",
                   "description", "Ask principles first, then apply → correct thresholds cited"),
            Map.of("technique", "grounded",         "stars", "⭐⭐⭐⭐",
                   "cost", "💰💰💰", "llmCalls", "1+tools",
                   "description", "Each step calls a @Tool — DTI 32.3% vs estimated 25%"),
            Map.of("technique", "self-critique",    "stars", "⭐⭐⭐⭐⭐",
                   "cost", "💰💰💰💰", "llmCalls", "3",
                   "description", "Generate → Critique → Correct — catches self-contradictions")
        ));
    }

    private String dispatchTechnique(String technique, String applicationSummary) {
        return switch (technique.toLowerCase()) {
            case "zero-shot"        -> zeroShot.evaluate(applicationSummary);
            case "few-shot"         -> fewShot.evaluate(applicationSummary);
            case "self-consistency" -> selfConsistency.evaluate(applicationSummary);
            case "structured"       -> structured.evaluate(applicationSummary);
            case "tree-of-thoughts" -> treeOfThoughts.evaluate(applicationSummary);
            case "step-back"        -> stepBack.evaluate(applicationSummary);
            case "grounded"         -> grounded.evaluate(
                applicationSummary,
                SampleApplications.app4821().applicationId(),
                SampleApplications.app4821().creditScore()
            );
            case "self-critique"    -> selfCritique.evaluate(applicationSummary);
            default -> "Unknown technique. Valid values: zero-shot, few-shot, self-consistency, " +
                       "structured, tree-of-thoughts, step-back, grounded, self-critique. " +
                       "Or GET /underwriting/techniques for the full list.";
        };
    }
}
