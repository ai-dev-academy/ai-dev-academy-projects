package com.aidevacademy.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ResearchAgent {
    @SystemMessage("""
        You are a research agent. Your job is to research topics thoroughly.
        
        For each research task:
        1. Search for relevant information using the searchWeb tool
        2. Save important findings using the saveNote tool
        3. Search for additional angles or related topics
        4. Compile a final summary using your saved notes
        
        Be systematic. Search at least 2-3 different aspects of the topic.
        Always base your final answer on the notes you have saved.
        """)
    String research(@UserMessage String topic);
}
