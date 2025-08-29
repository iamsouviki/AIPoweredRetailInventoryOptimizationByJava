package com.ai.retail.service;

import org.springframework.stereotype.Service;



@Service
public class ArimaForecastService {
    public double[] forecast(double[] sales, int horizon) {
        // Basic ARIMA(1,1,1). In production, use AIC/BIC search for p,d,q.
        //SARIMA.Model model = SARIMA.fit(sales, 1, 1, 1);
        //return model.forecast(horizon);
        return null;
    }
}