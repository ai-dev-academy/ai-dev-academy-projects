# Topic 12 — Conversation Memory

> **AI Dev Academy** · 🔒 Pro · Intermediate · 35 min
> Full interactive tutorial: [ai-dev-academy.com/topics/conversation-memory](https://ai-dev-academy.com)

---

## What You'll Build
A multi-user tutor bot where each student has isolated persistent memory.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/12-conversation-memory

# 2. Add your API key
cp .env.example .env
# Open .env and set OPENAI_API_KEY=sk-proj-...

# 3. Run
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows

# 4. Verify
curl http://localhost:8080/tutor/health
```



## Test the API

```bash
curl -X POST "http://localhost:8080/tutor/ask?studentId=alice" \
  -H "Content-Type: application/json" \
  -d "\"My name is Alice and I want to learn Java\""

curl -X POST "http://localhost:8080/tutor/ask?studentId=alice" \
  -H "Content-Type: application/json" \
  -d "\"What was my name again?\""
```

**Expected response:**
```json
{"answer":"Hello Alice! ...","studentId":"alice","messageCount":2}
{"answer":"Your name is Alice...","studentId":"alice","messageCount":4}
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /tutor/health | Health check |
| GET | /tutor/search | Search endpoint |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| 401 Unauthorized | Check your OPENAI_API_KEY in .env |
| Port 8080 in use | Add `server.port=8081` to application.properties |
| Build fails | Run `./mvnw dependency:resolve` |
| Java version error | Install Java 17+: `java -version` |

---
© 2025 Monika Digital LLC · [ai-dev-academy.com](https://ai-dev-academy.com)
