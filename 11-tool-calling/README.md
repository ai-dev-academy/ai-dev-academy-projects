# 🔧 Tool Calling / @Tool

> Give LLMs the ability to call your Java methods

**Tier:** Pro | **Duration:** 45 min | **Difficulty:** Intermediate | **Category:** LangChain4j

**Tags:** @Tool, Function Calling, LangChain4j, Spring AI

---

## Description

Expose Java methods as LLM-callable tools — the foundation of agentic systems.

---

## What You'll Learn

- @Tool annotation
- When LLMs call tools
- Tool chaining
- Spring AI FunctionCallback

---

## Tech Stack

- LangChain4j
- Spring Boot 3.x
- REST APIs
- Java 17

---

## Project

Order management agent with 3 tools

---

## Real-World Use Case

Retail assistant that looks up orders, processes returns, checks stock

---

## Prompt Guide

Tool descriptions are prompts. Write: "Get order status by ID. Returns: PENDING|SHIPPED|DELIVERED|CANCELLED"

---

## Steps

### Step 1: Create tool class

**Why:** @Tool turns any Java method into something the LLM can call when needed.

**What:** Create OrderTools with @Tool annotated methods.
**Files:** OrderTools.java
```
tools/OrderTools.java
```
```
class OrderTools {
  @Tool("Get order status by order ID")
  public String getStatus(String orderId) {
    return orderRepo.findById(orderId)
      .map(o -> "Order " + orderId + ": " + o.getStatus())
      .orElse("Order not found");
  }
  
  @Tool("Cancel an order by ID. Reason is required.")
  public String cancelOrder(String orderId, String reason) {
    orderService.cancel(orderId, reason);
    return "Order " + orderId + " cancelled: " + reason;
  }
}
```
**Common Mistakes:**
- Vague descriptions — LLM won't know when to call the tool
**Testing:** Ask agent "Where is order 12345?" — verify getStatus called in logs
**Expected:** Tool invoked with correct orderId extracted from natural language

---

### Step 2: Register tools with agent

**Why:** Tools must be registered at service creation time.

**What:** Add tools to AiServices builder.
**Files:** AiConfig.java
```
No new files
```
```
return AiServices.builder(OrderAgent.class)
  .chatLanguageModel(model)
  .tools(new OrderTools(orderRepository, orderService))
  .build();
```
**Common Mistakes:**
- Forgetting to inject real repository into tools
**Testing:** Ask about non-existent order — tool should return "Order not found"
**Expected:** Agent replies "I couldn't find order 99999" based on tool result

---

### Step 3: Chain multiple tools

**Why:** Complex requests need multiple tool calls — check status THEN cancel if pending.

**What:** Ask agent to cancel if not delivered — requires 2 tool calls.
**Files:** OrderAgentTest.java
```
test/OrderAgentTest.java
```
```
String response = orderAgent.chat(
  "Cancel order 12345 if it hasn't shipped yet"
);
// Agent calls: getStatus("12345") → sees PENDING → calls cancelOrder()
```
**Common Mistakes:**
- Assuming one tool call per request — agents iterate
**Testing:** Verify logs show 2 tool calls: getStatus then cancelOrder
**Expected:** Agent autonomously chains tools to complete multi-step task

---

### Step 4: Add tracking tool

**Why:** More tools = more capable agent. Add shipment tracking as third capability.

**What:** Add trackShipment tool with external API call.
**Files:** OrderTools.java
```
No new files
```
```
@Tool("Track shipment location. Returns current city and ETA.")
public String trackShipment(String orderId) {
  var tracking = shippingApi.track(orderId);
  return "Package in " + tracking.getCity() + ", ETA: " + tracking.getEta();
}
```
**Common Mistakes:**
- Tool that throws exception on not-found — return friendly string instead
**Testing:** Ask "Where is my order?" — verify tracking info returned
**Expected:** Agent calls trackShipment and returns city + ETA in natural language

---

### Step 5: Handle tool errors gracefully

**Why:** Real APIs fail. Tool errors must be caught so agent can respond gracefully.

**What:** Wrap tool implementations in try/catch.
**Files:** OrderTools.java
```
No new files
```
```
@Tool("Get order status by ID")
public String getStatus(String orderId) {
  try {
    return orderRepo.findById(orderId)
      .map(o -> o.getStatus().toString())
      .orElse("Order not found: " + orderId);
  } catch (Exception e) {
    return "Unable to retrieve order status. Please try again.";
  }
}
```
**Common Mistakes:**
- Letting exceptions propagate — breaks agent reasoning loop
**Testing:** Pass invalid orderId — verify friendly message returned not exception
**Expected:** Agent says "I couldn't retrieve that order" instead of stack trace

---

### Step 6: Log tool invocations

**Why:** Tool call logging is essential for debugging agent behavior.

**What:** Add aspect that logs every tool call with parameters and result.
**Files:** ToolLoggingAspect.java
```
aspect/ToolLoggingAspect.java
```
```
@Around("@annotation(dev.langchain4j.agent.tool.Tool)")
public Object logToolCall(ProceedingJoinPoint pjp) throws Throwable {
  log.info("TOOL CALL: {} args={}", pjp.getSignature().getName(), pjp.getArgs());
  Object result = pjp.proceed();
  log.info("TOOL RESULT: {}", result);
  return result;
}
```
**Common Mistakes:**
- No tool call logging — impossible to debug agent decisions
**Testing:** Send complex order request — verify full tool call chain in logs
**Expected:** Logs show: TOOL CALL: getStatus → TOOL RESULT: PENDING → TOOL CALL: cancelOrder

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/11-tool-calling

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
