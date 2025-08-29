
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReorderService {

    // In production this would write to a purchase-order DB table or call supplier APIs
    public Map<String,Object> createReorder(String productId, String storeId, int qty) {
        Map<String,Object> order = new HashMap<>();
        order.put("orderId", UUID.randomUUID().toString());
        order.put("productId", productId);
        order.put("storeId", storeId);
        order.put("qty", qty);
        order.put("status", "CREATED");
        order.put("createdAt", new Date().toString());
        System.out.println("Created reorder: " + order);
        return order;
    }
}
