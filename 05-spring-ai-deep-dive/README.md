# Topic 05 — Spring AI Deep Dive

> **AI Dev Academy** · 🟢 Free · Intermediate · 45 min
> Full interactive tutorial: [ai-dev-academy.com/topics/spring-ai-deep-dive](https://ai-dev-academy.com)

---

## What You'll Build
A multi-provider chatbot showing ChatClient, Advisors, and response timing.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/05-spring-ai-deep-dive

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
curl -X POST http://localhost:8080/ai/chat-with-logging \
  -H "Content-Type: application/json" \
  -d "\"Explain Spring AI in one sentence\""
```

**Expected response:**
```json
{"response":"Spring AI is...","latencyMs":"1234","model":"gpt-4o-mini"}
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
© 2025 Monika Digital LLC · [ai-dev-academy.com](https://ai-dev-academy.com)
