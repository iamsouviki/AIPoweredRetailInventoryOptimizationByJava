
package com.tcs.AIPoweredRetailInventoryOptimization.controller;

import com.tcs.AIPoweredRetailInventoryOptimization.Service.ForecastService;
import com.tcs.AIPoweredRetailInventoryOptimization.Service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Qualifier("arModelService")
    private final ForecastService arService;

    @Qualifier("lstmForecastAdapter")
    private final ForecastService lstmService;

    @GetMapping("/recommendation")
    public RecommendationService.Recommendation getRecommendation(@RequestParam String productId,
                                                                  @RequestParam(required=false) String storeId,
                                                                  @RequestParam(defaultValue = "14") int days,
                                                                  @RequestParam(defaultValue = "AR") String modelType) {
        // choose forecast model inside RecommendationService if needed; here we pass model selection
        if ("LSTM".equalsIgnoreCase(modelType)) {
            recommendationService.setForecastService(lstmService);
        } else {
            recommendationService.setForecastService(arService);
        }
        return recommendationService.recommendFor(productId, storeId, days);
    }
}
