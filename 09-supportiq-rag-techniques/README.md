# SupportIQ — All 8 RAG Techniques in One Spring Boot Project

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0-blue)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-21%2B-orange?logo=java)](https://www.java.com)
[![pgvector](https://img.shields.io/badge/pgvector-PostgreSQL-336791)](https://github.com/pgvector/pgvector)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)
[![Live Tutorial](https://img.shields.io/badge/Tutorial-ai--dev--academy.com-blue?logo=vercel)](https://ai-dev-academy.com/workflows)

> **Part of [AI Dev Academy](https://ai-dev-academy.com) — Topic 09: RAG Workflow**

---

## What This Project Does

Most RAG tutorials show **one technique in isolation**.

This project takes **one real-world scenario** — SupportIQ, an AI customer support assistant — and shows you **all 8 RAG techniques** applied to the exact same question, from the simplest to the most powerful.

**The question every Java developer has hit:**
> *"I'm getting a NullPointerException when the LLM returns an empty response. How do I fix this and make my AI integration production-safe?"*

You will see how each technique handles it — and measurably why each one gets better.

---

## The 8 Techniques at a Glance

| # | Technique | What It Fixes | Answer Quality | Cost |
|---|-----------|---------------|----------------|------|
| 1 | **Basic RAG** | No grounding = hallucination | ⭐⭐ | 💲 |
| 2 | **Hybrid RAG** | Misses exact terms like "NullPointerException" | ⭐⭐⭐ | 💲💲 |
| 3 | **Multi-Query RAG** | One phrasing misses relevant docs | ⭐⭐⭐ | 💲💲 |
| 4 | **HyDE RAG** | Short queries under-embed | ⭐⭐⭐⭐ | 💲💲 |
| 5 | **Parent-Document RAG** | Snippet lacks surrounding code context | ⭐⭐⭐⭐ | 💲💲💲 |
| 6 | **Contextual Compression** | Retrieved docs have too much noise | ⭐⭐⭐⭐ | 💲 |
| 7 | **Re-ranking RAG** | Top result isn't always the best result | ⭐⭐⭐⭐⭐ | 💲💲 |
| 8 | **Agentic RAG** | One search can't answer everything | ⭐⭐⭐⭐⭐ | 💲💲💲💲 |

---

## The Scenario: SupportIQ

SupportIQ is an AI-powered customer support assistant for a SaaS platform with **10 years of knowledge**:

```
📚 Knowledge Sources
├── 3,200  Documentation pages
├── 12,000 Resolved support tickets
├── 890    GitHub issues + solutions
├── 420    How-to blog posts
└── 50     Architecture decision records
```

All indexed into **pgvector** (PostgreSQL). Same question, 8 different RAG strategies — watch the answers improve.

---

## Project Structure

```
09-supportiq-rag-techniques/
├── src/main/java/com/aidevacademy/supportiq/
│   ├── techniques/
│   │   ├── BasicRagService.java              # Technique 1
│   │   ├── HybridRagService.java             # Technique 2
│   │   ├── MultiQueryRagService.java         # Technique 3
│   │   ├── HydeRagService.java               # Technique 4
│   │   ├── ParentDocumentRagService.java     # Technique 5
│   │   ├── ContextualCompressionService.java # Technique 6
│   │   ├── RerankingRagService.java          # Technique 7
│   │   └── AgenticRagService.java            # Technique 8
│   ├── ingestion/
│   │   └── DocumentIngestionService.java     # Load sample docs into pgvector
│   ├── config/
│   │   ├── VectorStoreConfig.java
│   │   └── EmbeddingConfig.java
│   └── controller/
│       └── RagCompareController.java         # Run all 8, compare results
├── data/
│   └── sample-docs/                          # 50 sample support documents
├── docker-compose.yml                        # PostgreSQL + pgvector
├── .env.example
└── README.md
```

---

## Prerequisites

- Java 21+
- Docker Desktop (for pgvector + PostgreSQL)
- OpenAI API key (or Azure OpenAI)
- Optional: Cohere API key (for Technique 7 Re-ranking)

---

## Quick Start

### Step 1 — Clone the repo
```bash
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/09-supportiq-rag-techniques
```

### Step 2 — Start the database
```bash
docker-compose up -d
# Starts PostgreSQL with pgvector extension on port 5432
```

### Step 3 — Configure API keys
```bash
cp .env.example .env
# Edit .env and add your keys:
```

```properties
OPENAI_API_KEY=sk-...
COHERE_API_KEY=...         # optional, needed for Technique 7 only
```

### Step 4 — Load sample data
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--load-data=true"
# Loads 50 sample support documents into pgvector
```

### Step 5 — Run a technique
```bash
# Compare all 8 techniques with the same question
curl -X POST http://localhost:8080/rag/compare \
  -H "Content-Type: application/json" \
  -d '{"question": "NullPointerException when LLM returns empty response"}'
```

### Step 6 — Run a specific technique
```bash
# Run just Technique 7 (Re-ranking)
curl -X POST http://localhost:8080/rag/reranking \
  -H "Content-Type: application/json" \
  -d '{"question": "NullPointerException when LLM returns empty response"}'
```

---

## What You Will Learn

- ✅ Why Basic RAG misses exact technical terms and how Hybrid RAG fixes it
- ✅ How Multi-Query RAG generates 4 query variations and deduplicates results
- ✅ Why short questions embed poorly and how HyDE solves it with hypothetical answers
- ✅ How Parent-Document RAG retrieves full sections, not just 2-sentence snippets
- ✅ How Contextual Compression cuts token cost by 70%+ without losing answer quality
- ✅ Why cosine similarity ≠ relevance, and how Cohere Rerank fixes the ranking
- ✅ How Agentic RAG loops over multiple tools until it has enough to answer confidently

---

## Common Pitfalls (avoid these)

| Pitfall | Fix |
|---------|-----|
| Chunk size too large (>500 tokens) | Use 150-200 token chunks with 20-token overlap |
| No metadata filtering | Filter by date, category, product at search time |
| Skipping re-ranking | Add Cohere Rerank — cosine similarity ≠ relevance |
| No faithfulness check | Validate with RAGAS in CI/CD (see Topic 20) |
| HyDE steering wrong | Only use HyDE when basic RAG consistently under-retrieves |
| Agentic infinite loops | Always set `maxSteps=6` and a token budget |

---

## Related Topics on AI Dev Academy

| Topic | Link |
|-------|------|
| 07 — Embeddings Explained | [ai-dev-academy.com/topics/embeddings-explained](https://ai-dev-academy.com/topics/embeddings-explained) |
| 08 — Vector Databases | [ai-dev-academy.com/topics/vector-databases](https://ai-dev-academy.com/topics/vector-databases) |
| 09 — RAG Pipeline | [ai-dev-academy.com/topics/rag-pipeline](https://ai-dev-academy.com/topics/rag-pipeline) |
| 10 — Advanced RAG | [ai-dev-academy.com/topics/advanced-rag](https://ai-dev-academy.com/topics/advanced-rag) |
| 20 — RAG Evaluation | [ai-dev-academy.com/topics/rag-evaluation](https://ai-dev-academy.com/topics/rag-evaluation) |
| RAG Workflow (free) | [ai-dev-academy.com/workflows](https://ai-dev-academy.com/workflows) |

---

## ⭐ Found This Useful?

If this project helped you understand RAG, please:

- ⭐ **[Star this repo](https://github.com/ai-dev-academy/ai-dev-academy-projects)** — helps other Java developers find it on GitHub
- 🍴 **[Fork it](https://github.com/ai-dev-academy/ai-dev-academy-projects/fork)** — build your own RAG implementation on top
- 💬 **[Open an issue](https://github.com/ai-dev-academy/ai-dev-academy-projects/issues)** — suggest a 9th technique or report a bug

Every star helps more Java developers discover this resource. Thank you! 🙏

---

Built with ❤️ by [AI Dev Academy](https://ai-dev-academy.com) — Java Spring Boot AI Tutorials
