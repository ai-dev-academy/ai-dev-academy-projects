package com.aidevacademy.service;

import com.aidevacademy.model.InvoiceRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    private final ChatClient chatClient;

    public InvoiceService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public InvoiceRecord extract(String invoiceText) {
        var converter = new BeanOutputConverter<>(InvoiceRecord.class);

        String prompt = """
                Extract invoice data from the following text.
                %s
                
                Text to extract from:
                %s
                """.formatted(converter.getFormat(), invoiceText);

        String raw = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return converter.convert(raw);
    }
}
