
package com.tcs.AIPoweredRetailInventoryOptimization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.tcs.AIPoweredRetailInventoryOptimization.Service.AlertService;
import com.tcs.AIPoweredRetailInventoryOptimization.Service.ReorderService;
import com.tcs.AIPoweredRetailInventoryOptimization.Service.ScenarioAnalysisService;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private ReorderService reorderService;

    @Autowired
    private ScenarioAnalysisService scenarioService;

    // Endpoint to set default model choice for forecasting (ARIMA or LSTM)
    @PostMapping("/model")
    public Map<String, String> setModel(@RequestParam String model) {
        String chosen = scenarioService.setDefaultModel(model);
        Map<String, String> resp = new HashMap<>();
        resp.put("model", chosen);
        resp.put("status", "saved");
        return resp;
    }

    @GetMapping("/model")
    public Map<String, String> getModel() {
        Map<String, String> resp = new HashMap<>();
        resp.put("model", scenarioService.getDefaultModel());
        return resp;
    }

    // Trigger an inventory health check and return alerts
    @GetMapping("/alerts/run")
    public Object runAlerts() {
        return alertService.runInventoryChecks();
    }

    // Manually create a reorder for a product
    @PostMapping("/reorder")
    public Object createReorder(@RequestParam String productId, @RequestParam String storeId, @RequestParam int qty) {
        return reorderService.createReorder(productId, storeId, qty);
    }

    // Scenario analysis endpoint - simulate promotion/seasonality/events
    @PostMapping("/scenario/simulate")
    public Object simulateScenario(@RequestBody Map<String, Object> payload) {
        return scenarioService.simulate(payload);
    }
}
