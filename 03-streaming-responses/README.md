# Topic 03 — Streaming Responses

> **AI Dev Academy** · 🟢 Free · Beginner · 25 min
> Full interactive tutorial: [aidevacademy.dev/topics/streaming-responses](https://aidevacademy.dev)

---

## What You'll Build
A live chat endpoint that streams tokens word-by-word using Server-Sent Events.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/03-streaming-responses

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
curl "http://localhost:8080/ai/stream?message=What+is+Java" \
  -H "Accept: text/event-stream"
```

**Expected response:**
```json
data: Java
data:  is
data:  a
data:  programming
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
