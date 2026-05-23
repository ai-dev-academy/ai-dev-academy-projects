# Topic 08 — Vector Databases

> **AI Dev Academy** · 🔒 Pro · Intermediate · 40 min
> Full interactive tutorial: [aidevacademy.dev/topics/vector-databases](https://aidevacademy.dev)

---

## What You'll Build
A pgvector-backed store and search system for semantic document retrieval.
  
## Extra Setup Required (Docker)
```bash
docker-compose up -d   # starts PostgreSQL with pgvector
```

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/08-vector-databases

# 2. Add your API key
cp .env.example .env
# Open .env and set OPENAI_API_KEY=sk-proj-...

# 3. Run
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows

# 4. Verify
curl http://localhost:8080/ai/health
```



## Test the API

```bash
curl -X POST http://localhost:8080/ai/store \
  -H "Content-Type: application/json" \
  -d "[\"Spring AI is a Java framework for LLM integration\"]"

curl "http://localhost:8080/ai/search?q=Java+AI+framework"
```

**Expected response:**
```json
{"stored":1,"status":"success"}
[{"content":"Spring AI is a Java framework...","metadata":{}}]
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /ai/health | Health check |
| GET | /ai/search | Search endpoint |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| 401 Unauthorized | Check your OPENAI_API_KEY in .env |
| Port 8080 in use | Add `server.port=8081` to application.properties |
| Build fails | Run `./mvnw dependency:resolve` |
| Java version error | Install Java 17+: `java -version` |

---
© 2025 Monika Digital LLC · [aidevacademy.dev](https://aidevacademy.dev)
