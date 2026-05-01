# 🧠 Conversation Memory

> Per-user persistent memory with Redis

**Tier:** Pro | **Duration:** 35 min | **Difficulty:** Intermediate | **Category:** LangChain4j

**Tags:** Memory, Redis, MessageWindow, Multi-turn

---

## Description

Per-user persistent conversation memory using Redis-backed MessageWindowChatMemory.

---

## What You'll Learn

- MessageWindowChatMemory
- Redis ChatMemoryStore
- Memory isolation
- TTL strategies

---

## Tech Stack

- LangChain4j
- Redis
- Spring Boot 3.x
- Java 17

---

## Project

Multi-user chatbot with isolated persistent context

---

## Real-World Use Case

Tutoring app that remembers what each student learned and continues from there

---

## Prompt Guide

Memory injects automatically. System prompt should acknowledge it: "Use conversation history to build on what the student already knows."

---

## Steps

### Step 1: Add Redis to Docker Compose

**Why:** Redis is the fastest in-memory store for conversation history. Survives app restarts.

**What:** Add Redis service to docker-compose.yml.
**Files:** docker-compose.yml
```
docker-compose.yml
```
```
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  command: redis-server --appendonly yes  # persist to disk
```
**Common Mistakes:**
- Not enabling appendonly — memory lost on Redis restart
**Testing:** redis-cli ping → should return PONG
**Expected:** Redis running and accepting connections on port 6379

---

### Step 2: Configure Redis memory store

**Why:** RedissonClient connects Spring to Redis. MessageWindowChatMemory limits history to N messages.

**What:** Configure Redisson and build the memory store.
**Files:** MemoryConfig.java
```
config/MemoryConfig.java
```
```
@Bean
public ChatMemoryStore redisChatMemoryStore(RedissonClient redis) {
  return new RedisChatMemoryStore(redis);
}

@Bean
public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore store) {
  return memoryId -> MessageWindowChatMemory.builder()
    .maxMessages(20)
    .chatMemoryStore(store)
    .id(memoryId)
    .build();
}
```
**Common Mistakes:**
- Sharing one memory instance — all users see each other's messages
**Testing:** Set maxMessages=3, send 4 messages, verify only last 3 in history
**Expected:** Oldest message evicted when window is full

---

### Step 3: Wire memory to AI service

**Why:** @MemoryId parameter tells LangChain4j which memory store to use for this conversation.

**What:** Add @MemoryId to your service interface.
**Files:** TutorService.java
```
service/TutorService.java
```
```
interface TutorService {
  @SystemMessage("You are a patient tutor. Build on what the student already knows.")
  String teach(@MemoryId String studentId, @UserMessage String question);
}
```
**Common Mistakes:**
- Using session ID instead of user ID — memory lost on browser refresh
**Testing:** Send 3 messages as user-1, then ask "What was my first question?" — should remember
**Expected:** AI correctly recalls earlier messages for that specific user

---

### Step 4: Set TTL on memories

**Why:** Without expiry, Redis fills up with old conversations. TTL balances memory usage vs continuity.

**What:** Configure 24-hour TTL on conversation memories.
**Files:** RedisChatMemoryStore.java
```
store/RedisChatMemoryStore.java
```
```
@Override
public void updateMessages(Object memoryId, List<ChatMessage> messages) {
  String key = "memory:" + memoryId;
  redis.getBucket(key).set(serialize(messages));
  redis.getBucket(key).expire(Duration.ofHours(24)); // evict after 24h inactivity
}
```
**Common Mistakes:**
- No TTL — Redis uses unbounded memory, crashes under load
**Testing:** Set TTL=5 seconds, send message, wait 6 seconds, verify memory cleared
**Expected:** After TTL expires, conversation starts fresh (no history)

---

### Step 5: Test memory isolation

**Why:** Critical: user A must never see user B's conversation history.

**What:** Write test with 2 users having parallel conversations.
**Files:** MemoryIsolationTest.java
```
test/MemoryIsolationTest.java
```
```
@Test
void testUserIsolation() {
  tutorService.teach("alice", "My name is Alice");
  tutorService.teach("bob",   "My name is Bob");
  
  String aliceContext = tutorService.teach("alice", "What is my name?");
  String bobContext   = tutorService.teach("bob",   "What is my name?");
  
  assertThat(aliceContext).containsIgnoringCase("Alice");
  assertThat(bobContext).containsIgnoringCase("Bob");
  assertThat(aliceContext).doesNotContainIgnoringCase("Bob");
}
```
**Common Mistakes:**
- Not testing isolation — privacy bug ships to production
**Testing:** Run isolation test — must pass before deploying
**Expected:** Each user receives only their own context, never cross-contaminated

---

### Step 6: Monitor memory usage

**Why:** Production needs alerting when Redis approaches capacity.

**What:** Add Micrometer metrics for memory store size.
**Files:** MemoryMetrics.java
```
metrics/MemoryMetrics.java
```
```
@Scheduled(fixedRate = 60000)
public void recordMemoryMetrics() {
  long conversationCount = redis.getKeys().countByPattern("memory:*");
  meterRegistry.gauge("ai.memory.conversations", conversationCount);
}
```
**Common Mistakes:**
- No memory monitoring — OOM crash in production with no warning
**Testing:** Check /actuator/metrics/ai.memory.conversations — should show active conversation count
**Expected:** Metric visible in Prometheus/Grafana dashboard

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/12-conversation-memory

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
