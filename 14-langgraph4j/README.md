# 🕸️ LangGraph4j

> Stateful workflows with graph-based orchestration

**Tier:** Advanced | **Duration:** 70 min | **Difficulty:** Advanced | **Category:** Agents & MCP

**Tags:** LangGraph4j, State Machine, Workflows, Human-in-Loop

---

## Description

Build stateful AI workflows using LangGraph4j directed graphs.

---

## What You'll Learn

- Node/edge model
- State management
- Conditional branching
- Human-in-loop

---

## Tech Stack

- LangGraph4j
- Spring Boot 3.x
- Java 17

---

## Project

Content approval workflow — AI draft → human review → publish

---

## Real-World Use Case

Marketing team workflow: AI writes social posts, human approves, auto-schedules

---

## Prompt Guide

Each node has focused prompt. Draft: "Write a tweet about {topic}." Review: "Rate 1-10. Reply JSON: {score, feedback}"

---

## Steps

### Step 1: Add LangGraph4j dependency

**Why:** LangGraph4j brings stateful graph workflows to Java.

**What:** Add dependency.
**Files:** pom.xml
```
No new files
```
```
<dependency>
  <groupId>dev.langchain4j</groupId>
  <artifactId>langgraph4j-core</artifactId>
  <version>0.1.0</version>
</dependency>
```
**Common Mistakes:**
- Confusing with LangChain4j — different library
**Testing:** mvn compile — no dependency errors
**Expected:** Project compiles with LangGraph4j on classpath

---

### Step 2: Define workflow state

**Why:** State is shared between all nodes. Every step reads and updates the same state object.

**What:** Create WorkflowState record.
**Files:** WorkflowState.java
```
workflow/WorkflowState.java
```
```
public record WorkflowState(
  String topic,
  String draftContent,
  int reviewScore,
  String reviewFeedback,
  String status
) {
  public WorkflowState withDraft(String d) { return new WorkflowState(topic, d, reviewScore, reviewFeedback, status); }
  public WorkflowState withReview(int s, String f) { return new WorkflowState(topic, draftContent, s, f, status); }
  public WorkflowState withStatus(String s) { return new WorkflowState(topic, draftContent, reviewScore, reviewFeedback, s); }
}
```
**Common Mistakes:**
- Mutable state — concurrency bugs in parallel workflows
**Testing:** Create state, call withDraft() — verify immutable copy returned
**Expected:** Original state unchanged, new state has updated draft field

---

### Step 3: Create workflow nodes

**Why:** Each node is a focused function that reads state, does one thing, returns updated state.

**What:** Build Draft, Review and Publish nodes.
**Files:** WorkflowNodes.java
```
workflow/WorkflowNodes.java
```
```
Function<WorkflowState, WorkflowState> draftNode = state -> {
  String draft = chatClient.prompt()
    .user("Write a tweet about: " + state.topic())
    .call().content();
  return state.withDraft(draft);
};

Function<WorkflowState, WorkflowState> reviewNode = state -> {
  String result = chatClient.prompt()
    .user("Rate this tweet 1-10 as JSON {score,feedback}: " + state.draftContent())
    .call().content();
  // parse JSON result
  return state.withReview(score, feedback);
};
```
**Common Mistakes:**
- Node doing too much — one responsibility per node
**Testing:** Run draftNode alone — verify state.draftContent() populated
**Expected:** Each node produces correct state transformation independently

---

### Step 4: Build the graph

**Why:** The graph defines execution order and conditional routing.

**What:** Connect nodes with edges and conditional branches.
**Files:** ContentWorkflow.java
```
workflow/ContentWorkflow.java
```
```
var graph = StateGraph.<WorkflowState>builder()
  .addNode("draft",   draftNode)
  .addNode("review",  reviewNode)
  .addNode("publish", publishNode)
  .addNode("revise",  reviseNode)
  .addEdge(START, "draft")
  .addEdge("draft", "review")
  .addConditionalEdges("review", state ->
    state.reviewScore() >= 7 ? "publish" : "revise"
  )
  .addEdge("revise", "review") // loop back
  .addEdge("publish", END)
  .build();
```
**Common Mistakes:**
- Missing edge from END node — graph hangs
**Testing:** Run workflow with easy topic — verify graph reaches publish node
**Expected:** State flows: draft → review → (score>=7) → publish → DONE

---

### Step 5: Add human-in-loop

**Why:** Some decisions need human judgment before proceeding.

**What:** Add interrupt point before publish.
**Files:** ContentWorkflow.java
```
No new files
```
```
// Interrupt before publishing for human approval
graph.addNode("await_approval", state -> {
  approvalService.requestApproval(state.draftContent());
  return state.withStatus("AWAITING_APPROVAL");
});

// Human calls /approve endpoint to resume workflow
@PostMapping("/approve/{workflowId}")
public void approve(@PathVariable String workflowId) {
  workflowEngine.resume(workflowId);
}
```
**Common Mistakes:**
- No timeout on human approval — workflow hangs forever
**Testing:** Start workflow, call /approve — verify workflow continues
**Expected:** Workflow pauses at approval node, resumes on human action

---

### Step 6: Monitor workflow executions

**Why:** Production workflows need visibility into status, failures, and history.

**What:** Add workflow tracking with Spring Data.
**Files:** WorkflowTracker.java
```
tracking/WorkflowTracker.java
```
```
@Entity
public class WorkflowExecution {
  private String id;
  private String currentNode;
  private String status; // RUNNING|COMPLETED|FAILED|AWAITING
  private LocalDateTime startTime;
  private LocalDateTime completedTime;
  private String finalContent;
}
```
**Common Mistakes:**
- No workflow history — can't audit what content was published
**Testing:** GET /workflows — verify all executions listed with status
**Expected:** Dashboard shows active, completed, and awaiting-approval workflows

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/14-langgraph4j

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
