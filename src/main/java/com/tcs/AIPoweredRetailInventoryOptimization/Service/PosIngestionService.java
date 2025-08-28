package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.Inventory;
import com.tcs.AIPoweredRetailInventoryOptimization.Model.SaleHistory;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.InventoryRepository;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.SaleHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosIngestionService {

    private final SaleHistoryRepository saleHistoryRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Fetch all sales (or filter by store) and apply updates to inventory.
     * This assumes sale_history already contains the POS data (Case 1).
     */
    public void ingestAllSalesAndUpdateInventory() {
        List<SaleHistory> sales = saleHistoryRepository.findAll();

        log.info("POS Ingestion: processing {} sale records", sales.size());

        for (SaleHistory s : sales) {
            try {
                String storeId = s.getStoreId();
                String productId = s.getProductId();
                int qty = s.getQuantity();

                inventoryRepository.findByStoreIdAndProductId(storeId, productId)
                    .ifPresentOrElse(inv -> {
                        int newStock = inv.getCurrentStock() - qty;
                        if (newStock < 0) newStock = 0; // avoid negative
                        inv.setCurrentStock(newStock);
                        inv.setLastUpdated(java.time.Instant.now());
                        inventoryRepository.save(inv);
                        log.debug("Updated inventory for store={} product={} newStock={}", storeId, productId, newStock);
                    }, () -> {
                        // If no inventory doc existed, we can create one or log
                        log.warn("No inventory document for store {} and product {} — sale {} not applied", storeId, productId, s.getSaleId());
                    });
            } catch (Exception ex) {
                log.error("Error processing sale {}: {}", s.getSaleId(), ex.getMessage(), ex);
            }
        }
    }

    /**
     * Convenience method: ingest sales for a single store.
     */
    public void ingestSalesForStore(String storeId) {
        List<SaleHistory> sales = saleHistoryRepository.findByStoreId(storeId);
        for (SaleHistory s : sales) {
            inventoryRepository.findByStoreIdAndProductId(storeId, s.getProductId())
                    .ifPresent(inv -> {
                        int newStock = inv.getCurrentStock() - s.getQuantity();
                        if (newStock < 0) newStock = 0;
                        inv.setCurrentStock(newStock);
                        inv.setLastUpdated(java.time.Instant.now());
                        inventoryRepository.save(inv);
                    });
        }
    }
}
