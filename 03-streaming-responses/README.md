# ⚡ Streaming Responses

> Token-by-token SSE streaming with Spring WebFlux

**Tier:** Free | **Duration:** 25 min | **Difficulty:** Beginner | **Category:** Foundations

**Tags:** SSE, WebFlux, Flux, Streaming

---

## Description

Implement real-time streaming responses using Server-Sent Events and Spring WebFlux Flux streams.

---

## What You'll Learn

- How token streaming works end-to-end
- Spring WebFlux Flux
- Frontend SSE consumption
- Backpressure and error handling

---

## Tech Stack

- Spring Boot 3.x
- Spring WebFlux
- SSE
- React

---

## Project

Live Chat UI — React frontend + Spring WebFlux streaming backend

---

## Real-World Use Case

A code review assistant that streams suggestions as it analyzes your code

---

## Prompt Guide

No special prompt for streaming — focus on keeping responses concise to minimize time-to-first-token.

---

## Steps

### Step 1: Add WebFlux dependency

**Why:** WebFlux enables reactive/streaming responses. Without it Spring uses blocking I/O.

**What:** Add spring-boot-starter-webflux to pom.xml.
**Files:** pom.xml
```
No new files
```
```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
**Common Mistakes:**
- Adding both web and webflux — pick one
**Testing:** Check startup logs for "Netty started" instead of "Tomcat started"
**Expected:** Application starts on Netty server (reactive server)

---

### Step 2: Create streaming endpoint

**Why:** Returning Flux<String> tells Spring to stream data as it arrives instead of buffering everything.

**What:** Change return type to Flux and add produces=text/event-stream.
**Files:** ChatController.java
```
No new files
```
```
@GetMapping(
  value = "/stream",
  produces = MediaType.TEXT_EVENT_STREAM_VALUE
)
public Flux<String> stream(@RequestParam String message) {
    return chatClient.prompt()
        .user(message)
        .stream()
        .content();
}
```
**Common Mistakes:**
- Using @PostMapping for SSE — browsers use GET for EventSource
**Testing:** Open browser: http://localhost:8080/ai/stream?message=Hello
**Expected:** Text appears word-by-word in browser network tab

---

### Step 3: Build React EventSource client

**Why:** EventSource is the browser API for receiving SSE. It auto-reconnects on failure.

**What:** Create a React component that connects to your stream endpoint.
**Files:** src/ChatStream.jsx
```
frontend/src/ChatStream.jsx
```
```
const [response, setResponse] = useState('');

const startStream = () => {
  const es = new EventSource(
    `/api/stream?message=${encodeURIComponent(input)}`
  );
  es.onmessage = (e) => {
    setResponse(prev => prev + e.data);
  };
  es.onerror = () => es.close();
};
```
**Common Mistakes:**
- Not calling es.close() when done — connection stays open forever
**Testing:** Open DevTools → Network → EventStream tab — see tokens arriving
**Expected:** Words appear one by one in the UI as model generates them

---

### Step 4: Handle stream errors

**Why:** Network drops, model errors, and timeouts must be caught or users see blank screens.

**What:** Add onError handler and reconnection logic.
**Files:** ChatStream.jsx
```
No new files
```
```
es.onerror = (err) => {
  setError('Stream interrupted. Retrying...');
  es.close();
  setTimeout(() => startStream(), 2000); // retry after 2s
};
```
**Common Mistakes:**
- Infinite retry loop without backoff — hammers your server
**Testing:** Kill the server mid-stream — verify UI shows error message
**Expected:** User sees "Stream interrupted" then reconnects automatically

---

### Step 5: Add loading state

**Why:** Users need visual feedback while waiting for first token — blank screen feels broken.

**What:** Show a pulsing indicator before first token arrives.
**Files:** ChatStream.jsx
```
No new files
```
```
{isLoading && !response && (
  <div className="flex gap-1">
    <span className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" />
    <span className="w-2 h-2 bg-blue-500 rounded-full animate-bounce delay-100" />
    <span className="w-2 h-2 bg-blue-500 rounded-full animate-bounce delay-200" />
  </div>
)}
```
**Common Mistakes:**
- Showing spinner after tokens start — should hide immediately on first token
**Testing:** Watch loading indicator — should disappear the moment first word arrives
**Expected:** Smooth transition from loading dots to streaming text

---

### Step 6: Test full flow

**Why:** End-to-end testing catches timing issues invisible in unit tests.

**What:** Test streaming with slow network, mobile, and concurrent users.
**Files:** StreamTest.java
```
test/StreamTest.java
```
```
@Test
void testStreamingResponse() {
    webTestClient.get()
        .uri("/ai/stream?message=Hello")
        .accept(MediaType.TEXT_EVENT_STREAM)
        .exchange()
        .expectStatus().isOk()
        .returnResult(String.class)
        .getResponseBody()
        .take(5)
        .collectList()
        .block();
}
```
**Common Mistakes:**
- Not using WebTestClient for reactive endpoints — MockMvc won't work
**Testing:** Run test 3 times — verify consistent token delivery
**Expected:** Each run receives at least 5 streaming tokens successfully

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/03-streaming-responses

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
