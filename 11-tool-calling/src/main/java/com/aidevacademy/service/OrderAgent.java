package com.aidevacademy.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface OrderAgent {
    @SystemMessage("""
        You are a helpful order management assistant.
        Use the tools provided to answer questions about orders.
        Always look up the actual order status before answering.
        Be concise and friendly.
        """)
    String chat(@UserMessage String message);
}
