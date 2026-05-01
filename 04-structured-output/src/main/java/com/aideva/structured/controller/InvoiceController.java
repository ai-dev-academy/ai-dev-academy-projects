package com.aideva.structured.controller;

import com.aideva.structured.model.InvoiceRecord;
import com.aideva.structured.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * POST /extract
 * Body: plain invoice text (JSON-quoted string)
 * Returns: structured JSON with vendor, amount, currency, date, etc.
 *
 * curl -X POST http://localhost:8080/extract \
 *      -H "Content-Type: application/json" \
 *      -d '"Invoice from Acme Corp. Amount: $1,234.56. Date: 2025-01-15. Invoice #INV-001"'
 */
@RestController
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/extract")
    public ResponseEntity<?> extract(@RequestBody String invoiceText) {
        try {
            InvoiceRecord result = invoiceService.extractInvoice(invoiceText);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(422).body(e.getMessage());
        }
    }
}
