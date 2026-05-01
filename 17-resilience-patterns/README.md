# 🛡️ Resilience Patterns

> Retry, circuit breaker, fallback and caching for AI services

**Tier:** Advanced | **Duration:** 45 min | **Difficulty:** Advanced | **Category:** Production

**Tags:** Resilience4j, Circuit Breaker, Retry, Fallback

---

## Description

Harden AI microservices with Resilience4j — retry, circuit breaker, fallback and semantic caching.

---

## What You'll Learn

- Retry with backoff+jitter
- Circuit breaker
- Fallback model
- Redis semantic cache

---

## Tech Stack

- Resilience4j
- Spring Boot 3.x
- Redis
- Java 17

---

## Project

Production-hardened AI service — zero errors under provider failure

---

## Real-World Use Case

Chatbot falling back from GPT-4o to Claude Haiku when OpenAI returns 429s

---

## Prompt Guide

No special prompting for resilience — infrastructure code. Ensure fallback uses same system prompt.

---

## Steps

### Step 1: Add Resilience4j

**Why:** Resilience4j adds retry, circuit breaker and rate limiting to any Java method.

**What:** Add dependency and starter.
**Files:** pom.xml
```
No new files
```
```
<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```
**Common Mistakes:**
- Using the Spring Boot 2 version with Boot 3
**Testing:** mvn compile — no version conflicts
**Expected:** Resilience4j annotations available in classpath

---

### Step 2: Configure retry with backoff

**Why:** LLM APIs return 429 (rate limit). Retry after 60 seconds with exponential backoff.

**What:** Configure @Retry on your AI service method.
**Files:** application.properties, AiService.java
```
No new files
```
```
# application.properties
resilience4j.retry.instances.ai.max-attempts=3
resilience4j.retry.instances.ai.wait-duration=2s
resilience4j.retry.instances.ai.enable-exponential-backoff=true
resilience4j.retry.instances.ai.exponential-backoff-multiplier=2

// Service
@Retry(name = "ai")
public String chat(String message) {
  return chatClient.prompt().user(message).call().content();
}
```
**Common Mistakes:**
- Retrying on 400 bad request — not a transient error, will always fail
**Testing:** Mock 429 response — verify 3 retry attempts in logs with increasing delays
**Expected:** Logs show: attempt 1 failed → wait 2s → attempt 2 failed → wait 4s → attempt 3 succeeds

---

### Step 3: Add circuit breaker

**Why:** If LLM is down for 5 minutes, don't keep retrying. Open circuit, fail fast.

**What:** Configure @CircuitBreaker with fallback.
**Files:** AiService.java
```
No new files
```
```
@CircuitBreaker(name = "ai", fallbackMethod = "fallbackResponse")
@Retry(name = "ai")
public String chat(String message) {
  return primaryModel.prompt().user(message).call().content();
}

public String fallbackResponse(String message, Exception e) {
  log.warn("Primary AI failed, using fallback: {}", e.getMessage());
  return fallbackModel.prompt().user(message).call().content();
}
```
**Common Mistakes:**
- Circuit breaker without fallback — users see errors instead of degraded service
**Testing:** Kill primary API → send 5 requests → circuit opens → fallback activates
**Expected:** Requests 1-2 retry, request 3 opens circuit, requests 4+ go directly to fallback

---

### Step 4: Implement semantic cache

**Why:** Same question asked 1000 times costs 1000x. Cache semantically similar queries.

**What:** Cache responses by embedding similarity, not exact string.
**Files:** SemanticCacheService.java
```
service/SemanticCacheService.java
```
```
public String cachedChat(String message) {
  float[] queryVector = embeddingModel.embed(message);
  
  // Find similar cached query (threshold 0.95)
  Optional<CachedResponse> hit = cacheStore.findSimilar(queryVector, 0.95);
  if (hit.isPresent()) {
    log.info("Cache hit for: {}", message);
    return hit.get().getResponse();
  }
  
  String response = chat(message);
  cacheStore.save(queryVector, message, response);
  return response;
}
```
**Common Mistakes:**
- Threshold too low (0.7) — caches semantically different questions
**Testing:** "What is Spring Boot?" and "Tell me about Spring Boot" should hit same cache
**Expected:** Second query returns cached response in <10ms vs 1000ms for API call

---

### Step 5: Add rate limiting per user

**Why:** Without user-level limits one user exhausts your entire API budget.

**What:** Configure @RateLimiter per userId.
**Files:** AiController.java
```
No new files
```
```
@RateLimiter(name = "per-user", fallbackMethod = "rateLimitFallback")
@PostMapping("/chat")
public String chat(@RequestBody String message, @AuthenticationPrincipal String userId) {
  return aiService.chat(message);
}

public String rateLimitFallback(String message, String userId, RequestNotPermitted e) {
  return "Rate limit reached. You can send 10 messages per minute. Please wait.";
}
```
**Common Mistakes:**
- Global rate limit instead of per-user — one user blocks all others
**Testing:** Send 11 requests in 1 minute — 11th should return rate limit message
**Expected:** First 10 succeed, 11th returns friendly rate limit message

---

### Step 6: Monitor resilience metrics

**Why:** You need to know circuit breaker state and cache hit ratio in production.

**What:** Expose resilience metrics via Actuator + Prometheus.
**Files:** application.properties
```
No new files
```
```
# Expose resilience metrics
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=always

# Key metrics to watch:
# resilience4j.circuitbreaker.ai.state (CLOSED/OPEN/HALF_OPEN)
# resilience4j.retry.ai.calls
# ai.cache.hits vs ai.cache.misses
```
**Common Mistakes:**
- No monitoring — circuit breaker opens silently, users suffer
**Testing:** GET /actuator/metrics/resilience4j.circuitbreaker.ai.state
**Expected:** Metric shows CLOSED (healthy) or OPEN (degraded) — alert when OPEN

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/17-resilience-patterns

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
