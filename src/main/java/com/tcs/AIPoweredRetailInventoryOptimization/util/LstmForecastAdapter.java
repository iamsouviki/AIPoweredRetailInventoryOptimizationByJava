package com.tcs.AIPoweredRetailInventoryOptimization.util;

import com.tcs.AIPoweredRetailInventoryOptimization.Service.ForecastService;
import com.tcs.AIPoweredRetailInventoryOptimization.Service.LstmModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("lstmForecastAdapter")
@RequiredArgsConstructor
@Slf4j
public class LstmForecastAdapter implements ForecastService {

    private final LstmModelService lstmModelService;

    @Override
    public double[] forecast(String productId, String storeId, int daysAhead) {
        try {
            // default window size = 10, you can adjust based on training
            return lstmModelService.predictNextNDays(productId, 10, daysAhead);
        } catch (Exception ex) {
            log.warn("LSTM forecast failed for {}: {}", productId, ex.getMessage());
            return new double[daysAhead];
        }
    }
}
