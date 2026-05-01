# ✍️ Prompt Engineering

> System, user and assistant roles — craft effective prompts

**Tier:** Free | **Duration:** 30 min | **Difficulty:** Beginner | **Category:** Foundations

**Tags:** Prompts, System Message, Few-Shot, Templates

---

## Description

Learn the engineering discipline of crafting prompts — system/user/assistant split, few-shot examples and output schemas.

---

## What You'll Learn

- System vs user vs assistant roles
- Few-shot prompting
- Chain-of-thought
- PromptTemplate in Spring AI

---

## Tech Stack

- Spring Boot 3.x
- Spring AI
- PromptTemplate
- Java 17

---

## Project

Smart Email Classifier — classifies support tickets automatically

---

## Real-World Use Case

An IT helpdesk that auto-categorizes incoming tickets as Bug/Feature/Billing/General

---

## Prompt Guide

System: "Classify the email into exactly one: BUG | FEATURE | BILLING | GENERAL. Reply with only the category word." — Constrain the output strictly.

---

## Steps

### Step 1: Understand message roles

**Why:** Roles tell the LLM who is speaking and what authority they have.

**What:** System = AI persona, User = human input, Assistant = AI response
**Files:** ChatController.java
```
No new files needed
```
```
chatClient.prompt()
    .system("You are an expert email classifier.")
    .user(emailContent)
    .call()
    .content();
```
**Common Mistakes:**
- Mixing business rules into user message instead of system
**Testing:** Send same email with different system prompts — watch output change
**Expected:** System prompt change dramatically affects classification accuracy

---

### Step 2: Write constrained output prompts

**Why:** Without constraints LLMs write essays. You need exactly "BUG" not "This seems to be a bug report..."

**What:** Add explicit format instructions to the system prompt.
**Files:** prompts/classifier-system.txt
```
resources/prompts/
```
```
String systemPrompt = """
    Classify the email into EXACTLY one category:
    BUG | FEATURE | BILLING | GENERAL
    
    Rules:
    - Reply with ONLY the category word
    - No explanation, no punctuation
    - If unsure, use GENERAL
    """;
```
**Common Mistakes:**
- Not specifying "only one word" — LLM adds explanations
**Testing:** Test 10 emails and verify each response is exactly one of the 4 words
**Expected:** 100% of responses are exactly BUG, FEATURE, BILLING, or GENERAL

---

### Step 3: Add few-shot examples

**Why:** Examples in the prompt dramatically improve accuracy — the model learns your specific classification logic.

**What:** Include 3-5 example input/output pairs in the prompt.
**Files:** prompts/classifier-system.txt
```
No new files
```
```
String systemPrompt = """
    Classify emails. Reply with ONE word only.
    
    Examples:
    Email: "The login button is broken"
    Category: BUG
    
    Email: "Please add dark mode"  
    Category: FEATURE
    
    Email: "I was charged twice"
    Category: BILLING
    """;
```
**Common Mistakes:**
- Using unrealistic examples that don't match real data
**Testing:** Compare accuracy with vs without examples on same test set
**Expected:** Few-shot improves accuracy from ~70% to ~95%

---

### Step 4: Use Spring AI PromptTemplate

**Why:** PromptTemplate lets you inject variables into prompts safely — no string concatenation bugs.

**What:** Replace hardcoded prompts with template placeholders.
**Files:** EmailClassifierService.java
```
service/EmailClassifierService.java
```
```
String template = """
    Classify this {type} message:
    {content}
    """;

PromptTemplate pt = new PromptTemplate(template);
Prompt prompt = pt.create(Map.of(
    "type", "support email",
    "content", emailBody
));
```
**Common Mistakes:**
- Forgetting curly braces around variable names
- Typos in variable name vs Map key
**Testing:** Template injection test: verify variables replaced correctly in logs
**Expected:** Prompt logs show actual email content not literal {content}

---

### Step 5: Build classification endpoint

**Why:** Wrap the classifier in a REST endpoint so any system can call it.

**What:** Create /classify endpoint returning structured JSON.
**Files:** ClassifierController.java
```
controller/ClassifierController.java
```
```
record ClassificationResult(String category, String confidence) {}

@PostMapping("/classify")
public ClassificationResult classify(@RequestBody String email) {
    String category = classifierService.classify(email);
    return new ClassificationResult(category, "HIGH");
}
```
**Common Mistakes:**
- Returning plain string instead of JSON object
**Testing:** curl -X POST http://localhost:8080/classify \
  -d '"My payment was charged twice"'
**Expected:** {"category":"BILLING","confidence":"HIGH"}

---

### Step 6: Test edge cases

**Why:** Real emails are messy — mixed languages, typos, ambiguous content.

**What:** Test the classifier against difficult real-world emails.
**Files:** ClassifierTest.java
```
test/ClassifierTest.java
```
```
@Test
void testAmbiguousEmail() {
    String email = "I want to report a problem AND suggest improvement";
    String result = classifierService.classify(email);
    assertThat(result).isIn("BUG", "FEATURE", "GENERAL");
}
```
**Common Mistakes:**
- Only testing happy path — ignoring edge cases
**Testing:** Run 20 diverse test emails including mixed language and typos
**Expected:** Classifier handles all cases without throwing exceptions

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/02-prompt-engineering

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
