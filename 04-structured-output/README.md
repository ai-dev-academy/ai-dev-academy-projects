# 📦 Structured Output

> Map LLM responses directly to Java POJOs

**Tier:** Free | **Duration:** 20 min | **Difficulty:** Beginner | **Category:** Foundations

**Tags:** JSON, POJO, Spring AI, Records

---

## Description

Use Spring AI structured output to map LLM JSON responses directly to typed Java records.

---

## What You'll Learn

- Spring AI BeanOutputConverter
- Designing output schemas
- Java records for AI output
- Validation and error handling

---

## Tech Stack

- Spring Boot 3.x
- Spring AI
- Jackson
- Java Records

---

## Project

Invoice Parser — extracts structured fields from invoice text

---

## Real-World Use Case

An accounts payable system that automatically extracts vendor, amount, date from emailed invoices

---

## Prompt Guide

System: "Extract invoice data. Reply ONLY with valid JSON matching this schema: {schema}. No explanation, no markdown, just JSON."

---

## Steps

### Step 1: Define output record

**Why:** Java records are perfect for AI output — immutable, concise, auto-generates equals/hashCode.

**What:** Define exactly what fields you want extracted.
**Files:** InvoiceRecord.java
```
model/InvoiceRecord.java
```
```
public record InvoiceRecord(
    String vendor,
    String amount,
    String currency,
    String date,
    String invoiceNumber,
    List<String> lineItems
) {}
```
**Common Mistakes:**
- Using class instead of record — more boilerplate for no benefit
**Testing:** Compile check: record should compile without explicit constructor
**Expected:** Record compiles cleanly with all fields accessible via accessor methods

---

### Step 2: Create BeanOutputConverter

**Why:** The converter generates the JSON schema from your record and tells the LLM exactly what structure to return.

**What:** Instantiate BeanOutputConverter with your record type.
**Files:** InvoiceService.java
```
service/InvoiceService.java
```
```
var converter = new BeanOutputConverter<>(InvoiceRecord.class);
String format = converter.getFormat(); // JSON schema

String prompt = "Extract invoice data from:
" + text 
    + "

Return as JSON:
" + format;

String raw = chatClient.prompt().user(prompt).call().content();
InvoiceRecord invoice = converter.convert(raw);
```
**Common Mistakes:**
- Not including converter.getFormat() in prompt — LLM won't know the schema
**Testing:** Log the raw string before conversion — check it's valid JSON
**Expected:** converter.convert() returns fully populated InvoiceRecord

---

### Step 3: Add validation

**Why:** LLMs sometimes hallucinate fields. Validate before storing or processing.

**What:** Use Jakarta validation annotations on the record.
**Files:** InvoiceRecord.java
```
No new files
```
```
public record InvoiceRecord(
    @NotBlank String vendor,
    @Pattern(regexp="\d+\.\d{2}") String amount,
    @NotBlank String currency,
    @NotNull String date,
    String invoiceNumber,
    List<String> lineItems
) {}
```
**Common Mistakes:**
- Trusting LLM output without validation — amount might be "unknown"
**Testing:** Send invoice without an amount — verify validation catches it
**Expected:** ValidationException thrown for missing required fields

---

### Step 4: Build extraction endpoint

**Why:** Expose the parser as an API so any system can submit invoice text and get structured data.

**What:** Create POST /extract endpoint returning JSON.
**Files:** InvoiceController.java
```
controller/InvoiceController.java
```
```
@PostMapping("/extract")
public InvoiceRecord extract(@RequestBody String invoiceText) {
    return invoiceService.extractInvoice(invoiceText);
}
```
**Common Mistakes:**
- Returning raw string instead of the record object
**Testing:** curl -X POST http://localhost:8080/extract \
  -d '"Invoice from Acme Corp. Amount: $1,234.56 Date: 2025-01-15"'
**Expected:** {"vendor":"Acme Corp","amount":"1234.56","currency":"USD"...}

---

### Step 5: Handle conversion failures

**Why:** LLMs occasionally return malformed JSON. Retry with clearer instructions.

**What:** Add retry logic when JSON parsing fails.
**Files:** InvoiceService.java
```
No new files
```
```
public InvoiceRecord extractInvoice(String text) {
    for (int attempt = 0; attempt < 3; attempt++) {
        try {
            String raw = callLlm(text, attempt > 0);
            return converter.convert(raw);
        } catch (Exception e) {
            if (attempt == 2) throw new ExtractionException("Failed after 3 attempts");
        }
    }
    return null;
}
```
**Common Mistakes:**
- Infinite retry — always cap attempts
**Testing:** Intercept and corrupt the LLM response — verify retry kicks in
**Expected:** After 3 failed attempts throws ExtractionException with clear message

---

### Step 6: Test with real invoices

**Why:** Real invoices have quirky formats — PDFs converted to text, tables, foreign currencies.

**What:** Test against 10 diverse invoice formats.
**Files:** InvoiceServiceTest.java
```
test/InvoiceServiceTest.java
```
```
@Test
void testMultiCurrencyInvoice() {
    String invoice = "From: Tokyo Tech. Amount: ¥50,000. Date: 2025-01";
    InvoiceRecord result = service.extractInvoice(invoice);
    assertThat(result.vendor()).isEqualTo("Tokyo Tech");
    assertThat(result.currency()).isEqualTo("JPY");
}
```
**Common Mistakes:**
- Testing only USD English invoices — real world is diverse
**Testing:** Test invoices in 3 different languages and 5 different formats
**Expected:** All 10 test invoices extract vendor and amount correctly

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/04-structured-output

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
