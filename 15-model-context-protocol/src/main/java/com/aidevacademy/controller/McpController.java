package com.aidevacademy.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Topic 15 - Model Context Protocol (MCP)
 * This simulates an MCP server exposing domain tools.
 * Spring AI 1.0 has native MCP server support - see README for full setup.
 */
@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class McpController {

    private final ChatClient chatClient;

    // Simulated inventory
    private static final Map<String, Integer> INVENTORY = Map.of(
        "SKU-001", 45, "SKU-002", 0, "SKU-003", 12, "SKU-004", 200
    );

    public McpController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** Tool: list all available MCP tools */
    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, String>>> listTools() {
        return ResponseEntity.ok(List.of(
            Map.of("name", "getInventory",   "description", "Get inventory level for a SKU"),
            Map.of("name", "getLowStock",    "description", "Get all SKUs below reorder threshold"),
            Map.of("name", "chat",           "description", "Ask the AI agent about inventory")
        ));
    }

    /** Tool: get inventory for a SKU */
    @GetMapping("/inventory/{sku}")
    public ResponseEntity<Map<String, Object>> getInventory(@PathVariable String sku) {
        Integer qty = INVENTORY.get(sku.toUpperCase());
        if (qty == null) return ResponseEntity.ok(Map.of("sku", sku, "error", "SKU not found"));
        return ResponseEntity.ok(Map.of("sku", sku, "quantity", qty, "status", qty > 0 ? "in_stock" : "out_of_stock"));
    }

    /** Tool: get low stock items */
    @GetMapping("/low-stock")
    public ResponseEntity<List<String>> getLowStock(@RequestParam(defaultValue = "20") int threshold) {
        List<String> lowStock = INVENTORY.entrySet().stream()
                .filter(e -> e.getValue() < threshold)
                .map(e -> e.getKey() + " (qty: " + e.getValue() + ")")
                .toList();
        return ResponseEntity.ok(lowStock);
    }

    /** AI agent that can use the tools above */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody String question) {
        String context = "Inventory data: " + INVENTORY;
        String response = chatClient.prompt()
                .system("You are an inventory management assistant. Available inventory: " + context)
                .user(question)
                .call()
                .content();
        return ResponseEntity.ok(Map.of("response", response));
    }

    @GetMapping("/health")
    public String health() { return "Topic 15 - MCP Server running! GET /mcp/tools to see available tools."; }
}
