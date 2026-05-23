package com.aidevacademy.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    /** Step 1-3: Ingest a document (load → split → embed → store) */
    public int ingest(Resource resource) {
        // Load
        List<Document> docs = new TikaDocumentReader(resource).get();

        // Split into 800-token chunks with 100-token overlap
        List<Document> chunks = new TokenTextSplitter(800, 100, 5, 10000, true)
                .apply(docs);

        // Embed + store (Spring AI does both automatically)
        vectorStore.add(chunks);
        return chunks.size();
    }

    /** Step 4-6: Answer a question using retrieved context */
    public String ask(String question) {
        // Retrieve top 5 relevant chunks
        List<Document> context = vectorStore.similaritySearch(
                SearchRequest.query(question).withTopK(5));

        // Build grounded prompt
        String contextText = context.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                Answer the question using ONLY the context below.
                If the answer is not in the context, say "I don't have that information in the provided documents."
                
                Context:
                %s
                
                Question: %s
                """.formatted(contextText, question);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
