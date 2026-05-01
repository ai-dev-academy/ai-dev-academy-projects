package com.aideva.structured;

import com.aideva.structured.controller.InvoiceController;
import com.aideva.structured.model.InvoiceRecord;
import com.aideva.structured.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  InvoiceService invoiceService;

    @Test
    void extractReturnsStructuredJson() throws Exception {
        var invoice = new InvoiceRecord("Acme Corp", "1234.56", "USD",
                "2025-01-15", "INV-001", List.of("Widget x2", "Service fee"));
        when(invoiceService.extractInvoice(anyString())).thenReturn(invoice);

        mockMvc.perform(post("/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"Invoice from Acme Corp. Amount: $1234.56\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendor").value("Acme Corp"))
                .andExpect(jsonPath("$.amount").value("1234.56"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
}
