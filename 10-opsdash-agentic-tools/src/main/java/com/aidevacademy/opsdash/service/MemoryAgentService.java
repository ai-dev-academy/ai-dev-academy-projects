package com.aidevacademy.opsdash.service;

import com.aidevacademy.opsdash.tools.OpsDashTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

/**
 * Technique ④: Memory Agent
 *
 * PROBLEM: Each ChatClient call is stateless by default. In a multi-turn incident
 * investigation the LLM re-calls tools it already called in the previous turn:
 *   Turn 1: checkMetrics("checkout") → result stored nowhere
 *   Turn 2: same question → checkMetrics("checkout") AGAIN (wasted API call + time)
 *
 * SOLUTION: Attach ChatMemory to the agent. Every turn — question, tool calls,
 * tool results, and the LLM answer — is stored and replayed in subsequent turns.
 * The LLM reads its prior findings before deciding what to do next.
 *
 * FLOW:
 *   Turn 1: "Why is checkout latency 850ms?"
 *     → checkMetrics + getDeployHistory → stored in memory
 *   Turn 2: "Which specific queries are slow?"
 *     → LLM reads memory → skips checkMetrics (already know p50=850ms)
 *     → calls ONLY querySlowLogs → result added to memory
 *   Turn 3: "Should we rollback or fix forward?"
 *     → LLM reads ALL prior context → 0 tool calls → pure synthesis
 *
 * KEY: Memory eliminates redundant tool calls and enables deep multi-turn
 * investigations that feel like a conversation with a senior SRE.
 *
 * ENDPOINTS:
 *   POST /incident/ask/memory/start  → starts a session, returns sessionId
 *   POST /incident/ask/memory/{sessionId}  → continues investigation
 */
@Service
public class MemoryAgentService {

    private final ChatClient chatClient;
    private final OpsDashTools tools;

    // One InMemoryChatMemory per active investigation session
    private final Map<String, InMemoryChatMemory> sessions = new ConcurrentHashMap<>();

    public MemoryAgentService(ChatClient chatClient, OpsDashTools tools) {
        this.chatClient = chatClient;
        this.tools      = tools;
    }

    /**
     * Start a new investigation session. Returns a sessionId for follow-up turns.
     */
    public Map<String, String> startSession(String question) {
        String sessionId = UUID.randomUUID().toString();
        InMemoryChatMemory memory = new InMemoryChatMemory();
        sessions.put(sessionId, memory);

        String answer = askWithMemory(question, sessionId, memory);
        return Map.of("sessionId", sessionId, "answer", answer);
    }

    /**
     * Continue an existing investigation session.
     * The agent remembers all prior tool calls and findings from this session.
     */
    public String continueSession(String sessionId, String question) {
        InMemoryChatMemory memory = sessions.get(sessionId);
        if (memory == null) {
            return "Session not found: " + sessionId + ". Start a new session first.";
        }
        return askWithMemory(question, sessionId, memory);
    }

    private String askWithMemory(String question, String sessionId, InMemoryChatMemory memory) {
        return chatClient.prompt()
            .system("""
                You are a persistent incident investigator for OpsDash.

                MEMORY RULE: You remember all findings from this conversation.
                Do NOT re-call tools for data you already retrieved this session.
                If you already know checkMetrics("checkout") returned p50=850ms,
                do NOT call it again — read it from your conversation history.

                Build on previous findings to investigate deeper each turn:
                  - Turn 1: gather broad metrics and timeline
                  - Turn 2: deep-dive into the specific failure area
                  - Turn 3: synthesize all findings into a root cause and fix
                """)
            .user(question)
            .tools(tools)
            .advisors(new MessageChatMemoryAdvisor(memory))
            .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
            .call()
            .content();
    }
}
