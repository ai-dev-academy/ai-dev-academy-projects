# LoanIQ — All 8 Chain-of-Thought Techniques in One Spring Boot Project

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0-blue)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=java)](https://www.java.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)
[![Live Tutorial](https://img.shields.io/badge/Tutorial-ai--dev--academy.com-blue?logo=vercel)](https://ai-dev-academy.com/workflows)

> **Part of [AI Dev Academy](https://ai-dev-academy.com) — Workflow 03: Chain-of-Thought (CoT)**

---

## What This Project Does

Most CoT tutorials show the technique working perfectly. This project shows you **why each variation exists** by running one real borderline loan application through all 8 techniques — and demonstrating exactly what each technique fixes that the previous one couldn't.

**The scenario: LoanIQ, an AI-powered loan underwriting platform processing 800 applications/day.**

**The question every fintech developer has hit:**
> *"Application #4821: salary $72K, credit score 618, 3 missed payments 2 years ago, $18K credit card debt, 2 years at current employer. Approve, conditionally approve, or reject — and show your reasoning for the audit trail."*

The catch: without CoT the LLM gives a one-liner. With Zero-Shot CoT it reasons — but estimates DTI at ~9% when the verified number is 32.3%. By Technique ⑦ it calls your database and gets it right.

---

## The 8 Techniques at a Glance

| # | Technique | What It Fixes Over Previous | Stars | Cost | LLM Calls |
|---|-----------|---------------------------|-------|------|-----------|
| ① | **Zero-Shot CoT** | No reasoning → 4-step trace | ⭐⭐ | 💰 | 1 |
| ② | **Few-Shot CoT** | Format varies → always parseable | ⭐⭐⭐ | 💰 | 1 |
| ③ | **Self-Consistency** | Variance → majority vote confidence | ⭐⭐⭐⭐ | 💰💰💰 | 5 |
| ④ | **Structured CoT** | Free-text → DB-insertable schema | ⭐⭐⭐ | 💰 | 1 |
| ⑤ | **Tree of Thoughts** | Single path → 3 branches, best wins | ⭐⭐⭐⭐⭐ | 💰💰💰💰 | 7 |
| ⑥ | **Step-Back Prompting** | Missing principles → rules cited first | ⭐⭐⭐ | 💰💰 | 2 |
| ⑦ | **CoT + Tool Calls** | Estimated DTI 25% → verified 32.3% | ⭐⭐⭐⭐ | 💰💰💰 | 1+tools |
| ⑧ | **CoT + Self-Critique** | Self-contradictions → caught and corrected | ⭐⭐⭐⭐⭐ | 💰💰💰💰 | 3 |

---

## Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/11-loaniq-chain-of-thought

# 2. Set your OpenAI key
export OPENAI_API_KEY=sk-your-key-here

# 3. Run the Spring Boot application
mvn spring-boot:run

# 4. List available techniques
curl http://localhost:8080/underwriting/techniques

# 5. Run Application #4821 through all 8 techniques (no body needed)
curl http://localhost:8080/underwriting/demo/zero-shot
curl http://localhost:8080/underwriting/demo/grounded    # see the 25% vs 32.3% DTI difference
curl http://localhost:8080/underwriting/demo/self-critique

# 6. Or POST your own application summary
curl -X POST http://localhost:8080/underwriting/evaluate/zero-shot \
  -H "Content-Type: text/plain" \
  -d "Application: income $72K, credit score 618, 3 missed payments 2yr ago, 2yr employment"
```

---

## Endpoints

| Endpoint | Technique |
|----------|-----------|
| `GET  /underwriting/demo/{technique}` | Run built-in Application #4821 demo |
| `POST /underwriting/evaluate/zero-shot` | ① Zero-Shot CoT |
| `POST /underwriting/evaluate/few-shot` | ② Few-Shot CoT |
| `POST /underwriting/evaluate/self-consistency` | ③ Self-Consistency CoT |
| `POST /underwriting/evaluate/structured` | ④ Structured CoT |
| `POST /underwriting/evaluate/tree-of-thoughts` | ⑤ Tree of Thoughts |
| `POST /underwriting/evaluate/step-back` | ⑥ Step-Back Prompting |
| `POST /underwriting/evaluate/grounded` | ⑦ CoT + Tool Calls |
| `POST /underwriting/evaluate/self-critique` | ⑧ CoT + Self-Critique |
| `GET  /underwriting/techniques` | List all techniques |

---

## Project Structure

```
src/main/java/com/aidevacademy/loaniq/
├── LoanIqApplication.java              # Spring Boot entry point
├── config/
│   └── AiConfig.java                   # ChatClient bean
├── model/
│   ├── LoanApplication.java            # Application data record
│   ├── DtiResult.java                  # Verified DTI from DB
│   ├── CreditTier.java                 # Credit tier + default rate
│   └── PaymentTrend.java               # Payment history analysis
├── tools/
│   ├── UnderwritingTools.java          # 3 @Tool-annotated methods
│   └── SampleApplications.java        # Built-in demo applications
├── service/
│   ├── ZeroShotCoTService.java         # Technique ①
│   ├── FewShotCoTService.java          # Technique ②
│   ├── SelfConsistencyCoTService.java  # Technique ③
│   ├── StructuredCoTService.java       # Technique ④
│   ├── TreeOfThoughtsService.java      # Technique ⑤
│   ├── StepBackPromptingService.java   # Technique ⑥
│   ├── GroundedCoTService.java         # Technique ⑦
│   └── SelfCritiqueCoTService.java     # Technique ⑧
└── controller/
    └── UnderwritingController.java     # REST API
```

---

## The Key Demo Insight: DTI Estimation Gap

Run `GET /underwriting/demo/zero-shot` and `GET /underwriting/demo/grounded` side by side.

**Zero-Shot CoT output (Technique ①):**
> "Step 2 (DTI): Annual income $72K, debt $18K → DTI ≈ 25%. Well within the 43% limit. ✅"

**Grounded CoT output (Technique ⑦):**
> "Step 2 (DTI): calculateDTI(4821) returned: monthly debt $1,940, verified income $6,000 → **DTI 32.3%** (Borderline tier). Still under 43% but requires compensating factor to proceed."

The 23-point gap (25% → 32.3%) changes the risk classification and the conditions attached to approval. On a $25K loan, that gap is a compliance liability.

---

## The 3 Underwriting Tools

Technique ⑦ (Grounded CoT) uses `UnderwritingTools` — 3 `@Tool`-annotated methods.

| Tool | Verifies | Zero-Shot Estimate | Tool Result (App #4821) |
|------|----------|--------------------|--------------------------|
| `calculateDTI(applicationId)` | Total monthly obligations / income | ~25% | **32.3%** |
| `getCreditTier(score)` | Tier + default rate | "borderline" | Subprime, 12.4% default |
| `getPaymentTrend(applicationId)` | Consecutive on-time since last miss | "some misses" | 18 consecutive — **Improving** ✅ |

---

## Requirements

- Java 17+
- Maven 3.8+
- OpenAI API key (GPT-4o or GPT-4o-mini)
- No database required — tools use in-memory stub data for demonstration

---

## Contributing

- ⭐ **[Star the repo](https://github.com/ai-dev-academy/ai-dev-academy-projects)** — helps more Java developers find this resource
- 🍴 **[Fork it](https://github.com/ai-dev-academy/ai-dev-academy-projects/fork)** — connect `UnderwritingTools` to a real database
- 💬 **[Open an issue](https://github.com/ai-dev-academy/ai-dev-academy-projects/issues)** — suggest a 9th technique or report a bug

---

Built with ♥ by [AI Dev Academy](https://ai-dev-academy.com) — Java Spring Boot AI Tutorials
