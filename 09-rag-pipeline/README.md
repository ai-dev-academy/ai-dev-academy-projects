# 🔍 RAG Pipeline

> Complete 6-step Retrieval-Augmented Generation

**Tier:** Pro | **Duration:** 60 min | **Difficulty:** Intermediate | **Category:** RAG & Vector

**Tags:** RAG, DocumentLoader, TextSplitter, QuestionAnswerAdvisor

---

## Description

Build a complete RAG pipeline — ingest PDFs, chunk, embed, store, retrieve and generate grounded answers.

---

## What You'll Learn

- Full ingest pipeline
- QuestionAnswerAdvisor
- Chunking strategies
- Grounding prompts

---

## Tech Stack

- Spring AI
- pgvector
- Apache Tika
- Java 17

---

## Project

Document Q&A — upload any PDF and ask questions

---

## Real-World Use Case

HR portal where employees ask questions answered from company policy documents

---

## Prompt Guide

System: "Answer using ONLY the context below. If not in context, say 'I don't have that information.' Context: {context}"

---

## Steps

### Step 1: Set up document loader

**Why:** Apache Tika extracts text from any file format — PDF, Word, HTML — with one API.

**What:** Configure TikaDocumentReader for PDF ingestion.
**Files:** IngestService.java
```
service/IngestService.java
```
```
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-tika-document-reader</artifactId>
</dependency>

var docs = new TikaDocumentReader(pdfResource).get();
```
**Common Mistakes:**
- Using PDFBox directly — Tika handles more formats
**Testing:** Load a PDF — log doc.getContent() — verify text extracted
**Expected:** PDF text extracted without formatting artifacts

---

### Step 2: Configure text splitter

**Why:** Chunk size is the most impactful RAG parameter. Too big = irrelevant context. Too small = loses meaning.

**What:** Configure TokenTextSplitter with 800 tokens, 100 overlap.
**Files:** RagConfig.java
```
config/RagConfig.java
```
```
@Bean
public TextSplitter textSplitter() {
  return new TokenTextSplitter(
    800,   // chunk size (tokens)
    100,   // overlap (tokens) — context continuity
    5,     // min chunk size
    10000, // max chunk size
    true   // keep separator
  );
}
```
**Common Mistakes:**
- Zero overlap — sentences at chunk boundary lose context
**Testing:** Split a 5000-token document — verify chunks are 700-900 tokens each
**Expected:** ~7 chunks with consistent size and 100-token overlap between consecutive chunks

---

### Step 3: Build ingest endpoint

**Why:** An API endpoint lets users upload documents through a web UI.

**What:** Create POST /ingest accepting multipart file upload.
**Files:** RagController.java
```
controller/RagController.java
```
```
@PostMapping("/ingest")
public String ingest(@RequestParam MultipartFile file) {
  var resource = new InputStreamResource(file.getInputStream());
  ingestService.ingest(resource, file.getOriginalFilename());
  return "Ingested: " + file.getOriginalFilename();
}
```
**Common Mistakes:**
- No file size limit — 100MB PDF will OOM your server
**Testing:** curl -X POST http://localhost:8080/ingest \
  -F "file=@company-policy.pdf"
**Expected:** "Ingested: company-policy.pdf" — check vector_store table row count increased

---

### Step 4: Add QuestionAnswerAdvisor

**Why:** This Spring AI advisor automatically retrieves relevant chunks and injects them into the prompt.

**What:** Register the advisor on your ChatClient.
**Files:** AiConfig.java
```
config/AiConfig.java
```
```
@Bean
public ChatClient ragClient(ChatClient.Builder builder, VectorStore vectorStore) {
  return builder
    .defaultAdvisors(new QuestionAnswerAdvisor(
      vectorStore,
      SearchRequest.defaults().withTopK(5)
    ))
    .build();
}
```
**Common Mistakes:**
- topK too high (>10) — irrelevant chunks hurt answer quality
**Testing:** Ask question about ingested document — verify answer references actual content
**Expected:** Answer contains specific facts from PDF, not hallucinated content

---

### Step 5: Build Q&A endpoint

**Why:** Users ask questions — your app retrieves context and generates grounded answers.

**What:** Create POST /ask endpoint.
**Files:** RagController.java
```
No new files
```
```
@PostMapping("/ask")
public Map<String, String> ask(@RequestBody String question) {
  String answer = ragClient.prompt()
    .user(question)
    .call()
    .content();
  return Map.of("question", question, "answer", answer);
}
```
**Common Mistakes:**
- Not returning the question — hard to debug which question got which answer
**Testing:** curl -X POST http://localhost:8080/ask \
  -d '"What is the vacation policy?"'
**Expected:** Answer contains specific policy details from ingested PDF

---

### Step 6: Evaluate with RAGAS metrics

**Why:** How do you know if RAG is working? RAGAS measures faithfulness and relevancy objectively.

**What:** Build a simple eval framework checking answer faithfulness.
**Files:** RagEvaluation.java
```
test/RagEvaluation.java
```
```
// Golden Q&A pairs
record QAPair(String question, String expectedKeyword) {}

List<QAPair> goldenSet = List.of(
  new QAPair("How many vacation days?", "15 days"),
  new QAPair("Remote work policy?", "3 days per week")
);

goldenSet.forEach(qa -> {
  String answer = ragService.ask(qa.question());
  assertThat(answer).contains(qa.expectedKeyword());
});
```
**Common Mistakes:**
- Golden set with only 2 questions — not statistically significant
**Testing:** Run golden set before and after any RAG change — track regression
**Expected:** All golden set questions answered correctly based on document content

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/09-rag-pipeline

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
