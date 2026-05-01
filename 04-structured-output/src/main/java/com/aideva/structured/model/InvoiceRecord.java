package com.aideva.structured.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Represents extracted invoice data.
 * Fields map directly to what the LLM extracts.
 */
public record InvoiceRecord(
        @NotBlank String vendor,
        @NotNull  String amount,
        @NotBlank String currency,
        @NotNull  String date,
        String invoiceNumber,
        List<String> lineItems
) {}
