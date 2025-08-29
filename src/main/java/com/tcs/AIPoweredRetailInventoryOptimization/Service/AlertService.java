
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Service
public class AlertService {

    @Autowired(required=false)
    private InventoryService inventoryService; // optional - if present in project

    @Autowired
    private ReorderService reorderService;

    // Thresholds could be moved to config; hard-coded here for simplicity
    private static final double STOCKOUT_THRESHOLD = 2.0;
    private static final double LOW_STOCK_THRESHOLD = 5.0;

    // Run every 5 minutes (cron can be adjusted). This requires @EnableScheduling in application class.
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void scheduledInventoryCheck() {
        try {
            List<Map<String, Object>> alerts = runInventoryChecks();
            if (!alerts.isEmpty()) {
                // In production, send emails/SLACK/WEBHOOKS. For now we log and auto-reorder.
                for (Map<String,Object> a : alerts) {
                    System.out.println("ALERT: " + a);
                    try {
                        String productId = (String)a.get("productId");
                        String storeId = (String)a.get("storeId");
                        int qty = ((Number)a.get("suggestedReorder")).intValue();
                        reorderService.createReorder(productId, storeId, qty);
                    } catch (Exception ex) {
                        System.err.println("Failed to auto-reorder: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Public method to trigger checks synchronously
    public List<Map<String, Object>> runInventoryChecks() {
        List<Map<String,Object>> alerts = new ArrayList<>();
        if (inventoryService == null) {
            // If no inventory service is available, produce a sample alert for demonstration
            Map<String,Object> demo = new HashMap<>();
            demo.put("productId", "DEMO-P-1");
            demo.put("storeId", "STORE-1");
            demo.put("currentStock", 1);
            demo.put("threshold", STOCKOUT_THRESHOLD);
            demo.put("severity", "CRITICAL");
            demo.put("suggestedReorder", 50);
            alerts.add(demo);
            return alerts;
        }

        // Query inventory service for stock levels (assumes such methods exist)
        List<String> productIds = inventoryService.listMonitoredProductIds();
        for (String pid : productIds) {
            Map<String,Object> stock = inventoryService.getStockForProduct(pid);
            double qty = ((Number)stock.getOrDefault("quantity", 0)).doubleValue();
            String storeId = (String)stock.getOrDefault("storeId", "ALL");
            if (qty <= STOCKOUT_THRESHOLD) {
                Map<String,Object> a = new HashMap<>();
                a.put("productId", pid);
                a.put("storeId", storeId);
                a.put("currentStock", qty);
                a.put("threshold", STOCKOUT_THRESHOLD);
                a.put("severity", "CRITICAL");
                a.put("suggestedReorder", 100);
                alerts.add(a);
            } else if (qty <= LOW_STOCK_THRESHOLD) {
                Map<String,Object> a = new HashMap<>();
                a.put("productId", pid);
                a.put("storeId", storeId);
                a.put("currentStock", qty);
                a.put("threshold", LOW_STOCK_THRESHOLD);
                a.put("severity", "WARNING");
                a.put("suggestedReorder", 50);
                alerts.add(a);
            }
        }
        return alerts;
    }
}
