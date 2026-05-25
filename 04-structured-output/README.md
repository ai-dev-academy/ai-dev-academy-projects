# Topic 04 — Structured Output

> **AI Dev Academy** · 🟢 Free · Beginner · 20 min
> Full interactive tutorial: [ai-dev-academy.com/topics/structured-output](https://ai-dev-academy.com)

---

## What You'll Build
An invoice parser that extracts vendor, amount, date into a typed Java record.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/04-structured-output

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
curl -X POST http://localhost:8080/ai/extract \
  -H "Content-Type: application/json" \
  -d "\"Invoice from Acme Corp. Amount: \$1234.56 Date: 2025-01-15 Invoice#: INV-999\""
```

**Expected response:**
```json
{"vendor":"Acme Corp","amount":"1234.56","currency":"USD","date":"2025-01-15","invoiceNumber":"INV-999","lineItems":[]}
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
