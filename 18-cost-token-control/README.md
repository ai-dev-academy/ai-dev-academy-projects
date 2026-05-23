# Topic 18 — Cost and Token Control

> **AI Dev Academy** · 🔒 Advanced · Advanced · 40 min
> Full interactive tutorial: [aidevacademy.dev/topics/cost-token-control](https://aidevacademy.dev)

---

## What You'll Build
Per-request token counting, cost tracking, and Prometheus metrics for AI spend.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/18-cost-token-control

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
  -d "\"What is tokenization?\""

curl http://localhost:8080/ai/costs
```

**Expected response:**
```json
{"response":"Tokenization is...","inputTokens":12,"outputTokens":67,"costUsd":"$0.000347","latencyMs":1203}
{"totalCostUsd":"$0.0003","costByEndpoint":{"/ai/chat":"$0.0003"},"requestsByEndpoint":{"/ai/chat":1}}
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
