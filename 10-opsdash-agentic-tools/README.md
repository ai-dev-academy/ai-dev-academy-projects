# OpsDash — All 8 Agentic Tool-Use Techniques in One Spring Boot Project

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0-blue)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=java)](https://www.java.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)
[![Live Tutorial](https://img.shields.io/badge/Tutorial-ai--dev--academy.com-blue?logo=vercel)](https://ai-dev-academy.com/workflows)

> **Part of [AI Dev Academy](https://ai-dev-academy.com) — Workflow 02: Agentic / Tool-Use**

---

## What This Project Does

Most agentic AI tutorials show a chatbot that "uses tools". This project takes **one real production incident** and shows you **all 8 agentic tool-use techniques** applied to the same question — from a basic single @Tool call to a multi-agent delegation system.

**The scenario: OpsDash, an AI-powered incident investigation platform for microservices teams.**

**The question every on-call engineer has hit:**
> *"Checkout service latency spiked to 850ms after the 3pm deploy. Our SLA is 200ms. 14 microservices, Prometheus metrics, CloudWatch logs, GitHub deploy history. Root cause?"*

Without tools: the LLM guesses. With the right agentic technique: it calls real APIs, chains the evidence, and gives you a root cause in seconds.

---

## The 8 Techniques at a Glance

| # | Technique | What It Adds | Stars | Cost |
|---|-----------|--------------|-------|------|
| ① | **Basic @Tool** | LLM calls ONE tool — no more guessing | ⭐⭐ | 💰 |
| ② | **ReAct Agent** | Think→Act→Observe loop — chains tools | ⭐⭐⭐ | 💰💰 |
| ③ | **Parallel Tool Calling** | All tools fire simultaneously — 3× faster | ⭐⭐⭐⭐ | 💰💰 |
| ④ | **Memory Agent** | Remembers prior findings — no re-calling | ⭐⭐⭐ | 💰💰 |
| ⑤ | **Plan & Execute** | Plan first, execute second — auditable | ⭐⭐⭐⭐ | 💰💰💰 |
| ⑥ | **Self-Correcting** | Retries with adjusted params on failure | ⭐⭐⭐⭐ | 💰💰💰 |
| ⑦ | **Guardrailed** | Read-only free; write actions need approval | ⭐⭐⭐⭐⭐ | 💰💰 |
| ⑧ | **Multi-Agent** | 3 specialist agents + orchestrator | ⭐⭐⭐⭐⭐ | 💰💰💰💰 |

---

## Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/10-opsdash-agentic-tools

# 2. Set your OpenAI key
export OPENAI_API_KEY=sk-your-key-here

# 3. Run the Spring Boot application
mvn spring-boot:run

# 4. List available techniques
curl http://localhost:8080/incident/techniques

# 5. Try the same incident with each technique
curl -X POST http://localhost:8080/incident/ask/basic-tool \
  -H "Content-Type: text/plain" \
  -d "Checkout service latency spiked to 850ms after the 3pm deploy. Our SLA is 200ms. Root cause?"
```

---

## Endpoints

| Endpoint | Technique |
|----------|-----------|
| `POST /incident/ask/basic-tool` | ① Basic @Tool |
| `POST /incident/ask/react` | ② ReAct Agent |
| `POST /incident/ask/parallel` | ③ Parallel Tool Calling |
| `POST /incident/memory/start` | ④ Memory Agent — start session |
| `POST /incident/memory/{sessionId}` | ④ Memory Agent — continue session |
| `POST /incident/ask/plan-execute` | ⑤ Plan & Execute |
| `POST /incident/ask/self-correcting` | ⑥ Self-Correcting Agent |
| `POST /incident/ask/guardrailed` | ⑦ Guardrailed Agent |
| `POST /incident/ask/multi-agent` | ⑧ Multi-Agent Delegation |
| `GET /incident/techniques` | List all techniques |

All endpoints accept plain text body and return JSON: `{ "technique", "question", "answer" }`.

---

## Project Structure

```
src/main/java/com/aidevacademy/opsdash/
├── OpsDashApplication.java          # Spring Boot entry point
├── config/
│   └── AiConfig.java                # ChatClient bean
├── model/
│   ├── ServiceMetrics.java          # Prometheus metrics record
│   ├── Deploy.java                  # CI/CD deployment event record
│   └── SlowQuery.java               # RDS slow query record
├── tools/
│   └── OpsDashTools.java            # All 4 @Tool-annotated methods
├── service/
│   ├── BasicToolAgentService.java   # Technique ①
│   ├── ReActAgentService.java       # Technique ②
│   ├── ParallelToolAgentService.java # Technique ③
│   ├── MemoryAgentService.java      # Technique ④
│   ├── PlanAndExecuteService.java   # Technique ⑤
│   ├── SelfCorrectingAgentService.java # Technique ⑥
│   ├── GuardrailedAgentService.java # Technique ⑦
│   └── MultiAgentDelegationService.java # Technique ⑧
└── controller/
    └── IncidentController.java      # REST API
```

---

## The 4 Tools

All services share the same `OpsDashTools` class with 4 `@Tool`-annotated methods.
The `@Tool` description is what the LLM reads to decide **when** to call each tool.

| Tool | Simulates | Real Implementation |
|------|-----------|---------------------|
| `checkServiceMetrics(service)` | Prometheus / Datadog | `prometheusClient.queryRange(...)` |
| `getDeployHistory(service, hours)` | GitHub Actions / Argo CD | `githubClient.listWorkflowRuns(...)` |
| `querySlowLogs(service, since)` | RDS Performance Insights | `rdsInsights.getTopSqlByLatency(...)` |
| `getErrorLogs(service, since, level)` | CloudWatch Logs | `cloudwatchClient.filterLogEvents(...)` |

---

## Key Learning Points

1. **`@Tool` description quality matters** — write it like a function signature + docstring. The LLM reads it to decide when to call the tool.

2. **ReAct vs Parallel** — use ReAct when each result determines what to investigate next; use Parallel when you know all data sources upfront.

3. **Memory eliminates redundant tool calls** — in multi-turn sessions, tool results stay in context. Turn 3 calls 0 tools because everything is already known.

4. **Guardrails belong in the architecture, not the system prompt** — don't rely on "please don't do X" instructions. Remove write-tools from the LLM's tool catalog entirely.

5. **Multi-agent is about focus, not speed** — each specialist has a small context and a clear mandate. The orchestrator synthesizes without touching raw data.

---

## Requirements

- Java 17+
- Maven 3.8+
- OpenAI API key (GPT-4o or GPT-4o-mini)
- No database required — tools use in-memory stub data for demonstration

---

## Contributing

- ⭐ **[Star the repo](https://github.com/ai-dev-academy/ai-dev-academy-projects)** — helps more Java developers find this resource
- 🍴 **[Fork it](https://github.com/ai-dev-academy/ai-dev-academy-projects/fork)** — add a 9th technique or replace stubs with real API calls
- 💬 **[Open an issue](https://github.com/ai-dev-academy/ai-dev-academy-projects/issues)** — suggest an improvement or report a bug

---

Built with ♥ by [AI Dev Academy](https://ai-dev-academy.com) — Java Spring Boot AI Tutorials
