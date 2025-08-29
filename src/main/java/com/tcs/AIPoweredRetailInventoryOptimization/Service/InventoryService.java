
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    // List product ids to check
    List<String> listMonitoredProductIds();

    // Returns a map with keys like quantity, storeId
    Map<String, Object> getStockForProduct(String productId);
}
