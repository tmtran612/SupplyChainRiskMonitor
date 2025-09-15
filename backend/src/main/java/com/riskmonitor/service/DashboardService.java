package com.riskmonitor.service;

import com.riskmonitor.dto.DashboardMetricsDTO;
import com.riskmonitor.model.RiskScore;
import com.riskmonitor.repository.SupplierRepository;
import com.riskmonitor.repository.RiskScoreRepository;
import com.riskmonitor.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private RiskScoreRepository riskScoreRepository;
    @Autowired
    private AlertRepository alertRepository;

    public DashboardMetricsDTO getDashboardMetrics() {
        int totalSuppliers = (int) supplierRepository.count();
        
        // Fetch only the latest risk score for each supplier
        List<RiskScore> latestRiskScores = riskScoreRepository.findLatestRiskScores();

        int activeRisks = latestRiskScores.size();

        int criticalAlerts = (int) alertRepository.findActiveUnacknowledged().stream()
            .filter(a -> "critical".equalsIgnoreCase(a.getSeverity()))
            .count();

        double averageRiskScore = latestRiskScores.stream()
            .mapToDouble(RiskScore::getRiskScore)
            .average().orElse(0.0);

        Map<String, Integer> riskDistribution = new HashMap<>();
        riskDistribution.put("low", (int) latestRiskScores.stream().filter(r -> r.getRiskScore() < 25).count());
        riskDistribution.put("medium", (int) latestRiskScores.stream().filter(r -> r.getRiskScore() >= 25 && r.getRiskScore() < 50).count());
        riskDistribution.put("high", (int) latestRiskScores.stream().filter(r -> r.getRiskScore() >= 50 && r.getRiskScore() < 75).count());
        riskDistribution.put("critical", (int) latestRiskScores.stream().filter(r -> r.getRiskScore() >= 75).count());
        // Weather factor aggregation (parse reasons)
        Map<String, Integer> weatherFactors = new HashMap<>();
        latestRiskScores.stream()
            .map(RiskScore::getReason)
            .filter(r -> r != null)
            .forEach(reason -> {
                // Normalize separators and look for tokens regardless of leading 'Weather' prefix
                String lower = reason.toLowerCase();
                if (lower.contains("severe_precipitation")) weatherFactors.merge("severe_precipitation", 1, Integer::sum);
                if (lower.contains("extreme_temperature")) weatherFactors.merge("extreme_temperature", 1, Integer::sum);
                if (lower.contains("high_wind")) weatherFactors.merge("high_wind", 1, Integer::sum);
            });

        String weatherNarrative;
        if (weatherFactors.isEmpty()) {
            weatherNarrative = "No significant weather drivers impacting supplier risk currently.";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Weather drivers: ");
            weatherFactors.forEach((k,v) -> sb.append(k.replace('_',' ')).append(" (").append(v).append(") "));
            weatherNarrative = sb.toString().trim();
        }
        return new DashboardMetricsDTO(totalSuppliers, activeRisks, criticalAlerts, averageRiskScore, riskDistribution, weatherFactors, weatherNarrative);
    }
}
