# Topic 17 — Resilience Patterns

> **AI Dev Academy** · 🔒 Advanced · Advanced · 45 min
> Full interactive tutorial: [ai-dev-academy.com/topics/resilience-patterns](https://ai-dev-academy.com)

---

## What You'll Build
Production-hardened AI service with retry, circuit breaker, fallback, and semantic cache.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/17-resilience-patterns

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
curl -X POST http://localhost:8080/ai/chat \
  -H "Content-Type: application/json" \
  -d "\"What is a circuit breaker pattern?\""

curl http://localhost:8080/ai/cache-stats
```

**Expected response:**
```json
{"response":"A circuit breaker pattern..."}
{"hits":2,"misses":1,"cacheSize":1,"hitRate":"67%"}
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
