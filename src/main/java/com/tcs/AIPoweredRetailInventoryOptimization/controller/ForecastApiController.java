package com.tcs.AIPoweredRetailInventoryOptimization.controller;

import com.tcs.AIPoweredRetailInventoryOptimization.Service.ArModelService;
import com.tcs.AIPoweredRetailInventoryOptimization.util.LstmForecastAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastApiController {

    private final ArModelService arModelService;
    private final LstmForecastAdapter lstmForecastAdapter;

    @GetMapping
    public double[] forecast(
            @RequestParam String productId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "lstm") String model
    ) throws Exception {
        if ("arima".equalsIgnoreCase(model) || "ar".equalsIgnoreCase(model)) {
            return arModelService.forecast(productId, null, days);
        } else {
            return lstmForecastAdapter.forecast(productId, null, days);
        }
    }
}
