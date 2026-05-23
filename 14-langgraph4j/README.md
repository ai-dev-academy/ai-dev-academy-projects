# Topic 14 — LangGraph4j Workflows

> **AI Dev Academy** · 🔒 Advanced · Advanced · 70 min
> Full interactive tutorial: [aidevacademy.dev/topics/langgraph4j](https://aidevacademy.dev)

---

## What You'll Build
A 3-step content approval workflow: AI drafts → AI reviews score → publish or revise.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/14-langgraph4j

# 2. Add your API key
cp .env.example .env
# Open .env and set OPENAI_API_KEY=sk-proj-...

# 3. Run
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows

# 4. Verify
curl http://localhost:8080/workflow/health
```



## Test the API

```bash
curl -X POST http://localhost:8080/workflow/run \
  -H "Content-Type: application/json" \
  -d "\"Java virtual threads for high-performance apps\""
```

**Expected response:**
```json
{"topic":"Java virtual threads","draft":"...","reviewScore":8,"finalContent":"...","status":"published"}
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /workflow/health | Health check |
| GET | /workflow/search | Search endpoint |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| 401 Unauthorized | Check your OPENAI_API_KEY in .env |
| Port 8080 in use | Add `server.port=8081` to application.properties |
| Build fails | Run `./mvnw dependency:resolve` |
| Java version error | Install Java 17+: `java -version` |

---
© 2025 Monika Digital LLC · [aidevacademy.dev](https://aidevacademy.dev)
