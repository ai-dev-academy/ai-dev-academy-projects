# 🤖 Intro to LLM APIs

> How AI models receive and respond to requests

**Tier:** Free | **Duration:** 25 min | **Difficulty:** Beginner | **Category:** Foundations

**Tags:** OpenAI, REST, Tokens, Spring Boot

---

## Description

Understand the full lifecycle of an LLM API call — from HTTP request to tokenization to streaming response.

---

## What You'll Learn

- How LLMs tokenize and process input
- Request/response structure of OpenAI & Claude APIs
- Build your first AI endpoint in Spring Boot
- Handle errors, timeouts and rate limits

---

## Tech Stack

- Spring Boot 3.x
- OpenAI API
- Maven
- Java 17

---

## Project

Hello AI — Spring Boot REST endpoint calling OpenAI

---

## Real-World Use Case

A customer service portal that routes messages using AI classification

---

## Prompt Guide

System: "You are a helpful assistant." User: "Classify this message: {message}" — Start simple, then layer specifics.

---

## Steps

### Step 1: Set up Spring Boot project

**Why:** Spring Boot gives you a production-ready server in minutes. Without it you'd write hundreds of lines of boilerplate.

**What:** We create a new project using Spring Initializr with the Spring AI dependency.
**Files:** pom.xml, src/main/java/com/example/AiApplication.java, src/main/resources/application.properties
```
ai-demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── AiApplication.java
│   │   │   └── controller/
│   │   │       └── ChatController.java
│   │   └── resources/
│   │       └── application.properties
└── pom.xml
```
```
<!-- pom.xml -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```
**Common Mistakes:**
- Forgetting to add spring-ai BOM to dependencyManagement
- Using Java 8 instead of Java 17+
**Testing:** Run: mvn spring-boot:run — should see "Started AiApplication" in logs
**Expected:** Server starts on http://localhost:8080 with no errors

---

### Step 2: Configure OpenAI API key

**Why:** The API key authenticates your app with OpenAI. Without it every request returns 401 Unauthorized.

**What:** Add your API key to application.properties so Spring AI auto-configures the client.
**Files:** src/main/resources/application.properties, .env
```
resources/
└── application.properties   ← add key here
```
```
# application.properties
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.7
```
**Common Mistakes:**
- Committing the API key to GitHub — always use env variables
- Wrong model name (gpt-4 vs gpt-4o)
**Testing:** Check startup logs for "OpenAI auto-configuration" — no error means key is valid
**Expected:** Application starts without "Unauthorized" errors

---

### Step 3: Create ChatClient bean

**Why:** ChatClient is Spring AI's main interface to the LLM. The @Bean pattern means Spring manages its lifecycle.

**What:** Build a ChatClient with a default system message that defines AI behavior for all requests.
**Files:** src/main/java/com/example/config/AiConfig.java
```
config/
└── AiConfig.java   ← new file
```
```
@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem("You are a helpful assistant.")
            .build();
    }
}
```
**Common Mistakes:**
- Not using the Builder pattern — always use ChatClient.Builder injection
- Hardcoding the system prompt in controllers
**Testing:** Add a unit test: assertThat(chatClient).isNotNull()
**Expected:** ChatClient bean created in Spring context without errors

---

### Step 4: Build REST controller

**Why:** The controller exposes your AI as an HTTP endpoint. Any frontend or tool can now call your AI via a simple POST request.

**What:** Create a /chat endpoint that accepts a user message and returns the AI response.
**Files:** src/main/java/com/example/controller/ChatController.java
```
controller/
└── ChatController.java   ← new file
```
```
@RestController
@RequestMapping("/ai")
public class ChatController {
    private final ChatClient chatClient;
    
    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatClient.prompt()
            .user(message)
            .call()
            .content();
    }
}
```
**Common Mistakes:**
- Missing @RequestBody annotation
- Using @GetMapping for a POST endpoint
- Not injecting ChatClient via constructor
**Testing:** curl -X POST http://localhost:8080/ai/chat \
  -H "Content-Type: application/json" \
  -d '"What is Spring Boot?"'
**Expected:** Response: "Spring Boot is a framework that simplifies..."

---

### Step 5: Add error handling

**Why:** LLM APIs can fail — rate limits, network issues, invalid inputs. Without error handling your app crashes for users.

**What:** Wrap the AI call in try/catch and return meaningful error messages.
**Files:** src/main/java/com/example/controller/ChatController.java
```
controller/
└── ChatController.java   ← update this file
```
```
@PostMapping("/chat")
public ResponseEntity<String> chat(@RequestBody String message) {
    try {
        String response = chatClient.prompt()
            .user(message)
            .call()
            .content();
        return ResponseEntity.ok(response);
    } catch (RateLimitException e) {
        return ResponseEntity.status(429)
            .body("Rate limit hit. Please wait 60 seconds.");
    } catch (Exception e) {
        return ResponseEntity.status(500)
            .body("AI service unavailable: " + e.getMessage());
    }
}
```
**Common Mistakes:**
- Returning stack traces to users — always use friendly messages
- Not handling 429 rate limit separately from 500 errors
**Testing:** Test with empty string, very long string (10000 chars), and special characters
**Expected:** All edge cases return 400/429/500 with readable messages — never a stack trace

---

### Step 6: Test with Postman

**Why:** Testing manually confirms everything works end-to-end before building a frontend.

**What:** Use Postman or curl to send requests and verify responses.
**Files:** postman-collection.json
```
root/
└── postman-collection.json   ← import this
```
```
// Sample Postman collection
{
  "name": "AI Dev Academy - Topic 01",
  "request": {
    "method": "POST",
    "url": "http://localhost:8080/ai/chat",
    "header": [{"key": "Content-Type", "value": "application/json"}],
    "body": {"raw": ""What is tokenization in LLMs?""}
  }
}
```
**Common Mistakes:**
- Forgetting Content-Type: application/json header
- Sending body without quotes for raw string
**Testing:** Send 3 test messages — check response time, accuracy, and error handling
**Expected:** Each request responds in 1-3 seconds with relevant AI-generated answer

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/01-intro-to-llm-apis

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
