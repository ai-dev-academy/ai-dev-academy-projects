# 🧮 Embeddings Explained

> How text becomes vectors — enabling semantic search

**Tier:** Pro | **Duration:** 35 min | **Difficulty:** Intermediate | **Category:** RAG & Vector

**Tags:** Embeddings, Vectors, Cosine Similarity, Search

---

## Description

Understand embeddings, cosine similarity, and how vector search powers semantic retrieval.

---

## What You'll Learn

- What embedding vectors are
- Cosine similarity
- OpenAI text-embedding-3-small
- Build semantic search

---

## Tech Stack

- Spring AI
- OpenAI Embeddings
- pgvector
- Java 17

---

## Project

Product catalog semantic search

---

## Real-World Use Case

E-commerce search finding "running shoes" for "shoes for jogging"

---

## Prompt Guide

For embeddings no LLM prompt needed — call embedding model directly on raw text.

---

## Steps

### Step 1: Configure embedding model

**Why:** The embedding model converts text to vectors. Different models produce different quality embeddings.

**What:** Configure text-embedding-3-small in Spring AI.
**Files:** application.properties
```
No new files
```
```
spring.ai.openai.embedding.model=text-embedding-3-small
```
**Common Mistakes:**
- Using chat model for embeddings — wrong endpoint
**Testing:** Call embeddingModel.embed("test") — verify 1536-dim float array
**Expected:** Array of 1536 floats representing semantic meaning of "test"

---

### Step 2: Embed product catalog

**Why:** Each product description becomes a vector. Similar products end up near each other in vector space.

**What:** Loop through products and embed each description.
**Files:** EmbeddingService.java
```
service/EmbeddingService.java
```
```
public void indexProducts(List<Product> products) {
  products.forEach(p -> {
    float[] vector = embeddingModel.embed(p.getDescription());
    vectorStore.save(p.getId(), vector, p.getMetadata());
  });
}
```
**Common Mistakes:**
- Re-embedding unchanged products — cache embeddings in DB
**Testing:** Index 100 products — check vectorStore has 100 entries
**Expected:** All products indexed with unique 1536-dim vectors

---

### Step 3: Build similarity search

**Why:** Cosine similarity finds vectors pointing in the same "direction" = semantically similar content.

**What:** Embed the search query, find top-5 closest products.
**Files:** SearchService.java
```
service/SearchService.java
```
```
public List<Product> search(String query, int topK) {
  float[] queryVector = embeddingModel.embed(query);
  return vectorStore.findSimilar(queryVector, topK);
}
```
**Common Mistakes:**
- Using Euclidean distance instead of cosine for text — wrong metric
**Testing:** Search "shoes for running" — verify running shoes appear before dress shoes
**Expected:** Semantically related products rank higher than keyword matches

---

### Step 4: Create search endpoint

**Why:** Expose search as REST API for frontend consumption.

**What:** Build /search endpoint with query parameter.
**Files:** SearchController.java
```
controller/SearchController.java
```
```
@GetMapping("/search")
public List<Product> search(@RequestParam String q) {
  return searchService.search(q, 5);
}
```
**Common Mistakes:**
- No pagination — returning all matches tanks performance
**Testing:** GET /search?q=comfortable+office+chair — verify relevant results
**Expected:** Top 5 semantically relevant products returned in <500ms

---

### Step 5: Compare keyword vs semantic

**Why:** Proving semantic search value convinces stakeholders. Show the difference with hard examples.

**What:** Create a comparison endpoint returning both result sets.
**Files:** ComparisonController.java
```
controller/ComparisonController.java
```
```
@GetMapping("/compare")
public Map<String, List<Product>> compare(@RequestParam String q) {
  return Map.of(
    "keyword", keywordSearch(q),
    "semantic", semanticSearch(q)
  );
}
```
**Common Mistakes:**
- Not normalizing keyword search for fair comparison
**Testing:** Query "footwear for hiking" — keyword finds nothing, semantic finds hiking boots
**Expected:** Semantic returns relevant results for queries with no exact keyword match

---

### Step 6: Measure search quality

**Why:** Without metrics you can't improve. Precision@5 tells you how many of top 5 are relevant.

**What:** Build a simple evaluation framework with test queries.
**Files:** SearchEvaluation.java
```
test/SearchEvaluation.java
```
```
@Test
void measurePrecisionAt5() {
  var testCases = Map.of(
    "running footwear", Set.of("running-shoes","trail-runners"),
    "seating for office", Set.of("office-chair","ergonomic-chair")
  );
  // Calculate Precision@5 for each test case
}
```
**Common Mistakes:**
- Using only 1-2 test queries — not statistically meaningful
**Testing:** Run 20 test queries — measure Precision@5 for each
**Expected:** Semantic search achieves >80% Precision@5 vs <30% for keyword

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/07-embeddings-explained

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
