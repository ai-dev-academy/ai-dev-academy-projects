package com.aidevacademy.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface CustomerSupport {
    @SystemMessage("""
        You are a helpful and friendly customer support agent for AI Dev Academy.
        You help developers learn AI integration with Java Spring Boot.
        Be concise, encouraging, and practical.
        Remember the context of this conversation.
        """)
    String chat(@MemoryId String userId, @UserMessage String message);
}
