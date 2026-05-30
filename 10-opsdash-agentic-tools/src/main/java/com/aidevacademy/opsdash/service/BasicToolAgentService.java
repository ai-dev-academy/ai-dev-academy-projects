package com.aidevacademy.opsdash.service;

import com.aidevacademy.opsdash.tools.OpsDashTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Technique ①: Basic @Tool Agent
 *
 * PROBLEM: Without tools, the LLM answers from training data only.
 * It guesses metrics, makes up deployment details, and hallucinates root causes.
 * In an incident investigation, a wrong guess costs you 30-60 minutes.
 *
 * SOLUTION: Annotate real Java methods with @Tool. The LLM reads the @Tool
 * description and decides automatically which method to call. Your code
 * runs, returns live data, and the LLM answers from facts — not guesses.
 *
 * FLOW:
 *   User Question
 *        │
 *        ▼
 *   LLM reads @Tool descriptions
 *        │ decides which tool matches
 *        ▼
 *   checkServiceMetrics("checkout")  ← your Java code runs
 *        │ returns: {p50:850ms, p99:1240ms, errorRate:0.4%}
 *        ▼
 *   LLM answers from live data  ← not from training memory
 *
 * ENDPOINT: POST /incident/ask/basic-tool
 */
@Service
public class BasicToolAgentService {

    private final ChatClient chatClient;
    private final OpsDashTools tools;

    public BasicToolAgentService(ChatClient chatClient, OpsDashTools tools) {
        this.chatClient = chatClient;
        this.tools      = tools;
    }

    public String investigate(String question) {
        return chatClient.prompt()
            .system("""
                You are an incident investigation assistant for OpsDash,
                an AI-powered operations platform for microservices teams.

                You have access to tools that query LIVE production data.
                ALWAYS call a tool before answering any performance question.
                NEVER guess latency numbers, error rates, or deployment details.

                After calling a tool, cite which tool you called and exactly
                what it returned before explaining your conclusion.
                """)
            .user(question)
            .tools(tools)                // LLM can call any @Tool method
            .call()
            .content();
    }
}
