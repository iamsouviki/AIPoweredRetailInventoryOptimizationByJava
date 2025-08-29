package com.ai.retail.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReorderService {

    // Dummy demo sales history. Replace with DB access.
    public double[] getSalesHistory(String storeId) {
        return new double[]{120, 130, 125, 150, 160, 155, 170, 180, 175, 190, 210};
    }

    // Simple supplier-wise recommendation stub
    public Map<String, Object> recommendBySupplier(String storeId, double[] forecast) {
        // Imagine 3 suppliers; split SKUs and quantities in a simple way
        Map<String, Object> recs = new HashMap<>();
        double needTotal = Arrays.stream(forecast).sum() * 0.6; // assume 60% needs reorder
        recs.put("Supplier-A", (int)Math.ceil(needTotal * 0.4));
        recs.put("Supplier-B", (int)Math.ceil(needTotal * 0.35));
        recs.put("Supplier-C", (int)Math.ceil(needTotal * 0.25));
        return recs;
    }
}