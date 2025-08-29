
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

public interface ForecastService {
    /**
     * Forecast next `daysAhead` daily demand for given productId and storeId.
     * @param productId product id
     * @param storeId store id (nullable)
     * @param daysAhead number of days to forecast
     * @return array of length daysAhead with predicted daily demand
     */
    double[] forecast(String productId, String storeId, int daysAhead);
}
