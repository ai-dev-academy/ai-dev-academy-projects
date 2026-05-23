# Topic 16 — Multi-Agent Systems

> **AI Dev Academy** · 🔒 Advanced · Advanced · 75 min
> Full interactive tutorial: [aidevacademy.dev/topics/multi-agent-systems](https://aidevacademy.dev)

---

## What You'll Build
A 3-agent pipeline: Researcher + Writer + Editor collaborate to produce polished content.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/16-multi-agent-systems

# 2. Add your API key
cp .env.example .env
# Open .env and set OPENAI_API_KEY=sk-proj-...

# 3. Run
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows

# 4. Verify
curl http://localhost:8080/pipeline/health
```



## Test the API

```bash
curl -X POST http://localhost:8080/pipeline/run \
  -H "Content-Type: application/json" \
  -d "\"Spring Boot virtual threads\""

curl -X POST http://localhost:8080/pipeline/parallel \
  -H "Content-Type: application/json" \
  -d "\"Java 21 features\""
```

**Expected response:**
```json
{"topic":"Spring Boot virtual threads","research":"1. Virtual threads...","draft":"...","final":"...","durationMs":6230}
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /pipeline/health | Health check |
| GET | /pipeline/search | Search endpoint |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| 401 Unauthorized | Check your OPENAI_API_KEY in .env |
| Port 8080 in use | Add `server.port=8081` to application.properties |
| Build fails | Run `./mvnw dependency:resolve` |
| Java version error | Install Java 17+: `java -version` |

---
© 2025 Monika Digital LLC · [aidevacademy.dev](https://aidevacademy.dev)
