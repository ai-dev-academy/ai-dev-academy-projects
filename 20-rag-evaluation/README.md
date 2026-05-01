# ✅ RAG Evaluation (RAGAS)

> Measure faithfulness, relevance and quality in CI/CD

**Tier:** Advanced | **Duration:** 45 min | **Difficulty:** Advanced | **Category:** Production

**Tags:** RAGAS, Evaluation, CI/CD, Quality Gates

---

## Description

Automated RAG quality evaluation using RAGAS metrics as a CI gate.

---

## What You'll Learn

- RAGAS metrics
- Golden Q&A datasets
- Evals in GitHub Actions
- Quality gates

---

## Tech Stack

- RAGAS
- JUnit 5
- GitHub Actions
- Java 17

---

## Project

Automated RAG eval CI pipeline — deploy blocked if faithfulness drops below 0.85

---

## Real-World Use Case

RAG-powered support bot where every deployment tested against 50 known questions

---

## Prompt Guide

Faithfulness eval: "Given context: {context} and answer: {answer} — does answer contain ONLY info from context? Score 0.0-1.0."

---

## Steps

### Step 1: Define RAGAS metrics

**Why:** You can't improve what you can't measure. RAGAS gives objective quality scores.

**What:** Implement 3 core RAGAS metrics.
**Files:** RagasMetrics.java
```
eval/RagasMetrics.java
```
```
// Faithfulness: Does answer stick to retrieved context?
// Score 0.0 = hallucinated, 1.0 = fully grounded

// Answer Relevancy: Is the answer relevant to the question?
// Score 0.0 = irrelevant, 1.0 = perfectly relevant

// Context Precision: Are retrieved chunks actually useful?
// Score 0.0 = wrong chunks, 1.0 = perfect retrieval
```
**Common Mistakes:**
- Implementing only one metric — each catches different failure modes
**Testing:** Manually score 5 Q&A pairs — compare with RAGAS automated scores
**Expected:** RAGAS scores within 10% of manual human scores

---

### Step 2: Build golden dataset

**Why:** Golden set = ground truth. Without it you can't measure regression.

**What:** Create 20-50 curated Q&A pairs from your actual documents.
**Files:** golden-dataset.json
```
src/test/resources/golden-dataset.json
```
```
[
  {
    "question": "How many vacation days do employees get?",
    "expected_keywords": ["15 days", "per year"],
    "ground_truth": "Employees receive 15 vacation days per year"
  },
  {
    "question": "What is the remote work policy?",
    "expected_keywords": ["3 days", "per week"],
    "ground_truth": "Employees may work remotely up to 3 days per week"
  }
]
```
**Common Mistakes:**
- Only 5 Q&A pairs — not statistically meaningful, high variance
**Testing:** Verify every question is answerable from ingested documents
**Expected:** All 20+ questions have clear answers in the knowledge base

---

### Step 3: Implement faithfulness scorer

**Why:** Faithfulness catches hallucinations — the most dangerous RAG failure.

**What:** Use LLM-as-judge to score if answer is grounded in context.
**Files:** FaithfulnessScorer.java
```
eval/FaithfulnessScorer.java
```
```
public double scoreFaithfulness(String context, String answer) {
  String prompt = """
    Given context: %s
    Answer: %s
    
    Does the answer contain ONLY information from the context?
    Score 0.0 (hallucinated) to 1.0 (fully grounded).
    Reply with ONLY a decimal number.
    """.formatted(context, answer);
  
  String score = chatClient.prompt().user(prompt).call().content();
  return Double.parseDouble(score.trim());
}
```
**Common Mistakes:**
- Using same LLM that generated the answer to score faithfulness — biased
**Testing:** Test with answer containing hallucinated fact — should score <0.5
**Expected:** Hallucinated answers score <0.5, grounded answers score >0.9

---

### Step 4: Build evaluation runner

**Why:** Automated runner executes all golden set Q&A pairs and computes aggregate scores.

**What:** Create EvaluationRunner that scores all golden set questions.
**Files:** EvaluationRunner.java
```
eval/EvaluationRunner.java
```
```
public EvaluationReport runEvaluation(List<QAPair> goldenSet) {
  var scores = goldenSet.stream().map(qa -> {
    String answer = ragService.ask(qa.question());
    String context = ragService.getLastRetrievedContext();
    double faithfulness = faithfulnessScorer.score(context, answer);
    double relevancy = relevancyScorer.score(qa.question(), answer);
    return new Score(qa.question(), faithfulness, relevancy);
  }).toList();
  
  return new EvaluationReport(
    scores.stream().mapToDouble(Score::faithfulness).average().orElse(0),
    scores.stream().mapToDouble(Score::relevancy).average().orElse(0)
  );
}
```
**Common Mistakes:**
- Running evaluation against production — use test environment with same documents
**Testing:** Run on 5-question subset first — verify scores match manual assessment
**Expected:** Evaluation report shows per-question and aggregate faithfulness + relevancy scores

---

### Step 5: Add quality gate JUnit test

**Why:** CI fails if faithfulness drops below threshold — blocks bad deployments automatically.

**What:** Create JUnit test that runs evaluation and asserts minimum scores.
**Files:** RagQualityGateTest.java
```
test/RagQualityGateTest.java
```
```
@Test
@Tag("quality-gate")
void ragMeetsQualityThreshold() {
  var goldenSet = loadGoldenDataset();
  var report = evaluationRunner.runEvaluation(goldenSet);
  
  assertThat(report.avgFaithfulness())
    .as("Faithfulness must be >= 0.85 to deploy")
    .isGreaterThanOrEqualTo(0.85);
  
  assertThat(report.avgRelevancy())
    .as("Relevancy must be >= 0.80 to deploy")
    .isGreaterThanOrEqualTo(0.80);
}
```
**Common Mistakes:**
- Quality gate in optional test profile — developers skip it
**Testing:** Intentionally degrade RAG (wrong chunk size) — verify CI fails
**Expected:** Test failure message: "Faithfulness 0.62 is below required 0.85 — deployment blocked"

---

### Step 6: Add to GitHub Actions CI

**Why:** Manual eval is forgettable. CI runs it automatically on every PR.

**What:** Add eval job to .github/workflows/ci.yml.
**Files:** .github/workflows/ci.yml
```
.github/workflows/
```
```
jobs:
  rag-quality-gate:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: pgvector/pgvector:pg16
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: {java-version: '17'}
      - run: mvn test -Dgroups=quality-gate
        env:
          OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
```
**Common Mistakes:**
- Not caching Maven dependencies — 5 minute download on every CI run
**Testing:** Open PR → verify quality gate job runs → check score in PR comment
**Expected:** Every PR shows RAG quality scores as a required status check

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/20-rag-evaluation

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
