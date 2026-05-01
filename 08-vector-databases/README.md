# 🗄️ Vector Databases

> Store, index and search embeddings at scale with pgvector

**Tier:** Pro | **Duration:** 40 min | **Difficulty:** Intermediate | **Category:** RAG & Vector

**Tags:** pgvector, Pinecone, Spring AI, VectorStore

---

## Description

Set up pgvector with PostgreSQL and integrate with Spring AI VectorStore.

---

## What You'll Learn

- pgvector with Docker
- Spring AI VectorStore API
- HNSW vs IVFFlat indexes
- Metadata filtering

---

## Tech Stack

- PostgreSQL
- pgvector
- Spring AI
- Docker

---

## Project

pgvector Spring Boot integration — full ingest and similarity search

---

## Real-World Use Case

Legal firm knowledge base searching thousands of case documents

---

## Prompt Guide

After retrieval, ground LLM: "Answer ONLY based on: {context}. If not in context, say so."

---

## Steps

### Step 1: Run pgvector with Docker

**Why:** pgvector extends PostgreSQL with vector operations. Docker gives you a clean isolated database.

**What:** Start PostgreSQL with pgvector extension using Docker Compose.
**Files:** docker-compose.yml
```
docker-compose.yml in root
```
```
version: '3.8'
services:
  pgvector:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_DB: aidb
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
```
**Common Mistakes:**
- Using plain postgres image without pgvector extension
**Testing:** psql -h localhost -U postgres -d aidb -c "CREATE EXTENSION vector;"
**Expected:** Extension created successfully without errors

---

### Step 2: Configure Spring AI pgvector

**Why:** Spring AI VectorStore abstraction means switching to Pinecone later requires zero code changes.

**What:** Add pgvector starter and configure connection.
**Files:** pom.xml, application.properties
```
No new files
```
```
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
</dependency>

# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/aidb
spring.ai.vectorstore.pgvector.dimensions=1536
```
**Common Mistakes:**
- Wrong dimensions — must match your embedding model output
**Testing:** Startup creates vector table automatically
**Expected:** Table "vector_store" created with 1536-dimension column

---

### Step 3: Ingest documents

**Why:** The ingest pipeline converts raw documents into searchable vectors.

**What:** Load documents, split into chunks, embed and store.
**Files:** IngestService.java
```
service/IngestService.java
```
```
public void ingest(Resource doc) {
  var docs = new TikaDocumentReader(doc).get();
  var chunks = new TokenTextSplitter(800, 100).apply(docs);
  vectorStore.add(chunks); // embeds + stores automatically
}
```
**Common Mistakes:**
- Chunk size too large (>1000 tokens) — loses retrieval precision
**Testing:** Ingest a 10-page PDF — verify 40-60 chunks created in vector_store table
**Expected:** SELECT COUNT(*) FROM vector_store returns ~50 rows

---

### Step 4: Query with metadata filtering

**Why:** Metadata filters let you search within a specific document or category.

**What:** Add metadata to documents and filter during search.
**Files:** SearchService.java
```
No new files
```
```
// Ingest with metadata
var doc = new Document(text, Map.of("source", "legal-2024", "type", "contract"));
vectorStore.add(List.of(doc));

// Search only contracts from 2024
var results = vectorStore.similaritySearch(
  SearchRequest.query("termination clause")
    .withFilterExpression("type == 'contract'")
    .withTopK(5)
);
```
**Common Mistakes:**
- Not adding metadata at ingest — can't filter later
**Testing:** Ingest 2 doc types, search with filter — verify only correct type returned
**Expected:** Filter reduces result set to only matching document type

---

### Step 5: Compare index strategies

**Why:** HNSW is fast for search but slow to build. IVFFlat is opposite. Pick based on your use case.

**What:** Test both index types on your data.
**Files:** IndexConfig.java
```
config/IndexConfig.java
```
```
-- HNSW: best for search speed (production default)
CREATE INDEX ON vector_store USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

-- IVFFlat: best for fast ingest + acceptable search
CREATE INDEX ON vector_store USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);
```
**Common Mistakes:**
- No index at all — full table scan on every query
**Testing:** Time 1000 searches with HNSW vs no index — measure speedup
**Expected:** HNSW search 10-100x faster than sequential scan at >10K vectors

---

### Step 6: Production configuration

**Why:** Dev settings work for 1000 vectors. Production needs tuning for millions.

**What:** Configure connection pool, index parameters, and monitoring.
**Files:** application-prod.properties
```
resources/application-prod.properties
```
```
# Production pgvector settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.connection-timeout=30000

# Increase ef_search for better recall at scale
# SET hnsw.ef_search = 100;
```
**Common Mistakes:**
- Default pool size of 10 — bottleneck under load
**Testing:** Load test with 100 concurrent search requests
**Expected:** P95 search latency under 100ms with connection pool configured

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/08-vector-databases

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
