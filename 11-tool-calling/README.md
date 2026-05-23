# Topic 11 — Tool Calling

> **AI Dev Academy** · 🔒 Pro · Intermediate · 45 min
> Full interactive tutorial: [aidevacademy.dev/topics/tool-calling](https://aidevacademy.dev)

---

## What You'll Build
An order management agent that autonomously calls getStatus, cancelOrder, getDelivery tools.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/11-tool-calling

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
curl -X POST http://localhost:8080/agent/chat \
  -H "Content-Type: application/json" \
  -d "\"What is the status of order ORD-001? And when will it arrive?\""
```

**Expected response:**
```json
{"response":"Order ORD-001 is currently SHIPPED and estimated to arrive in 2-3 business days."}
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
© 2025 Monika Digital LLC · [aidevacademy.dev](https://aidevacademy.dev)
