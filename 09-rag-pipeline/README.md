# Topic 09 — RAG Pipeline

> **AI Dev Academy** · 🔒 Pro · Intermediate · 60 min
> Full interactive tutorial: [aidevacademy.dev/topics/rag-pipeline](https://aidevacademy.dev)

---

## What You'll Build
A complete RAG system: upload any PDF, ask questions answered from its content.

## Extra Setup Required (Docker)
```bash
docker-compose up -d
```

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/09-rag-pipeline

# 2. Add your API key
cp .env.example .env
# Open .env and set OPENAI_API_KEY=sk-proj-...

# 3. Run
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows

# 4. Verify
curl http://localhost:8080/rag/health
```



## Test the API

```bash
curl -X POST http://localhost:8080/rag/ingest \
  -F "file=@your-document.pdf"

curl -X POST http://localhost:8080/rag/ask \
  -H "Content-Type: application/json" \
  -d "\"What is the main topic of this document?\""
```

**Expected response:**
```json
{"filename":"your-document.pdf","chunks":42,"status":"ingested successfully"}
{"question":"What is the main topic?","answer":"Based on the document, the main topic is..."}
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /rag/health | Health check |
| GET | /rag/search | Search endpoint |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| 401 Unauthorized | Check your OPENAI_API_KEY in .env |
| Port 8080 in use | Add `server.port=8081` to application.properties |
| Build fails | Run `./mvnw dependency:resolve` |
| Java version error | Install Java 17+: `java -version` |

---
© 2025 Monika Digital LLC · [aidevacademy.dev](https://aidevacademy.dev)
