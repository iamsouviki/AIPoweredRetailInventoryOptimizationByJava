
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.SaleHistory;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.SaleHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class SalesApiController {

    private final SaleHistoryRepository saleHistoryRepository;

    @GetMapping("/api/sales")
    public Map<String,Object> getDailySales(@RequestParam String productId,
                                            @RequestParam(required=false) String storeId,
                                            @RequestParam(defaultValue = "365") int daysBack) {

        List<SaleHistory> sales = saleHistoryRepository.findByProductId(productId);
        if (storeId != null && !storeId.isEmpty()) {
            sales = sales.stream().filter(s -> storeId.equals(s.getStoreId())).toList();
        }
        Map<Long, Integer> byDay = new TreeMap<>();
        long today = java.time.Instant.now().atZone(ZoneOffset.UTC).toLocalDate().toEpochDay();
        long start = today - daysBack;
        for (SaleHistory s: sales) {
            long day = s.getSaleDate().atZone(ZoneOffset.UTC).toLocalDate().toEpochDay();
            if (day >= start) {
                byDay.put(day, byDay.getOrDefault(day, 0) + s.getQuantity());
            }
        }
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (Map.Entry<Long,Integer> e : byDay.entrySet()) {
            dates.add(java.time.LocalDate.ofEpochDay(e.getKey()).toString());
            values.add(e.getValue());
        }
        Map<String,Object> out = new HashMap<>();
        out.put("dates", dates);
        out.put("values", values);
        return out;
    }
}
