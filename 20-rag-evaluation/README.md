# Topic 20 — RAG Evaluation (RAGAS)

> **AI Dev Academy** · 🔒 Advanced · Advanced · 45 min
> Full interactive tutorial: [aidevacademy.dev/topics/rag-evaluation](https://aidevacademy.dev)

---

## What You'll Build
Automated RAG quality evaluation: faithfulness scoring on a golden Q&A test set.

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
cd ai-dev-academy-projects/20-rag-evaluation

# 2. Add your API key
cp .env.example .env
# Open .env and set OPENAI_API_KEY=sk-proj-...

# 3. Run
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows

# 4. Verify
curl http://localhost:8080/eval/health
```



## Test the API

```bash
curl -X POST http://localhost:8080/eval/seed

curl http://localhost:8080/eval/run

curl -X POST http://localhost:8080/eval/ask \
  -H "Content-Type: application/json" \
  -d "\"What is Spring AI?\""
```

**Expected response:**
```json
{"seeded":5,"status":"Knowledge base ready for evaluation"}
{"totalQuestions":5,"passed":5,"keywordAccuracy":"100%","avgFaithfulness":"0.87","meetsThreshold":true}
{"answer":"Spring AI is a Java framework by Broadcom/VMware..."}
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /eval/health | Health check |
| GET | /eval/search | Search endpoint |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| 401 Unauthorized | Check your OPENAI_API_KEY in .env |
| Port 8080 in use | Add `server.port=8081` to application.properties |
| Build fails | Run `./mvnw dependency:resolve` |
| Java version error | Install Java 17+: `java -version` |

---
© 2025 Monika Digital LLC · [aidevacademy.dev](https://aidevacademy.dev)
