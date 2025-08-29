
package com.tcs.AIPoweredRetailInventoryOptimization.Service;

import com.tcs.AIPoweredRetailInventoryOptimization.Model.SaleHistory;
import com.tcs.AIPoweredRetailInventoryOptimization.Repository.SaleHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Primary;

/**
 * Simple autoregressive forecaster using OLS on lagged daily sums.
 * Not a full ARIMA implementation, but useful as a lightweight baseline.
 */

@RequiredArgsConstructor
@Slf4j
@Primary
@Service
public class ArModelService implements ForecastService {

    private final SaleHistoryRepository saleHistoryRepository;

    private List<Double> aggregateDaily(String productId, String storeId) {
        List<SaleHistory> sales = saleHistoryRepository.findByProductId(productId);
        if (storeId != null && !storeId.isEmpty()) {
            sales = sales.stream().filter(s -> storeId.equals(s.getStoreId())).collect(Collectors.toList());
        }
        // aggregate by yyyy-MM-dd epoch day
        java.util.Map<Long, Integer> byDay = new java.util.TreeMap<>();
        for (SaleHistory s : sales) {
            long day = s.getSaleDate().atZone(ZoneOffset.UTC).toLocalDate().toEpochDay();
            byDay.put(day, byDay.getOrDefault(day, 0) + s.getQuantity());
        }
        List<Double> res = new ArrayList<>();
        for (Long k : byDay.keySet()) res.add(byDay.get(k).doubleValue());
        return res;
    }

    @Override
    public double[] forecast(String productId, String storeId, int daysAhead) {
        List<Double> daily = aggregateDaily(productId, storeId);
        int n = daily.size();
        if (n == 0) return new double[daysAhead];
        int p = Math.min(7, n/2); // use up to 7 lags
        if (p < 1) p = 1;
        int rows = n - p;
        if (rows <= 0) {
            // fallback: repeat last value
            double last = daily.get(n-1);
            double[] out = new double[daysAhead];
            for (int i=0;i<daysAhead;i++) out[i] = last;
            return out;
        }
        double[] y = new double[rows];
        double[][] x = new double[rows][p];
        for (int i=0;i<rows;i++) {
            y[i] = daily.get(p + i);
            for (int j=0;j<p;j++) x[i][j] = daily.get(i + (p - j -1));
        }
        try {
            OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
            ols.newSampleData(y, x);
            double[] beta = ols.estimateRegressionParameters(); // beta[0] intercept
            // generate forecasts iteratively
            List<Double> window = new ArrayList<>(daily.subList(n-p, n));
            double[] preds = new double[daysAhead];
            for (int h=0; h<daysAhead; h++) {
                double pred = beta[0];
                for (int j=0;j<p;j++) {
                    double lag = window.get(window.size() - 1 - j);
                    pred += beta[j+1] * lag;
                }
                preds[h] = Math.max(0.0, pred);
                window.add(pred);
                if (window.size() > p) window.remove(0);
            }
            return preds;
        } catch (Exception ex) {
            log.warn("AR forecasting failed, falling back to last-value: {}", ex.getMessage());
            double last = daily.get(n-1);
            double[] out = new double[daysAhead];
            for (int i=0;i<daysAhead;i++) out[i] = last;
            return out;
        }
    }
}