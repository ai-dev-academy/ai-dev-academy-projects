package com.aidevacademy.loaniq.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ④: Structured CoT
 *
 * PROBLEM: Zero-Shot and Few-Shot CoT produce free-text reasoning. Downstream code
 * must scrape it with fragile string parsing. When the LLM changes its format
 * slightly ("DECISION: Approve" vs "Decision: I would approve"), parsing breaks.
 * Storing the reasoning in a database means storing raw text blobs — no queryable
 * structure, no risk score column, no confidence field.
 *
 * SOLUTION: Enforce a rigid schema in the system message. Every response MUST contain
 * FACTS, RISKS, MITIGATIONS, RISK_SCORE, DECISION, CONFIDENCE, and SUMMARY as
 * labeled sections. The output maps directly to a database entity with no parsing
 * heuristics — just a clean section split on known keywords.
 *
 * THE IMPROVEMENT:
 *   Free-text CoT:  stored as a text blob → not queryable, not auditable at scale
 *   Structured CoT: stored as typed fields → queryable by risk score, filterable
 *                   by confidence, aggregatable for model performance reports
 *
 * AUDIT VALUE: Regulators can query "show all applications with RISK_SCORE >= 7
 * that were approved in Q1" — impossible with free text, trivial with structured output.
 *
 * ENDPOINT: POST /underwriting/evaluate/structured
 */
@Service
public class StructuredCoTService {

    private final ChatClient chatClient;

    public StructuredCoTService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String evaluate(String applicationSummary) {
        return chatClient.prompt()
            .system("""
                You are a loan underwriting assistant for LoanIQ.
                ALWAYS respond in this EXACT structure. Do not add any other sections.

                FACTS:
                - [key fact 1 from the application data]
                - [key fact 2]
                - [key fact 3]

                RISKS:
                - [risk factor 1: name and severity High/Medium/Low]
                - [risk factor 2: name and severity]

                MITIGATIONS:
                - [how risk 1 is offset by a positive factor, or "None identified"]
                - [how risk 2 is offset, or "None identified"]

                RISK_SCORE: [integer 1-10, where 10 = maximum risk]
                DECISION:   [Approve / Conditional Approval / Reject]
                CONFIDENCE: [High / Medium / Low]
                SUMMARY:    [one sentence suitable for the applicant notification]

                Rules:
                  - RISK_SCORE 1-3   = Approve
                  - RISK_SCORE 4-6   = Conditional Approval
                  - RISK_SCORE 7-10  = Reject
                  - If DECISION contradicts RISK_SCORE, explain in SUMMARY
                """)
            .user(applicationSummary)
            .call()
            .content();
    }
}
