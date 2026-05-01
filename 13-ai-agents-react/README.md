# 🤖 AI Agents (ReAct)

> Reasoning + Acting loop — autonomous multi-step agents

**Tier:** Advanced | **Duration:** 60 min | **Difficulty:** Advanced | **Category:** Agents & MCP

**Tags:** ReAct, Agent Loop, Planning, LangChain4j

---

## Description

Build ReAct agents that plan, call tools, observe results and iterate.

---

## What You'll Learn

- ReAct: Thought→Action→Observation
- Agent loop
- Max iterations
- Debugging traces

---

## Tech Stack

- LangChain4j
- Spring Boot 3.x
- Web Search API
- Java 17

---

## Project

Research agent — searches web and synthesizes a report

---

## Real-World Use Case

Competitive intelligence agent that researches a company and produces SWOT analysis

---

## Prompt Guide

System: "Think step by step. For each step: state Thought, choose Action, observe result, decide next step. Stop when goal complete."

---

## Steps

### Step 1: Understand ReAct pattern

**Why:** ReAct enables multi-step autonomous reasoning — the foundation of all AI agents.

**What:** Learn Thought → Action → Observation cycle.
**Files:** README.md
```
No code needed — conceptual
```
```
// ReAct loop (pseudocode):
// 1. LLM generates Thought: "I need to search for X"
// 2. LLM calls Action: webSearch("X")
// 3. Agent observes result
// 4. LLM generates next Thought: "Now I need to read Y"
// 5. Repeat until: "I have enough to answer"
```
**Common Mistakes:**
- Confusing ReAct with simple function calling — ReAct reasons between calls
**Testing:** Trace a 3-step research task — identify each Thought/Action/Observation
**Expected:** Clear Thought→Action→Observation pattern visible in agent logs

---

### Step 2: Build research tools

**Why:** Research agent needs tools to search, read pages, and save notes.

**What:** Implement 3 tools: webSearch, readUrl, saveNote.
**Files:** ResearchTools.java
```
tools/ResearchTools.java
```
```
class ResearchTools {
  @Tool("Search the web for current information on a topic")
  public String webSearch(String query) {
    return searchApiClient.search(query).getTopResults();
  }
  
  @Tool("Read the full content of a webpage by URL")
  public String readUrl(String url) {
    return httpClient.get(url).bodyAsText().substring(0, 2000);
  }
  
  @Tool("Save a key finding to the research notes")
  public String saveNote(String finding) {
    notes.add(finding);
    return "Saved: " + finding;
  }
}
```
**Common Mistakes:**
- readUrl with no length limit — 50KB pages overflow context window
**Testing:** Call each tool directly — verify they return useful data
**Expected:** webSearch returns 3-5 results, readUrl extracts readable text, saveNote persists

---

### Step 3: Configure agent with safety limits

**Why:** Without limits, runaway agent makes 100 API calls and costs $50.

**What:** Set maxIterations and timeout.
**Files:** AgentConfig.java
```
config/AgentConfig.java
```
```
return AiServices.builder(ResearchAgent.class)
  .chatLanguageModel(model)
  .tools(new ResearchTools(searchClient, httpClient))
  .maxSequentialToolsInvocations(10) // max 10 tool calls
  .build();
```
**Common Mistakes:**
- No iteration limit — agents can loop indefinitely
**Testing:** Give impossible research task — verify agent stops at max iterations
**Expected:** Agent stops with "Could not complete research within iteration limit" message

---

### Step 4: Run first research task

**Why:** End-to-end test proves the agent loop works.

**What:** Ask agent to research a company.
**Files:** AgentController.java
```
controller/AgentController.java
```
```
@PostMapping("/research")
public String research(@RequestBody String topic) {
  return researchAgent.research(
    "Research this topic thoroughly and provide a 3-point summary: " + topic
  );
}
```
**Common Mistakes:**
- Not logging intermediate steps — can't debug what went wrong
**Testing:** curl -X POST http://localhost:8080/research \
  -d '"Spring AI framework key features"'
**Expected:** 3-point summary based on current web search results — not training data

---

### Step 5: Add reasoning trace logging

**Why:** Agent reasoning is a black box without logging. Traces reveal planning quality.

**What:** Log every Thought/Action/Observation in the agent loop.
**Files:** AgentTraceLogger.java
```
logging/AgentTraceLogger.java
```
```
// LangChain4j agent listener
class TraceLogger implements AgentEventListener {
  public void onThought(String thought) {
    log.info("🤔 THOUGHT: {}", thought);
  }
  public void onAction(ToolExecution execution) {
    log.info("⚡ ACTION: {}({})", execution.toolName(), execution.arguments());
  }
  public void onObservation(String result) {
    log.info("👁️ OBSERVE: {}", result.substring(0, Math.min(200, result.length())));
  }
}
```
**Common Mistakes:**
- Logging full observation — 10KB per step fills disk fast
**Testing:** Send research task — verify emoji-prefixed trace in logs
**Expected:** Full reasoning trace visible: 🤔 → ⚡ → 👁️ → 🤔 → ⚡ → 👁️ → answer

---

### Step 6: Handle agent failures

**Why:** Agents fail when tools error or model hallucinates. Graceful failure is essential.

**What:** Add timeout and exception handling around agent calls.
**Files:** AgentController.java
```
No new files
```
```
@PostMapping("/research")
public ResponseEntity<String> research(@RequestBody String topic) {
  try {
    String result = CompletableFuture
      .supplyAsync(() -> agent.research(topic))
      .get(30, TimeUnit.SECONDS); // 30s max
    return ResponseEntity.ok(result);
  } catch (TimeoutException e) {
    return ResponseEntity.status(504).body("Research timed out. Please try a simpler query.");
  }
}
```
**Common Mistakes:**
- No timeout — user waits forever if agent loops
**Testing:** Give complex task that exceeds 30 seconds — verify 504 with message
**Expected:** User receives timeout message, not hanging connection

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/13-ai-agents-react

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
