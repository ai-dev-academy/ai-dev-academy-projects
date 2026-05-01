# 👥 Multi-Agent Systems

> Orchestrator + specialist agents in parallel

**Tier:** Advanced | **Duration:** 75 min | **Difficulty:** Advanced | **Category:** Agents & MCP

**Tags:** Multi-Agent, Orchestrator, Parallel, LangGraph4j

---

## Description

Orchestrator delegates to specialist agents, synthesizes results.

---

## What You'll Learn

- Orchestrator pattern
- Parallel with CompletableFuture
- Agent aggregation
- Failure handling

---

## Tech Stack

- LangChain4j
- LangGraph4j
- CompletableFuture
- Java 17

---

## Project

Research + write + review pipeline — 3 agents collaborate

---

## Real-World Use Case

Content team: Researcher finds facts, Writer drafts, Editor improves — fully automated

---

## Prompt Guide

Each agent gets specialist persona. Editor: "Improve clarity and fix errors. Return improved text only."

---

## Steps

### Step 1: Define agent interfaces

**Why:** Each specialist agent has a narrow focused job defined by its @SystemMessage.

**What:** Create 3 distinct agent interfaces.
**Files:** ResearcherAgent.java, WriterAgent.java, EditorAgent.java
```
agents/
```
```
interface ResearcherAgent {
  @SystemMessage("You are a fact-finder. Return only verified facts with sources.")
  String research(@UserMessage String topic);
}
interface WriterAgent {
  @SystemMessage("You are a technical writer. Use provided facts to write clear prose.")
  String write(@UserMessage String factsAndTopic);
}
interface EditorAgent {
  @SystemMessage("You are an editor. Improve clarity and fix errors. Return improved text only.")
  String edit(@UserMessage String draft);
}
```
**Common Mistakes:**
- Generalist agents doing everything — defeats specialist pattern
**Testing:** Call each agent independently — verify they stay in their lane
**Expected:** Researcher returns facts only, Writer prose only, Editor improved text only

---

### Step 2: Build agent beans

**Why:** Each agent needs its own ChatLanguageModel configuration.

**What:** Create beans for all 3 agents.
**Files:** AgentConfig.java
```
config/AgentConfig.java
```
```
@Bean ResearcherAgent researcher(ChatLanguageModel model) {
  return AiServices.builder(ResearcherAgent.class).chatLanguageModel(model).build();
}
@Bean WriterAgent writer(ChatLanguageModel model) {
  return AiServices.builder(WriterAgent.class).chatLanguageModel(model).build();
}
@Bean EditorAgent editor(ChatLanguageModel model) {
  return AiServices.builder(EditorAgent.class).chatLanguageModel(model).build();
}
```
**Common Mistakes:**
- All agents sharing one ChatLanguageModel bean — rate limit contention
**Testing:** Each bean created at startup with correct system prompt
**Expected:** 3 distinct agent beans in Spring context

---

### Step 3: Build orchestrator

**Why:** Orchestrator sequences agent calls and passes outputs between them.

**What:** Create PipelineOrchestrator that runs research → write → edit.
**Files:** PipelineOrchestrator.java
```
orchestrator/PipelineOrchestrator.java
```
```
public String runPipeline(String topic) {
  // Step 1: Research
  String facts = researcher.research(topic);
  
  // Step 2: Write (inject facts)
  String draft = writer.write("Topic: " + topic + "\nFacts: " + facts);
  
  // Step 3: Edit
  return editor.edit(draft);
}
```
**Common Mistakes:**
- Not passing facts to writer — writer hallucinates without context
**Testing:** Run pipeline on "Java 21 virtual threads" — verify factual article produced
**Expected:** Final output is edited prose incorporating researched facts

---

### Step 4: Parallelize independent steps

**Why:** Research subtopics can happen in parallel — 3 parallel searches takes same time as 1.

**What:** Use CompletableFuture.allOf() for parallel research.
**Files:** PipelineOrchestrator.java
```
No new files
```
```
public String parallelResearch(List<String> subtopics) {
  List<CompletableFuture<String>> futures = subtopics.stream()
    .map(subtopic -> CompletableFuture.supplyAsync(() -> researcher.research(subtopic)))
    .toList();
  
  CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  
  String allFacts = futures.stream()
    .map(CompletableFuture::join)
    .collect(Collectors.joining("\n"));
  return writer.write(allFacts);
}
```
**Common Mistakes:**
- Unbounded parallelism — 20 concurrent LLM calls hits rate limits
**Testing:** Time sequential vs parallel research on 3 subtopics — parallel should be 3x faster
**Expected:** Parallel completes in same time as single research call, not 3x

---

### Step 5: Handle agent failures

**Why:** One agent failing should not crash the whole pipeline.

**What:** Add compensation logic for agent failures.
**Files:** PipelineOrchestrator.java
```
No new files
```
```
public String runPipelineWithFallback(String topic) {
  String facts;
  try {
    facts = researcher.research(topic);
  } catch (Exception e) {
    log.warn("Researcher failed, using basic facts: {}", e.getMessage());
    facts = "Basic facts about: " + topic; // minimal fallback
  }
  return writer.write("Topic: " + topic + "\nFacts: " + facts);
}
```
**Common Mistakes:**
- Bubbling agent exception to user — show fallback content instead
**Testing:** Force researcher to throw exception — verify pipeline completes with fallback
**Expected:** Article produced even when researcher fails, using fallback facts

---

### Step 6: Build pipeline endpoint

**Why:** Expose the pipeline as an API for frontend or other services.

**What:** Create POST /pipeline endpoint.
**Files:** PipelineController.java
```
controller/PipelineController.java
```
```
@PostMapping("/pipeline")
public Map<String, String> runPipeline(@RequestBody String topic) {
  long start = System.currentTimeMillis();
  String article = orchestrator.runPipeline(topic);
  long duration = System.currentTimeMillis() - start;
  return Map.of("article", article, "generatedIn", duration + "ms");
}
```
**Common Mistakes:**
- Synchronous endpoint for 30-second pipeline — client times out
**Testing:** curl -X POST http://localhost:8080/pipeline \
  -d '"Spring Boot 3 virtual threads"'
**Expected:** Full article returned in <60 seconds with generation time in response

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/16-multi-agent-systems

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
