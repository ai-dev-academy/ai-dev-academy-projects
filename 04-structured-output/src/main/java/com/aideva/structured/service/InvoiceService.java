package com.aideva.structured.service;

import com.aideva.structured.model.InvoiceRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    private final ChatClient chatClient;

    public InvoiceService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * Extracts structured invoice fields from free-form text.
     * Retries up to 3 times if JSON parsing fails.
     */
    public InvoiceRecord extractInvoice(String invoiceText) {
        var converter = new BeanOutputConverter<>(InvoiceRecord.class);

        String prompt = """
                Extract invoice data from the text below.
                Return ONLY valid JSON matching this schema — no markdown, no explanation:
                %s

                Invoice text:
                %s
                """.formatted(converter.getFormat(), invoiceText);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                String raw = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();
                return converter.convert(raw);
            } catch (Exception e) {
                if (attempt == 3) {
                    throw new RuntimeException("Failed to extract invoice after 3 attempts: " + e.getMessage());
                }
            }
        }
        return null; // unreachable
    }
}
