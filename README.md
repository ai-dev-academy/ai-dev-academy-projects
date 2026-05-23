# AI Dev Academy — Spring Boot Projects

[![GitHub stars](https://img.shields.io/github/stars/ai-dev-academy/ai-dev-academy-projects?style=social)](https://github.com/ai-dev-academy/ai-dev-academy-projects/stargazers)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=java)](https://www.java.com)
[![Live Platform](https://img.shields.io/badge/Platform-ai--dev--academy.com-blue?logo=vercel)](https://ai-dev-academy.com)

> 20 complete Spring Boot projects for every topic at [ai-dev-academy.com](https://ai-dev-academy.com)
> Each project is standalone, runnable, and fully commented.

---

## How to Clone and Use

### Option A — Clone Everything (Recommended)
```bash
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects
```
You now have all 20 project folders. Each is a standalone Spring Boot app.

### Option B — Download ZIP (No Git Required)
1. Go to: `https://github.com/ai-dev-academy/ai-dev-academy-projects`
2. Click the green **Code** button
3. Click **Download ZIP**
4. Extract to your projects folder

### Option C — Clone a Single Topic Only
```bash
# Use sparse checkout to get just one topic
git clone --filter=blob:none --sparse \
  https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects
git sparse-checkout set 01-intro-to-llm-apis
```

---

## Quick Start (Any Topic)

```bash
# 1. Enter the topic folder
cd 01-intro-to-llm-apis

# 2. Set up your API key
cp .env.example .env
# Open .env in any text editor and set:
# OPENAI_API_KEY=sk-proj-your-key-here

# 3. Run it
./mvnw spring-boot:run       # Mac / Linux
mvnw.cmd spring-boot:run     # Windows

# 4. Test it (open a new terminal)
curl http://localhost:8080/ai/health
```

---

## All 20 Topics

| # | Folder | Topic | Access | Port | Health Check |
|---|--------|-------|--------|------|-------------|
| 01 | `01-intro-to-llm-apis`       | Intro to LLM APIs         | 🟢 Free     | 8080 | `GET /ai/health`       |
| 02 | `02-prompt-engineering`       | Prompt Engineering        | 🟢 Free     | 8080 | `GET /ai/health`       |
| 03 | `03-streaming-responses`      | Streaming Responses       | 🟢 Free     | 8080 | `GET /ai/health`       |
| 04 | `04-structured-output`        | Structured Output         | 🟢 Free     | 8080 | `GET /ai/health`       |
| 05 | `05-spring-ai-deep-dive`      | Spring AI Deep Dive       | 🟢 Free     | 8080 | `GET /ai/health`       |
| 06 | `06-langchain4j-aiservices`   | LangChain4j AiServices    | 🔒 Pro      | 8080 | `GET /support/health`  |
| 07 | `07-embeddings-explained`     | Embeddings Explained      | 🔒 Pro      | 8080 | `GET /ai/health`       |
| 08 | `08-vector-databases`         | Vector Databases          | 🔒 Pro      | 8080 | `GET /ai/health`       |
| 09 | `09-rag-pipeline`             | RAG Pipeline              | 🔒 Pro      | 8080 | `GET /rag/health`      |
| 10 | `10-advanced-rag`             | Advanced RAG              | 🔒 Pro      | 8080 | `GET /rag/health`      |
| 11 | `11-tool-calling`             | Tool Calling              | 🔒 Pro      | 8080 | `GET /agent/health`    |
| 12 | `12-conversation-memory`      | Conversation Memory       | 🔒 Pro      | 8080 | `GET /tutor/health`    |
| 13 | `13-ai-agents-react`          | AI Agents (ReAct)         | 🔒 Advanced | 8080 | `GET /agent/health`    |
| 14 | `14-langgraph4j`              | LangGraph4j Workflows     | 🔒 Advanced | 8080 | `GET /workflow/health` |
| 15 | `15-model-context-protocol`   | Model Context Protocol    | 🔒 Advanced | 8080 | `GET /mcp/health`      |
| 16 | `16-multi-agent-systems`      | Multi-Agent Systems       | 🔒 Advanced | 8080 | `GET /pipeline/health` |
| 17 | `17-resilience-patterns`      | Resilience Patterns       | 🔒 Advanced | 8080 | `GET /ai/health`       |
| 18 | `18-cost-token-control`       | Cost & Token Control      | 🔒 Advanced | 8080 | `GET /ai/health`       |
| 19 | `19-ai-observability`         | AI Observability          | 🔒 Advanced | 8080 | `GET /ai/health`       |
| 20 | `20-rag-evaluation`           | RAG Evaluation (RAGAS)    | 🔒 Advanced | 8080 | `GET /eval/health`     |

---

## Topics That Need Docker

Topics 08, 09, 10, 20 use **pgvector** (PostgreSQL with vector support). Run Docker first:

```bash
# From the topic folder (e.g. 08-vector-databases)
docker-compose up -d

# Verify it's running
docker ps
# You should see: pgvector/pgvector:pg16 ... 0.0.0.0:5432->5432/tcp
```

**Don't have Docker?** Download from [docker.com/get-started](https://docker.com/get-started)

---

## Requirements

| Tool | Version | Check | Download |
|------|---------|-------|----------|
| Java JDK | 17 or higher | `java -version` | [adoptium.net](https://adoptium.net) |
| Maven | Included in each project via `mvnw` | `./mvnw -v` | Not needed |
| Docker | Any recent version | `docker -v` | [docker.com](https://docker.com) |
| OpenAI Key | Any | — | [platform.openai.com/api-keys](https://platform.openai.com/api-keys) |

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `401 Unauthorized` | API key is wrong or missing. Check `.env` file |
| `Port 8080 already in use` | Add `server.port=8081` to `application.properties` |
| `Cannot resolve dependencies` | Run `./mvnw dependency:resolve` |
| `Java version error` | Install Java 17+. Check `java -version` |
| `Docker connection refused` | Run `docker-compose up -d` from topic folder |
| `./mvnw: Permission denied` | Run `chmod +x mvnw` then retry |
| Windows `mvnw.cmd` not found | Use `.\mvnw.cmd spring-boot:run` in PowerShell |

---

## License

MIT — free to use for learning and personal projects.
© 2025 Monika Digital LLC · [aidevacademy.dev](https://aidevacademy.dev)
