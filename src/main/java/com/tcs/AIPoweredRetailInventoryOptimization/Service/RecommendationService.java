
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.Inventory;
import com.tcs.AIPoweredRetailInventoryOptimization.Model.SaleHistory;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.InventoryRepository;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.SaleHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple recommendation engine:
 * - average daily demand, stddev
 * - safety stock = z * stddev * sqrt(leadTime)
 * - reorder point = avgDaily*leadTime + safetyStock
 * - suggested order qty = max(0, (targetStock - currentInventory))
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final InventoryRepository inventoryRepository;
    private final SaleHistoryRepository saleHistoryRepository;
    private ForecastService forecastService; // chosen at runtime via controller

    public void setForecastService(ForecastService fs) { this.forecastService = fs; }


    @Value("${recommendation.leadTimeDays:7}")
    private int leadTimeDays;

    @Value("${recommendation.serviceLevelZ:1.65}")
    private double serviceLevelZ; // default ~95%

    public static class Recommendation {
        public String productId;
        public String storeId;
        public int currentInventory;
        public double avgDaily;
        public double stdDaily;
        public double safetyStock;
        public double reorderPoint;
        public double suggestedOrderQty;
        public double[] forecastNextDays;
    }

    public Recommendation recommendFor(String productId, String storeId, int forecastDays) {
        // get history
        List<SaleHistory> sales = saleHistoryRepository.findByProductId(productId);
        if (storeId != null && !storeId.isEmpty()) {
            sales = sales.stream().filter(s -> storeId.equals(s.getStoreId())).collect(Collectors.toList());
        }
        // aggregate daily
        Map<Long, Integer> byDay = new TreeMap<>();
        for (SaleHistory s : sales) {
            long day = s.getSaleDate().atZone(ZoneOffset.UTC).toLocalDate().toEpochDay();
            byDay.put(day, byDay.getOrDefault(day, 0) + s.getQuantity());
        }
        List<Double> daily = new ArrayList<>();
        for (Long k : byDay.keySet()) daily.add(byDay.get(k).doubleValue());
        double avg = 0.0;
        double std = 0.0;
        if (!daily.isEmpty()) {
            double sum = 0.0;
            for (double v : daily) sum += v;
            avg = sum / daily.size();
            double var = 0.0;
            for (double v : daily) var += (v - avg)*(v - avg);
            std = Math.sqrt(var / daily.size());
        }
        double safety = serviceLevelZ * std * Math.sqrt(Math.max(1, leadTimeDays));
        double reorder = avg * leadTimeDays + safety;

        // current inventory
        Inventory inv = inventoryRepository.findByProductIdAndStoreId(productId, storeId);
        int current = inv != null ? inv.getQuantityOnHand() : 0;

        // forecast using AR model (or switch to LSTM externally)
        double[] f = (forecastService != null ? forecastService.forecast(productId, storeId, forecastDays) : new double[forecastDays]);

        double targetStock = avg * leadTimeDays + safety;
        double suggestedOrder = Math.max(0.0, targetStock - current);

        Recommendation r = new Recommendation();
        r.productId = productId;
        r.storeId = storeId;
        r.currentInventory = current;
        r.avgDaily = avg;
        r.stdDaily = std;
        r.safetyStock = safety;
        r.reorderPoint = reorder;
        r.suggestedOrderQty = suggestedOrder;
        r.forecastNextDays = f;
        return r;
    }
}
