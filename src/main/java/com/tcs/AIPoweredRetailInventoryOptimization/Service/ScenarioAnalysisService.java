
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Service
public class ScenarioAnalysisService {

    private String defaultModel = "lstm"; // default model choice

    @Autowired(required=false)
    private ForecastService forecastService; // optional injection for simulation

    public String setDefaultModel(String model) {
        if (model == null) return defaultModel;
        if ("arima".equalsIgnoreCase(model) || "ar".equalsIgnoreCase(model)) {
            defaultModel = "arima";
        } else {
            defaultModel = "lstm";
        }
        return defaultModel;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    // payload may include productId, storeId, promotion % impact, seasonality factor, externalEventImpact
    public Map<String,Object> simulate(Map<String,Object> payload) {
        Map<String,Object> result = new HashMap<>();
        String productId = (String)payload.getOrDefault("productId", "DEMO-P-1");
        String storeId = (String)payload.getOrDefault("storeId", "STORE-1");
        int days = ((Number)payload.getOrDefault("days", 14)).intValue();
        double promoImpact = ((Number)payload.getOrDefault("promotionImpactPercent", 0)).doubleValue();
        double seasonalityFactor = ((Number)payload.getOrDefault("seasonalityFactor", 1.0)).doubleValue();
        double eventImpact = ((Number)payload.getOrDefault("externalEventImpactPercent", 0)).doubleValue();

        // Basic simulation: call forecastService if available, otherwise return deterministic synthetic forecast
        double[] baseForecast;
        if (forecastService != null) {
            baseForecast = forecastService.forecast(productId, storeId, days);
        } else {
            baseForecast = new double[days];
            for (int i=0;i<days;i++) baseForecast[i] = 10 + (i%7); // simple pattern
        }

        double[] adjusted = new double[days];
        for (int i=0;i<days;i++) {
            double v = baseForecast[i];
            v = v * seasonalityFactor;
            v = v * (1.0 + promoImpact/100.0);
            v = v * (1.0 + eventImpact/100.0);
            adjusted[i] = v;
        }

        result.put("productId", productId);
        result.put("storeId", storeId);
        result.put("baseForecast", baseForecast);
        result.put("adjustedForecast", adjusted);
        result.put("modelUsed", defaultModel);
        return result;
    }
}
