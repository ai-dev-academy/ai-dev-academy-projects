# 🌱 Spring AI Deep Dive

> ChatClient, Advisors and the full Spring AI architecture

**Tier:** Free | **Duration:** 45 min | **Difficulty:** Intermediate | **Category:** Spring AI

**Tags:** Spring AI, ChatClient, Advisors, AutoConfig

---

## Description

Master the full Spring AI architecture — ChatClient, Advisor chain pattern, multi-provider switching and auto-configuration.

---

## What You'll Learn

- ChatClient builder
- Advisor pattern
- Multi-provider switching
- Auto-configuration

---

## Tech Stack

- Spring AI 1.x
- Spring Boot 3.x
- Multiple LLM Providers
- Java 17

---

## Project

Multi-provider chatbot — switch OpenAI/Claude with zero code changes

---

## Real-World Use Case

Enterprise chatbot that falls back from GPT-4o to Claude during outages

---

## Prompt Guide

Use PromptTemplate with {placeholders} for all dynamic content. Set defaultSystem() on ChatClient builder.

---

## Steps

### Step 1: Understand ChatClient architecture

**Why:** ChatClient is the central abstraction. Understanding it unlocks all Spring AI features.

**What:** Learn the Builder → Client → Advisor → Model chain.
**Files:** AiConfig.java
```
config/AiConfig.java
```
```
// ChatClient.Builder is auto-injected by Spring AI
@Bean
public ChatClient chatClient(ChatClient.Builder builder) {
    return builder
        .defaultSystem("You are a helpful Java assistant.")
        .defaultAdvisors(
            new MessageChatMemoryAdvisor(chatMemory),
            new SimpleLoggerAdvisor()
        )
        .build();
}
```
**Common Mistakes:**
- Creating ChatClient directly — always use the Builder bean
**Testing:** Check logs for "ChatClient initialized" with your advisors listed
**Expected:** Startup logs show advisor chain: Logger → Memory → Model

---

### Step 2: Add Advisor chain

**Why:** Advisors are Spring AI's equivalent of interceptors — they run before/after every LLM call.

**What:** Create a custom logging advisor that records every prompt.
**Files:** LoggingAdvisor.java
```
advisor/LoggingAdvisor.java
```
```
@Component
public class LoggingAdvisor implements CallAroundAdvisor {
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest req, CallAroundAdvisorChain chain) {
        log.info("PROMPT: {}", req.userText());
        var response = chain.nextAroundCall(req);
        log.info("RESPONSE: {}", response.response().getResult().getOutput().getContent());
        return response;
    }
}
```
**Common Mistakes:**
- Modifying the request/response inadvertently — return unchanged objects
**Testing:** Send a chat message — check logs for PROMPT and RESPONSE entries
**Expected:** Both prompt and response logged for every API call

---

### Step 3: Configure multiple providers

**Why:** Different LLMs excel at different tasks. Multi-provider lets you route by task type.

**What:** Configure both OpenAI and Anthropic in application.properties.
**Files:** application.properties
```
No new files
```
```
# OpenAI
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.model=gpt-4o-mini

# Anthropic  
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.anthropic.chat.model=claude-haiku-4-5-20251001

# Active provider (switch without code change)
app.ai.provider=openai
```
**Common Mistakes:**
- Hardcoding provider in code — defeats the purpose of configuration
**Testing:** Change app.ai.provider=anthropic — restart — verify different model responds
**Expected:** Same request returns response from different provider based on config

---

### Step 4: Build provider router

**Why:** Route simple questions to cheap models, complex to expensive — saves 60-80% on API costs.

**What:** Create a service that selects the right model by prompt complexity.
**Files:** RouterService.java
```
service/RouterService.java
```
```
public String chat(String message) {
    // Short simple message → fast cheap model
    if (message.length() < 100) {
        return haikuClient.prompt().user(message).call().content();
    }
    // Complex analysis → powerful model
    return gpt4Client.prompt().user(message).call().content();
}
```
**Common Mistakes:**
- Only routing by length — use a classifier for accuracy
**Testing:** Send 5-word message → check model used. Send 500-word analysis → check different model.
**Expected:** Short messages use Haiku, long/complex use GPT-4o

---

### Step 5: Implement provider fallback

**Why:** LLM APIs have outages and rate limits. Fallback keeps your app running.

**What:** Wrap primary model call with catch → fallback model.
**Files:** ResilientAiService.java
```
service/ResilientAiService.java
```
```
public String chatWithFallback(String message) {
    try {
        return primaryClient.prompt().user(message).call().content();
    } catch (Exception e) {
        log.warn("Primary model failed, using fallback: {}", e.getMessage());
        return fallbackClient.prompt().user(message).call().content();
    }
}
```
**Common Mistakes:**
- Not logging fallback activations — you won't know when primary fails
**Testing:** Set wrong API key for primary — verify fallback activates
**Expected:** Response still arrives from fallback with warning in logs

---

### Step 6: Test multi-provider setup

**Why:** Integration tests verify the full provider chain works end-to-end.

**What:** Write tests that verify both providers respond correctly.
**Files:** MultiProviderTest.java
```
test/MultiProviderTest.java
```
```
@Test
void testOpenAiResponds() {
    String response = openAiClient.prompt()
        .user("Say hello in one word")
        .call().content();
    assertThat(response).isNotEmpty();
}

@Test  
void testFallbackActivates() {
    // Force primary to fail
    String response = resilientService.chatWithFallback("Hello");
    assertThat(response).isNotEmpty(); // fallback worked
}
```
**Common Mistakes:**
- Tests that call real APIs in CI — use mocks instead
**Testing:** Run tests with mocked providers — no real API calls needed
**Expected:** All tests pass without real API keys in CI

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/05-spring-ai-deep-dive

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
