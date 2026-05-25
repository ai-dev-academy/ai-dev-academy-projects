# Topic 06 — LangChain4j AiServices

> **AI Dev Academy** · 🔒 Pro · Intermediate · 40 min
> Full interactive tutorial: [ai-dev-academy.com/topics/langchain4j-aiservices](https://ai-dev-academy.com)

---

## What You'll Build
A customer support bot with per-user memory using LangChain4j AiServices.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/06-langchain4j-aiservices

# 2. Add your API key
cp .env.example .env
# Open .env and set OPENAI_API_KEY=sk-proj-...

# 3. Run
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows

# 4. Verify
curl http://localhost:8080/support/health
```



## Test the API

```bash
curl -X POST "http://localhost:8080/support/chat?userId=alice" \
  -H "Content-Type: application/json" \
  -d "\"What topics does AI Dev Academy cover?\""
```

**Expected response:**
```json
{"response":"AI Dev Academy covers Spring AI, RAG, Agents...","userId":"alice"}
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /support/health | Health check |
| GET | /support/search | Search endpoint |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| 401 Unauthorized | Check your OPENAI_API_KEY in .env |
| Port 8080 in use | Add `server.port=8081` to application.properties |
| Build fails | Run `./mvnw dependency:resolve` |
| Java version error | Install Java 17+: `java -version` |

---
© 2025 Monika Digital LLC · [ai-dev-academy.com](https://ai-dev-academy.com)
