# Topic 15 — Model Context Protocol

> **AI Dev Academy** · 🔒 Advanced · Advanced · 55 min
> Full interactive tutorial: [aidevacademy.dev/topics/model-context-protocol](https://aidevacademy.dev)

---

## What You'll Build
An MCP server exposing inventory domain tools that any LLM client can discover and call.

## Requirements
- Java 17+  ·  Maven (included via `mvnw`)  ·  OpenAI API key

## Quick Start

```bash
# 1. Clone the full monorepo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/15-model-context-protocol

# 2. Add your API key
cp .env.example .env
# Open .env and set OPENAI_API_KEY=sk-proj-...

# 3. Run
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows

# 4. Verify
curl http://localhost:8080/mcp/health
```



## Test the API

```bash
curl http://localhost:8080/mcp/tools
curl http://localhost:8080/mcp/inventory/SKU-001
curl http://localhost:8080/mcp/low-stock?threshold=20
```

**Expected response:**
```json
[{"name":"getInventory","description":"Get inventory level..."}]
{"sku":"SKU-001","quantity":45,"status":"in_stock"}
["SKU-002 (qty: 0)"]
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /mcp/health | Health check |
| GET | /mcp/search | Search endpoint |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| 401 Unauthorized | Check your OPENAI_API_KEY in .env |
| Port 8080 in use | Add `server.port=8081` to application.properties |
| Build fails | Run `./mvnw dependency:resolve` |
| Java version error | Install Java 17+: `java -version` |

---
© 2025 Monika Digital LLC · [aidevacademy.dev](https://aidevacademy.dev)
