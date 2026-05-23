package com.aidevacademy.service;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TutorService {

    interface Tutor {
        @SystemMessage("""
            You are a patient and encouraging Java programming tutor.
            Remember what the student has already learned and build on it.
            Keep responses concise — 2-3 short paragraphs maximum.
            Use simple examples. Never assume prior knowledge.
            """)
        String teach(@MemoryId String studentId, @UserMessage String question);
    }

    private final Tutor tutor;
    // In-memory store - replace with Redis in production
    private final ConcurrentHashMap<String, MessageWindowChatMemory> memoryMap = new ConcurrentHashMap<>();

    public TutorService(@Value("${spring.ai.openai.api-key}") String apiKey) {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .build();

        this.tutor = AiServices.builder(Tutor.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(studentId -> memoryMap.computeIfAbsent(
                        studentId.toString(),
                        id -> MessageWindowChatMemory.withMaxMessages(20)))
                .build();
    }

    public String teach(String studentId, String question) {
        return tutor.teach(studentId, question);
    }

    public void clearMemory(String studentId) {
        memoryMap.remove(studentId);
    }

    public int getMessageCount(String studentId) {
        MessageWindowChatMemory memory = memoryMap.get(studentId);
        return memory != null ? memory.messages().size() : 0;
    }
}
