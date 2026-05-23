# Topic 10 — Advanced RAG Patterns

> **AI Dev Academy** · 🔒 Pro · Advanced · 55 min
> Full interactive tutorial: [aidevacademy.dev/topics/advanced-rag](https://aidevacademy.dev)

---

## What You'll Build
Advanced RAG with HyDE query expansion for better retrieval on vague questions.

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
cd ai-dev-academy-projects/10-advanced-rag

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
curl "http://localhost:8080/rag/hyde-search?q=how+does+memory+work+in+chatbots"
```

**Expected response:**
```json
{"question":"how does memory work?","hypothetical":"Memory in chatbots stores...","answer":"Based on retrieved context...","docsUsed":5}
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
