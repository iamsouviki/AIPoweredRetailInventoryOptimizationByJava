package com.ai.retail.controller;

import com.ai.retail.dto.ForecastRequest;
import com.ai.retail.service.ArimaForecastService;
import com.ai.retail.service.LstmForecastService;
import com.ai.retail.service.ReorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private ArimaForecastService arimaService;
    @Autowired
    private LstmForecastService lstmService;
    @Autowired
    private ReorderService reorderService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/forecast")
    public String forecast(@ModelAttribute ForecastRequest req, Model model) {
        double[] history = reorderService.getSalesHistory(req.getStoreId());
        double[] yhat = null;
        String modelUsed = req.getModel();

        if ("LSTM".equalsIgnoreCase(modelUsed)) {
            yhat = lstmService.forecast(history, req.getDays());
        } else {
            //yhat = arimaService.forecast(history, req.getDays());
            //modelUsed = "ARIMA";
        }

        Map<String, Object> recs = reorderService.recommendBySupplier(req.getStoreId(), yhat);

        model.addAttribute("forecast", yhat);
        model.addAttribute("modelUsed", modelUsed);
        model.addAttribute("storeId", req.getStoreId());
        model.addAttribute("days", req.getDays());
        model.addAttribute("recs", recs);

        return "result";
    }

    @ResponseBody
    @PostMapping("/api/forecast")
    public double[] apiForecast(@RequestBody ForecastRequest req) {
        double[] history = reorderService.getSalesHistory(req.getStoreId());
        if ("LSTM".equalsIgnoreCase(req.getModel())) {
            return lstmService.forecast(history, req.getDays());
        }
        //return arimaService.forecast(history, req.getDays());
        return lstmService.forecast(history, req.getDays());
    }
}