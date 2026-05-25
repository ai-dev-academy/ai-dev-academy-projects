# Topic 13 — AI Agents (ReAct)

> **AI Dev Academy** · 🔒 Advanced · Advanced · 60 min
> Full interactive tutorial: [ai-dev-academy.com/topics/ai-agents-react](https://ai-dev-academy.com)

---

## What You'll Build
A ReAct research agent that searches, saves notes, and compiles a final report autonomously.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/13-ai-agents-react

# 2. Add your API key
cp .env.example .env
# Open .env and set OPENAI_API_KEY=sk-proj-...

# 3. Run
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows

# 4. Verify
curl http://localhost:8080/agent/health
```



## Test the API

```bash
curl -X POST http://localhost:8080/agent/research \
  -H "Content-Type: application/json" \
  -d "\"Spring AI vs LangChain4j — key differences\""
```

**Expected response:**
```json
{"topic":"Spring AI vs LangChain4j","report":"Based on research...","timeMs":"8432"}
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /agent/health | Health check |
| GET | /agent/search | Search endpoint |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| 401 Unauthorized | Check your OPENAI_API_KEY in .env |
| Port 8080 in use | Add `server.port=8081` to application.properties |
| Build fails | Run `./mvnw dependency:resolve` |
| Java version error | Install Java 17+: `java -version` |

---
© 2025 Monika Digital LLC · [ai-dev-academy.com](https://ai-dev-academy.com)
