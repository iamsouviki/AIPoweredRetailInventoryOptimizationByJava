
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.EventData;
import com.tcs.AIPoweredRetailInventoryOptimization.Model.SaleHistory;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.EventDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventImpactService {

    private final EventDataRepository eventDataRepository;

    private double weightForType(String type) {
        if (type == null) return 0.05;
        switch (type.toLowerCase()) {
            case "festival": return 0.20;
            case "national": return 0.10;
            case "sports": return 0.12;
            case "exhibition": return 0.08;
            case "cultural": return 0.07;
            default: return 0.05;
        }
    }

    /**
     * Returns a new list with quantities adjusted upward on event days for matching storeId.
     */
    public List<SaleHistory> applyEventBoost(List<SaleHistory> sales) {
        if (sales == null || sales.isEmpty()) return sales;
        Map<String, List<EventData>> byStore = eventDataRepository.findAll().stream()
                .filter(e -> e.getStoreId() != null && e.getStartTime() != null)
                .collect(Collectors.groupingBy(EventData::getStoreId));

        List<SaleHistory> out = new ArrayList<>(sales.size());
        for (SaleHistory s : sales) {
            double qty = s.getQuantity();
            String store = s.getStoreId();
            if (store != null && byStore.containsKey(store)) {
                int y = s.getSaleDate().atZone(ZoneOffset.UTC).getYear();
                int m = s.getSaleDate().atZone(ZoneOffset.UTC).getMonthValue();
                int d = s.getSaleDate().atZone(ZoneOffset.UTC).getDayOfMonth();
                double boost = 0.0;
                for (EventData e : byStore.get(store)) {
                    int ey = e.getStartTime().atZone(ZoneOffset.UTC).getYear();
                    int em = e.getStartTime().atZone(ZoneOffset.UTC).getMonthValue();
                    int ed = e.getStartTime().atZone(ZoneOffset.UTC).getDayOfMonth();
                    if (ey == y && em == m && ed == d) {
                        boost = Math.max(boost, weightForType(e.getEventType()));
                    }
                }
                qty = qty * (1.0 + boost);
            }
            SaleHistory copy = SaleHistory.builder()
                    .saleId(s.getSaleId())
                    .storeId(s.getStoreId())
                    .productId(s.getProductId())
                    .quantity((int)Math.round(qty))
                    .price(s.getPrice())
                    .saleDate(s.getSaleDate())
                    .build();
            out.add(copy);
        }
        return out;
    }
}
