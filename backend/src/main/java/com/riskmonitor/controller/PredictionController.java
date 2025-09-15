package com.riskmonitor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.riskmonitor.model.RiskScore;
import com.riskmonitor.repository.RiskScoreRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PredictionController {
    @Autowired
    private RiskScoreRepository riskScoreRepository;

    @GetMapping("/risk-forecast")
    public Map<String, Object> getRiskForecast() {
        Map<String, Object> forecast = new HashMap<>();
        List<RiskScore> scores = riskScoreRepository.findAll();
        List<Map<String, Object>> dailyPredictions = scores.stream()
            .map(score -> {
                Map<String, Object> prediction = new HashMap<>();
                prediction.put("date", score.getTimestamp());
                prediction.put("predicted_risk_score", score.getRiskScore());
                prediction.put("supplier_id", score.getSupplier().getId());
                prediction.put("reason", score.getReason());
                return prediction;
            })
            .collect(Collectors.toList());
        forecast.put("daily_predictions", dailyPredictions);
        forecast.put("generated_at", java.time.LocalDateTime.now().toString());
        return forecast;
    }
    
    @GetMapping("/supplier/{supplierId}/forecast")
    public Map<String, Object> getSupplierForecast(@PathVariable UUID supplierId) {
        Map<String, Object> forecast = new HashMap<>();
        List<RiskScore> scores = riskScoreRepository.findBySupplierIdOrderByTimestampDesc(supplierId);
        if (!scores.isEmpty()) {
            RiskScore latest = scores.get(0);
            forecast.put("supplier_id", supplierId);
            forecast.put("predicted_risk_score", latest.getRiskScore());
            forecast.put("timestamp", latest.getTimestamp());
            forecast.put("reason", latest.getReason());
        } else {
            forecast.put("error", "No risk scores found for supplier");
        }
        return forecast;
    }
    
    // Removed mock prediction helper
}