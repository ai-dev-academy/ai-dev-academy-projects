package com.aidevacademy.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class OrderTools {

    // Simulated order database
    private static final Map<String, String> ORDERS = Map.of(
        "ORD-001", "SHIPPED",
        "ORD-002", "PENDING",
        "ORD-003", "DELIVERED",
        "ORD-004", "CANCELLED"
    );

    @Tool("Get the current status of an order by its order ID. Returns: PENDING, SHIPPED, DELIVERED, or CANCELLED.")
    public String getOrderStatus(String orderId) {
        String status = ORDERS.get(orderId.toUpperCase());
        if (status == null) return "Order " + orderId + " not found in the system.";
        return "Order " + orderId + " status: " + status;
    }

    @Tool("Cancel an order by order ID. Requires a reason. Only works if order is PENDING.")
    public String cancelOrder(String orderId, String reason) {
        String status = ORDERS.get(orderId.toUpperCase());
        if (status == null)          return "Order " + orderId + " not found.";
        if (!"PENDING".equals(status)) return "Cannot cancel order " + orderId + " - it is already " + status;
        return "Order " + orderId + " has been cancelled. Reason: " + reason;
    }

    @Tool("Get estimated delivery date for a shipped order by order ID.")
    public String getDeliveryEstimate(String orderId) {
        String status = ORDERS.get(orderId.toUpperCase());
        if (!"SHIPPED".equals(status)) return "Order " + orderId + " is not shipped yet (status: " + status + ")";
        return "Order " + orderId + " estimated delivery: 2-3 business days.";
    }
}
