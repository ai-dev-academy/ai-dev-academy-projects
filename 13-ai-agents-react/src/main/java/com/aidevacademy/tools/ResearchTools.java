package com.aidevacademy.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ResearchTools {

    private final List<String> notes = new ArrayList<>();

    @Tool("Search for information about a topic. Returns a summary of key facts.")
    public String searchWeb(String query) {
        // Simulated search results - in production use a real search API
        return switch (query.toLowerCase()) {
            case String q when q.contains("spring ai") ->
                "Spring AI is a framework by VMware/Broadcom that provides a Spring-friendly API for LLM integration. Key features: ChatClient, VectorStore, RAG support, multiple providers (OpenAI, Anthropic, Ollama). Version 1.0 released 2024.";
            case String q when q.contains("langchain4j") ->
                "LangChain4j is a Java port of LangChain. Features: AiServices with @Tool annotations, memory management, streaming, document loading. Popular in enterprise Java shops. Version 0.32+ has Spring Boot starter.";
            case String q when q.contains("rag") ->
                "RAG (Retrieval-Augmented Generation) retrieves relevant documents from a vector store and injects them into the LLM prompt as context. Solves hallucination for domain-specific questions. Key metrics: faithfulness, relevancy.";
            default ->
                "Found general information about: " + query + ". This is a simulated search result. In production, integrate with a real search API like Serper or Tavily.";
        };
    }

    @Tool("Save an important finding to the research notes for the final report.")
    public String saveNote(String finding) {
        notes.add(finding);
        return "Note saved: " + finding;
    }

    @Tool("Get all saved research notes compiled so far.")
    public String getNotes() {
        if (notes.isEmpty()) return "No notes saved yet.";
        StringBuilder sb = new StringBuilder("Research notes:\n");
        for (int i = 0; i < notes.size(); i++) {
            sb.append(i + 1).append(". ").append(notes.get(i)).append("\n");
        }
        return sb.toString();
    }

    public void clearNotes() { notes.clear(); }
}
