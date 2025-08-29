
package com.tcs.AIPoweredRetailInventoryOptimization.controller;

import com.tcs.AIPoweredRetailInventoryOptimization.Service.LstmModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TrainingController {

    private final LstmModelService lstmModelService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Manual trigger
    @PostMapping("/admin/train/{productId}")
    public String trainNow(@PathVariable String productId) {
        try {
            Future<?> f = executor.submit(() -> {
                try {
                    lstmModelService.trainModelForProduct(productId, 14, 20);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            // wait up to 10 minutes
            f.get(10, TimeUnit.MINUTES);
            return "Training completed for " + productId;
        } catch (TimeoutException te) {
            return "Training timed out for " + productId;
        } catch (Exception ex) {
            return "Training failed: " + ex.getMessage();
        }
    }

    // Scheduled nightly retrain (at 2:30 AM)
    @Scheduled(cron = "0 30 2 * * ?")
    public void nightlyRetrainAll() {
        log.info("Starting nightly retrain for top products");
        // For safety, run single-threaded sequential training of a few top products
        // You can customize which products to train (e.g., top sellers)
        String[] productsToTrain = {"prod001","prod002","prod003"};
        for (String pid : productsToTrain) {
            try {
                Future<?> f = executor.submit(() -> {
                    try {
                        lstmModelService.trainModelForProduct(pid, 14, 20);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                f.get(15, TimeUnit.MINUTES); // per-product timeout
            } catch (Exception ex) {
                log.error("Retrain failed for {}: {}", pid, ex.getMessage(), ex);
            }
        }
        log.info("Nightly retrain finished");
    }
}
