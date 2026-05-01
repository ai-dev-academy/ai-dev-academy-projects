# 🚀 Advanced RAG Patterns

> Hybrid search, re-ranking, HyDE and parent-child chunks

**Tier:** Pro | **Duration:** 55 min | **Difficulty:** Advanced | **Category:** RAG & Vector

**Tags:** Hybrid Search, Re-ranking, HyDE, Cohere

---

## Description

Go beyond basic RAG with hybrid search, Cohere Rerank, HyDE and parent-child chunking.

---

## What You'll Learn

- Hybrid BM25+cosine
- Cohere Rerank
- HyDE query expansion
- Parent-child chunks

---

## Tech Stack

- pgvector
- Cohere Rerank
- Spring AI
- Java 17

---

## Project

Enterprise knowledge base with hybrid search

---

## Real-World Use Case

Tech support finding right answer even when users describe problems vaguely

---

## Prompt Guide

For HyDE: "Generate a hypothetical document that answers: {question}". Embed that instead of raw query.

---

## Steps

### Step 1: Implement BM25 keyword search

**Why:** Pure vector search misses exact keyword matches. BM25 catches what semantic search misses.

**What:** Add PostgreSQL full-text search alongside pgvector.
**Files:** HybridSearchService.java
```
service/HybridSearchService.java
```
```
// BM25 keyword search
List<Document> keywordResults = jdbcTemplate.query(
  "SELECT * FROM vector_store WHERE to_tsvector(content) @@ plainto_tsquery(?)",
  new Object[]{query}, docRowMapper
);

// Vector semantic search
List<Document> vectorResults = vectorStore.similaritySearch(query);
```
**Common Mistakes:**
- Treating both result sets as independent — must fuse them
**Testing:** Query "login bug" — BM25 finds exact match, vector finds similar issues
**Expected:** Both result sets populated, ready for fusion

---

### Step 2: Reciprocal Rank Fusion

**Why:** RRF combines rankings from multiple sources without needing scores on the same scale.

**What:** Implement RRF to merge BM25 and vector search results.
**Files:** HybridSearchService.java
```
No new files
```
```
public List<Document> hybridSearch(String query) {
  var keyword = bm25Search(query);
  var semantic = vectorSearch(query);
  
  // RRF score = sum of 1/(rank + 60) for each result list
  return mergeWithRRF(keyword, semantic, 60);
}
```
**Common Mistakes:**
- Simple list concat instead of proper RRF — duplicates not merged
**Testing:** Document appearing in both lists should rank higher than single-list docs
**Expected:** Hybrid results measurably better than either search alone on test queries

---

### Step 3: Add Cohere Rerank

**Why:** LLMs are better at relevancy judgment than vector similarity. Reranking improves Precision@5 by 20-40%.

**What:** Call Cohere Rerank API on top-20 results to reorder by true relevance.
**Files:** RerankService.java
```
service/RerankService.java
```
```
public List<Document> rerank(String query, List<Document> candidates) {
  var request = RerankRequest.builder()
    .model("rerank-english-v3.0")
    .query(query)
    .documents(candidates.stream().map(Document::getContent).toList())
    .topN(5)
    .build();
  return cohereClient.rerank(request).getResults();
}
```
**Common Mistakes:**
- Reranking only top-5 — rerank top-20 then take top-5
**Testing:** Compare Precision@5 before/after reranking on 20 test queries
**Expected:** Reranking improves Precision@5 from ~70% to ~90%

---

### Step 4: Implement HyDE

**Why:** Short vague queries embed poorly. HyDE generates a hypothetical answer and embeds that instead — much better matches.

**What:** Generate hypothetical document then embed it for retrieval.
**Files:** HydeService.java
```
service/HydeService.java
```
```
public List<Document> hydeSearch(String query) {
  // Generate hypothetical answer
  String hypothetical = chatClient.prompt()
    .user("Write a brief paragraph that would answer: " + query)
    .call().content();
  
  // Embed the hypothetical answer, not the query
  return vectorStore.similaritySearch(hypothetical);
}
```
**Common Mistakes:**
- Forgetting to tell LLM to keep hypothetical brief — long = poor embedding
**Testing:** Query "crashes on startup" — HyDE generates better context than raw query
**Expected:** HyDE retrieval improves recall by 15-25% on vague queries

---

### Step 5: Parent-child chunking

**Why:** Small chunks retrieve precisely. Large chunks give LLM enough context. Parent-child gives you both.

**What:** Ingest with small child chunks, retrieve parents for generation.
**Files:** ParentChildSplitter.java
```
service/ParentChildSplitter.java
```
```
// Split into parents (512 tokens) and children (128 tokens)
List<Document> parents = parentSplitter.apply(docs);
List<Document> children = childSplitter.apply(docs);

// Link children to parents via metadata
children.forEach(child -> child.getMetadata().put("parentId", getParentId(child)));

// Store children for retrieval, retrieve parent for LLM
```
**Common Mistakes:**
- Storing parent chunks for retrieval — defeats the purpose
**Testing:** Retrieve child chunk — verify parent retrieved for LLM contains full context
**Expected:** LLM receives 512-token parent while search used 128-token child for precision

---

### Step 6: Measure improvement

**Why:** Quantify every optimization — stakeholders need numbers not impressions.

**What:** Build evaluation pipeline comparing baseline vs advanced RAG.
**Files:** AdvancedRagEval.java
```
test/AdvancedRagEval.java
```
```
// Compare strategies on same test set
Map<String, Double> scores = Map.of(
  "baseline", evaluateRag(baselineRag, testSet),
  "hybrid",   evaluateRag(hybridRag, testSet),
  "reranked", evaluateRag(rerankedRag, testSet),
  "hyde",     evaluateRag(hydeRag, testSet)
);
```
**Common Mistakes:**
- Testing on training data — use held-out test set
**Testing:** Run evaluation on 50 held-out Q&A pairs
**Expected:** Advanced RAG scores 85%+ vs baseline 65% faithfulness

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/10-advanced-rag

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
