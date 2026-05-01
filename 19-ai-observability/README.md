# 📊 AI Observability

> Trace every AI call — latency, cost, quality in Grafana

**Tier:** Advanced | **Duration:** 50 min | **Difficulty:** Advanced | **Category:** Production

**Tags:** Micrometer, Prometheus, Grafana, Tracing

---

## Description

Instrument AI calls with Micrometer, build Grafana dashboards for latency/cost/quality.

---

## What You'll Learn

- Micrometer for AI
- Prometheus+Grafana
- LangSmith tracing
- P95 alerts

---

## Tech Stack

- Micrometer
- Prometheus
- Grafana
- Spring Boot 3.x

---

## Project

Full AI observability stack — Grafana dashboard with 5 production metrics

---

## Real-World Use Case

Platform team paged when AI latency exceeds 3s or error rate spikes above 1%

---

## Prompt Guide

Observability wraps calls — no prompting needed. Tag metrics with model, endpoint, user tier for Grafana segmentation.

---

## Steps

### Step 1: Add Micrometer to project

**Why:** Micrometer is the standard metrics facade for Spring — works with Prometheus, Datadog, CloudWatch.

**What:** Add Micrometer + Prometheus registry.
**Files:** pom.xml
```
No new files
```
```
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```
**Common Mistakes:**
- Adding Micrometer without a registry — metrics not exported
**Testing:** GET /actuator/prometheus — should return Prometheus metrics page
**Expected:** Metrics page shows JVM, HTTP and custom metrics in Prometheus format

---

### Step 2: Create AI metrics

**Why:** Default Spring metrics don't cover LLM-specific concerns like token cost and model latency.

**What:** Build custom counters and timers for every AI call.
**Files:** AiMetricsService.java
```
metrics/AiMetricsService.java
```
```
public void recordAiCall(String model, String endpoint, long latencyMs, int tokens, boolean success) {
  Timer.builder("ai.call.latency")
    .tag("model", model)
    .tag("endpoint", endpoint)
    .register(meterRegistry)
    .record(latencyMs, TimeUnit.MILLISECONDS);
  
  meterRegistry.counter("ai.tokens.total", "model", model).increment(tokens);
  meterRegistry.counter("ai.calls.total", "success", String.valueOf(success)).increment();
}
```
**Common Mistakes:**
- High-cardinality tags like userId — creates millions of metric series
**Testing:** Send 10 requests — verify all 3 metrics populated at /actuator/prometheus
**Expected:** ai.call.latency, ai.tokens.total, ai.calls.total all visible in Prometheus

---

### Step 3: Run Prometheus + Grafana

**Why:** Local observability stack lets you build dashboards before going to production.

**What:** Add Prometheus and Grafana to Docker Compose.
**Files:** docker-compose.yml, prometheus.yml
```
docker-compose.yml + config/
```
```
prometheus:
  image: prom/prometheus
  volumes:
    - ./config/prometheus.yml:/etc/prometheus/prometheus.yml
  ports: ["9090:9090"]

grafana:
  image: grafana/grafana
  ports: ["3000:3000"]
  environment:
    GF_SECURITY_ADMIN_PASSWORD: admin
```
**Common Mistakes:**
- Not configuring Prometheus to scrape your Spring Boot app
**Testing:** Grafana at localhost:3000 → Explore → ai.call.latency metric visible
**Expected:** Grafana shows latency timeseries for your AI endpoint

---

### Step 4: Build Grafana dashboard

**Why:** A good dashboard lets you see problems in 10 seconds, not 10 minutes.

**What:** Create dashboard with 5 key panels.
**Files:** grafana-dashboard.json
```
config/grafana-dashboard.json
```
```
// 5 essential AI panels:
// 1. P95 latency by model (target: <3000ms)
// 2. Error rate % (target: <1%)
// 3. Requests per minute (track growth)
// 4. Token cost per hour (budget tracking)
// 5. Cache hit rate % (efficiency metric)

// Prometheus query for P95:
histogram_quantile(0.95, rate(ai_call_latency_seconds_bucket[5m]))
```
**Common Mistakes:**
- Average latency instead of P95 — hides slow outliers
**Testing:** Import dashboard JSON into Grafana — verify all 5 panels populate
**Expected:** Dashboard shows real-time metrics updating every 15 seconds

---

### Step 5: Set up alerting

**Why:** Dashboards you watch; alerts find you. Critical for production.

**What:** Configure Grafana alerts for latency and error rate.
**Files:** grafana-alerts.json
```
config/grafana-alerts.json
```
```
// Alert 1: P95 latency > 3s for 5 minutes
EXPR: histogram_quantile(0.95, rate(ai_call_latency_seconds_bucket[5m])) > 3

// Alert 2: Error rate > 1% for 2 minutes
EXPR: rate(ai_calls_total{success="false"}[2m]) 
      / rate(ai_calls_total[2m]) > 0.01

// Notification: Slack channel #ai-alerts
```
**Common Mistakes:**
- Immediate alerts on single spike — add "for 5 minutes" to reduce noise
**Testing:** Force slow response (Thread.sleep) — verify latency alert fires within 5 minutes
**Expected:** Slack message received: "P95 AI latency exceeded 3s threshold"

---

### Step 6: Add distributed tracing

**Why:** Tracing shows exactly where time is spent — DB query? Embedding call? LLM generation?

**What:** Add Spring Boot Actuator tracing with Zipkin.
**Files:** pom.xml, application.properties
```
No new files
```
```
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>

# application.properties
management.tracing.sampling.probability=0.1 # trace 10% of requests
```
**Common Mistakes:**
- Sampling probability=1.0 — 100% tracing overwhelms Zipkin at scale
**Testing:** Send request → check Zipkin at localhost:9411 → see full trace with spans
**Expected:** Trace shows: HTTP → Controller → Embedding → VectorSearch → LLM → Response with timings

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/19-ai-observability

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
