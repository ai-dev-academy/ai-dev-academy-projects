package com.aidevacademy.model;

import java.util.List;

public record InvoiceRecord(
    String vendor,
    String amount,
    String currency,
    String date,
    String invoiceNumber,
    List<String> lineItems
) {}
