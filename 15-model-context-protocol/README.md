# 🔌 Model Context Protocol

> Build MCP servers that any LLM can call

**Tier:** Advanced | **Duration:** 55 min | **Difficulty:** Advanced | **Category:** Agents & MCP

**Tags:** MCP, Spring AI, Tool Registry, Protocol

---

## Description

Implement Anthropic's Model Context Protocol — expose Spring Boot domain as universal AI tools.

---

## What You'll Learn

- MCP server/client architecture
- Spring AI 1.0 MCP
- Tool discovery
- Connect Claude Desktop

---

## Tech Stack

- Spring AI 1.x
- MCP Protocol
- Spring Boot 3.x
- Java 17

---

## Project

Spring Boot MCP server exposing order/inventory tools

---

## Real-World Use Case

Your microservice becomes callable by Claude Desktop, Cursor and any MCP client

---

## Prompt Guide

MCP tool descriptions are your prompts. Write: "Retrieves real-time inventory for SKU. Returns: quantity, warehouse, reorder threshold."

---

## Steps

### Step 1: Add MCP server dependency

**Why:** MCP is becoming the universal protocol for LLM tool calling.

**What:** Add Spring AI MCP server starter.
**Files:** pom.xml
```
No new files
```
```
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-mcp-server-spring-boot-starter</artifactId>
</dependency>
```
**Common Mistakes:**
- Using client starter instead of server starter
**Testing:** mvn compile — no dependency errors
**Expected:** Project compiles with MCP server on classpath

---

### Step 2: Create MCP tools

**Why:** @Tool annotation on Spring beans auto-registers them as MCP tools.

**What:** Annotate service methods for MCP exposure.
**Files:** OrderMcpTools.java
```
mcp/OrderMcpTools.java
```
```
@Component
public class OrderMcpTools {
  @Tool("Get real-time order status. Returns current status and estimated delivery.")
  public String getOrderStatus(String orderId) {
    return orderService.getStatus(orderId).toString();
  }
  
  @Tool("List all inventory items below reorder threshold.")
  public List<String> getLowInventory() {
    return inventoryService.getLowStock();
  }
}
```
**Common Mistakes:**
- Tools that throw exceptions — MCP clients crash
**Testing:** Start server — check /mcp/tools endpoint lists all tools
**Expected:** GET /mcp/tools returns JSON with tool names and descriptions

---

### Step 3: Configure MCP transport

**Why:** HTTP+SSE transport lets remote clients connect — not just local processes.

**What:** Configure server transport in application.properties.
**Files:** application.properties
```
No new files
```
```
spring.ai.mcp.server.transport=http-sse
spring.ai.mcp.server.port=8080
spring.ai.mcp.server.name=order-management-server
spring.ai.mcp.server.version=1.0.0
```
**Common Mistakes:**
- Using stdio for remote clients — only works locally
**Testing:** curl http://localhost:8080/mcp/sse — verify SSE stream opens
**Expected:** SSE connection established, server info returned as first event

---

### Step 4: Connect Claude Desktop

**Why:** Claude Desktop natively supports MCP — connect and call your Spring Boot APIs from Claude.

**What:** Configure Claude Desktop mcp_servers.json.
**Files:** claude_desktop_config.json
```
~/.config/claude/claude_desktop_config.json
```
```
{
  "mcpServers": {
    "order-management": {
      "url": "http://localhost:8080/mcp/sse",
      "transport": "sse"
    }
  }
}
```
**Common Mistakes:**
- Wrong port or URL — Claude silently fails to connect
**Testing:** Open Claude Desktop → type "What orders have low inventory?" — verify tool called
**Expected:** Claude calls getLowInventory() and returns real data from your Spring Boot app

---

### Step 5: Add tool input validation

**Why:** External clients (Claude, Cursor) pass user-entered parameters. Validate before DB queries.

**What:** Add validation to all tool methods.
**Files:** OrderMcpTools.java
```
No new files
```
```
@Tool("Get order status by ID")
public String getOrderStatus(String orderId) {
  if (orderId == null || !orderId.matches("[A-Z0-9-]+")) {
    return "Invalid order ID format. Use format: ORD-12345";
  }
  return orderService.getStatus(orderId).toString();
}
```
**Common Mistakes:**
- Passing raw user input to SQL — SQL injection via MCP
**Testing:** Pass "DROP TABLE orders" as orderId — verify sanitized
**Expected:** Returns "Invalid order ID format" not DB error

---

### Step 6: Monitor MCP tool calls

**Why:** MCP calls from Claude/Cursor are invisible without logging.

**What:** Add request logging for all MCP tool invocations.
**Files:** McpAuditLogger.java
```
audit/McpAuditLogger.java
```
```
@Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
public Object auditMcpCall(ProceedingJoinPoint pjp) throws Throwable {
  log.info("MCP CALL: tool={} args={}", pjp.getSignature().getName(), pjp.getArgs());
  Object result = pjp.proceed();
  log.info("MCP RESULT: {}", result);
  return result;
}
```
**Common Mistakes:**
- No audit trail — compliance requirement for enterprise
**Testing:** Call tool from Claude Desktop — verify audit log entry created
**Expected:** Every Claude Desktop tool call logged with timestamp and parameters

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/ai-dev-academy/ai-dev-academy-projects.git
cd ai-dev-academy-projects/15-model-context-protocol

# Build
mvn clean install

# Run
mvn spring-boot:run
```

> **Note:** Set your API key in `src/main/resources/application.properties` or as an environment variable before running.

---

*Part of the [AI Dev Academy](https://github.com/ai-dev-academy) curriculum.*
