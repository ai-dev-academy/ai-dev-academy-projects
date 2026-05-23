package com.aidevacademy.controller;

import com.aidevacademy.model.InvoiceRecord;
import com.aidevacademy.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * POST /ai/extract   body: "Invoice from Acme Corp. Amount: $1,234.56 Date: 2025-01-15"
     */
    @PostMapping("/extract")
    public ResponseEntity<InvoiceRecord> extract(@RequestBody String invoiceText) {
        try {
            InvoiceRecord invoice = invoiceService.extract(invoiceText);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/health")
    public String health() {
        return "Topic 04 - Structured Output running! POST invoice text to /ai/extract";
    }
}
