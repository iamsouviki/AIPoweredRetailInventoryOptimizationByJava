package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ForecastController {

    private final LstmModelService lstmModelService;

    @GetMapping("/api/forecast/{productId}/{days}")
    public Map<String, Object> forecast(@PathVariable String productId, @PathVariable int days) throws Exception {
        int windowSize = 14;
        double[] preds = lstmModelService.predictNextNDays(productId, windowSize, days);
        Map<String, Object> resp = new HashMap<>();
        resp.put("productId", productId);
        resp.put("predictions", preds);
        return resp;
    }
}
