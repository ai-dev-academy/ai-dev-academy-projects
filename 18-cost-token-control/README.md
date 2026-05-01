# 💰 Cost & Token Control

> Track, optimize and cap AI spend in production

**Tier:** Advanced | **Duration:** 40 min | **Difficulty:** Advanced | **Category:** Production

**Tags:** Token Counting, Caching, Cost, Budget

---

## Description

Token tracking, semantic caching, model tiering and budget alerts.

---

## What You'll Learn

- Token counting
- Semantic cache
- Model tiering
- Cost metrics

---

## Tech Stack

- Micrometer
- Spring AI
- Tiktoken
- Java 17

---

## Project

Cost dashboard — real-time per-endpoint token spend with budget circuit breaker

---

## Real-World Use Case

SaaS platform tracking AI cost per customer and blocking usage at plan limit

---

## Prompt Guide

Route by complexity: simple→Haiku, complex→Sonnet, code→Opus. Add classifier step first.

---

## Steps

### Step 1: Count tokens before sending

**Why:** Knowing token count before the API call lets you enforce limits and estimate cost.

**What:** Add tiktoken for token counting.
**Files:** TokenCountingService.java
```
service/TokenCountingService.java
```
```
// Using tiktoken-java
public int countTokens(String text, String model) {
  EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
  Encoding enc = registry.getEncodingForModel(ModelType.GPT_4O);
  return enc.encode(text).size();
}
```
**Common Mistakes:**
- Using word count as proxy — very inaccurate for token budget
**Testing:** Count tokens for 100 words of English text — should be ~130 tokens
**Expected:** Token count within 5% of OpenAI's actual token count

---

### Step 2: Track cost per request

**Why:** Cost visibility is the first step to cost control.

**What:** Calculate and log USD cost for every API call.
**Files:** CostTrackingAdvisor.java
```
advisor/CostTrackingAdvisor.java
```
```
// Cost constants (USD per million tokens)
static final double INPUT_COST  = 1.00 / 1_000_000;  // Haiku
static final double OUTPUT_COST = 5.00 / 1_000_000;

public AdvisedResponse aroundCall(AdvisedRequest req, CallAroundAdvisorChain chain) {
  var response = chain.nextAroundCall(req);
  var usage = response.response().getMetadata().getUsage();
  double cost = usage.getPromptTokens() * INPUT_COST + usage.getGenerationTokens() * OUTPUT_COST;
  meterRegistry.counter("ai.cost.usd", "endpoint", req.context().get("endpoint"))
    .increment(cost);
  return response;
}
```
**Common Mistakes:**
- Using wrong model pricing — GPT-4o costs 60x more than Haiku
**Testing:** Send 10 requests — check /actuator/metrics/ai.cost.usd total
**Expected:** Cost metric matches actual OpenAI dashboard spend within 5%

---

### Step 3: Build model tier router

**Why:** Routing simple queries to cheap models saves 50-80% without quality loss.

**What:** Classify prompt complexity and route to appropriate model.
**Files:** ModelRouter.java
```
service/ModelRouter.java
```
```
public String smartChat(String message) {
  int tokens = tokenCounter.count(message);
  String complexity = classifyComplexity(message);
  
  return switch (complexity) {
    case "SIMPLE"   -> haikuClient.prompt().user(message).call().content();
    case "MODERATE" -> sonnetClient.prompt().user(message).call().content();
    case "COMPLEX"  -> opusClient.prompt().user(message).call().content();
    default         -> haikuClient.prompt().user(message).call().content();
  };
}
```
**Common Mistakes:**
- Only routing by token count — complexity is about reasoning not length
**Testing:** Send "What is 2+2?" → Haiku. Send "Analyze this 500-line algorithm" → Sonnet/Opus
**Expected:** 90% of queries routed to cheapest appropriate model

---

### Step 4: Set user budget limits

**Why:** Free plan users should not consume $100 of API budget.

**What:** Track spend per user and block when limit hit.
**Files:** BudgetService.java
```
service/BudgetService.java
```
```
public void checkAndRecordCost(String userId, double cost) {
  double monthlySpend = redis.getAndAdd("budget:" + userId, cost);
  double limit = getUserPlanLimit(userId); // free=$0.10, pro=$5.00
  
  if (monthlySpend + cost > limit) {
    throw new BudgetExceededException(
      "Monthly AI limit reached. Upgrade to Pro for more messages."
    );
  }
}
```
**Common Mistakes:**
- No per-user tracking — one power user exhausts all budget
**Testing:** Set limit to $0.01, send 5 messages — 3rd/4th should trigger budget exception
**Expected:** User receives upgrade prompt after hitting monthly limit

---

### Step 5: Build cost dashboard

**Why:** Operations team needs real-time visibility into AI spend.

**What:** Create /admin/costs endpoint with per-user and per-endpoint breakdown.
**Files:** CostDashboardController.java
```
controller/CostDashboardController.java
```
```
@GetMapping("/admin/costs")
@AdminOnly
public CostReport getCosts(@RequestParam String period) {
  return CostReport.builder()
    .totalCostUSD(metricsService.getTotalCost(period))
    .costByEndpoint(metricsService.getCostByEndpoint(period))
    .costByUser(metricsService.getTopUsersBySpend(period, 10))
    .averageCostPerRequest(metricsService.getAvgCost(period))
    .build();
}
```
**Common Mistakes:**
- Exposing cost data without admin auth — users see each other's spend
**Testing:** GET /admin/costs?period=today — verify breakdown by endpoint and user
**Expected:** Dashboard shows top 10 users by spend and cost per endpoint in real-time

---

### Step 6: Cost alerts

**Why:** Unexpected cost spikes need immediate alerting before invoice arrives.

**What:** Send Slack/email alert when hourly cost exceeds threshold.
**Files:** CostAlertService.java
```
service/CostAlertService.java
```
```
@Scheduled(fixedRate = 3600000) // every hour
public void checkCostAlerts() {
  double hourlySpend = metricsService.getHourlyCost();
  double threshold = 10.0; // alert if $10/hour
  
  if (hourlySpend > threshold) {
    slackClient.sendAlert(
      "⚠️ AI cost spike: $" + hourlySpend + "/hour — investigate immediately"
    );
  }
}
```
**Common Mistakes:**
- Daily alerts instead of hourly — $240 spent before you notice
**Testing:** Set threshold to $0.001, run 5 requests — verify Slack alert fires
**Expected:** Alert fires within 1 hour of threshold breach with spend amount

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/18-cost-token-control

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
