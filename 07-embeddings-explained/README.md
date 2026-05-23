# Topic 07 — Embeddings Explained

> **AI Dev Academy** · 🔒 Pro · Intermediate · 35 min
> Full interactive tutorial: [aidevacademy.dev/topics/embeddings-explained](https://aidevacademy.dev)

---

## What You'll Build
A semantic product search that finds relevant products by meaning, not keywords.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/07-embeddings-explained

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
curl "http://localhost:8080/ai/search?q=shoes+for+running&topK=3"
```

**Expected response:**
```json
[{"product":"Nike Air Max running shoes...","score":0.94},{"product":"Hiking boots...","score":0.81}]
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
