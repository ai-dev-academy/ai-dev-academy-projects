# 🔗 LangChain4j AiServices

> Interface-based AI proxies — the production Java pattern

**Tier:** Pro | **Duration:** 40 min | **Difficulty:** Intermediate | **Category:** LangChain4j

**Tags:** LangChain4j, AiServices, @SystemMessage, Proxy

---

## Description

Use LangChain4j AiServices to create clean, testable AI interfaces.

---

## What You'll Learn

- AiServices proxy pattern
- @SystemMessage annotations
- @MemoryId per-user memory
- Testing AI services

---

## Tech Stack

- LangChain4j
- Spring Boot 3.x
- Redis
- Java 17

---

## Project

Customer support bot with persona and per-user memory

---

## Real-World Use Case

A bank support bot that remembers each customer context

---

## Prompt Guide

Put persona in @SystemMessage at interface level. Keep @UserMessage lean.

---

## Steps

### Step 1: Add LangChain4j dependency

**Why:** LangChain4j provides more control over agents and memory than Spring AI.

**What:** Add the Spring Boot starter.
**Files:** pom.xml
```
No new files
```
```
<dependency>
  <groupId>dev.langchain4j</groupId>
  <artifactId>langchain4j-spring-boot-starter</artifactId>
  <version>0.32.0</version>
</dependency>
```
**Common Mistakes:**
- Version mismatch between core and starter
**Testing:** mvn dependency:tree | grep langchain4j
**Expected:** Single version of langchain4j in dependency tree

---

### Step 2: Define AiService interface

**Why:** Annotated interfaces auto-proxy to LLM calls — no boilerplate implementation needed.

**What:** Create interface with @SystemMessage.
**Files:** CustomerSupport.java
```
service/CustomerSupport.java
```
```
interface CustomerSupport {
  @SystemMessage("You are a helpful bank support agent.")
  String chat(@MemoryId String userId, @UserMessage String message);
}
```
**Common Mistakes:**
- Missing @MemoryId — all users share same memory
**Testing:** Call chat() with two different userIds — verify separate contexts
**Expected:** Each user maintains independent conversation history

---

### Step 3: Build the service bean

**Why:** AiServices.builder() wires the interface to the actual LLM.

**What:** Configure with model and memory provider.
**Files:** AiConfig.java
```
config/AiConfig.java
```
```
@Bean
public CustomerSupport customerSupport(ChatLanguageModel model) {
  return AiServices.builder(CustomerSupport.class)
    .chatLanguageModel(model)
    .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(20))
    .build();
}
```
**Common Mistakes:**
- Not using chatMemoryProvider — memory leaks across sessions
**Testing:** Send 5 messages — 6th should remember context from message 1
**Expected:** AI recalls earlier context within 20-message window

---

### Step 4: Add tools

**Why:** Tools let the AI call real business logic — check balances, create tickets.

**What:** Create @Tool methods the AI can invoke.
**Files:** BankingTools.java
```
tools/BankingTools.java
```
```
class BankingTools {
  @Tool("Get account balance for customer")
  public String getBalance(String accountId) {
    return "Balance: $" + accountRepository.findBalance(accountId);
  }
}
```
**Common Mistakes:**
- Vague tool descriptions — LLM won't know when to call them
**Testing:** Ask "What is my balance?" — verify tool is called in logs
**Expected:** AI calls getBalance() and returns actual DB value

---

### Step 5: Wire tools to service

**Why:** Tools must be registered with the AiService to be callable.

**What:** Add .tools() to the builder.
**Files:** AiConfig.java
```
No new files
```
```
AiServices.builder(CustomerSupport.class)
  .chatLanguageModel(model)
  .tools(new BankingTools(accountRepository))
  .build();
```
**Common Mistakes:**
- Forgetting to inject real dependencies into tool class
**Testing:** Ask balance question — check tool is invoked with correct accountId
**Expected:** Tool called with customer accountId extracted from context

---

### Step 6: Test the service

**Why:** AiServices are interfaces — easy to mock for unit tests.

**What:** Write unit test mocking the ChatLanguageModel.
**Files:** CustomerSupportTest.java
```
test/CustomerSupportTest.java
```
```
@Test
void testSupportBot() {
  when(model.generate(any())).thenReturn(mockResponse);
  String result = customerSupport.chat("user123", "Hello");
  assertThat(result).isNotEmpty();
}
```
**Common Mistakes:**
- Calling real LLM in unit tests — slow and costs money
**Testing:** Unit test runs in <100ms without real API calls
**Expected:** Test passes with mocked model, verifying service wiring

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/06-langchain4j-aiservices

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
