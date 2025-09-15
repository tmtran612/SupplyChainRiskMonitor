package com.riskmonitor.dto;

import java.util.Map;

public class DashboardMetricsDTO {
    private int totalSuppliers;
    private int activeRisks;
    private int criticalAlerts;
    private double averageRiskScore;
    private Map<String, Integer> riskDistribution;
    private Map<String, Integer> weatherFactors; // counts of suppliers impacted by factor
    private String weatherNarrative;

    public DashboardMetricsDTO(int totalSuppliers, int activeRisks, int criticalAlerts, double averageRiskScore, Map<String, Integer> riskDistribution, Map<String, Integer> weatherFactors, String weatherNarrative) {
        this.totalSuppliers = totalSuppliers;
        this.activeRisks = activeRisks;
        this.criticalAlerts = criticalAlerts;
        this.averageRiskScore = averageRiskScore;
        this.riskDistribution = riskDistribution;
        this.weatherFactors = weatherFactors;
        this.weatherNarrative = weatherNarrative;
    }

    // Getters
    public int getTotalSuppliers() { return totalSuppliers; }
    public int getActiveRisks() { return activeRisks; }
    public int getCriticalAlerts() { return criticalAlerts; }
    public double getAverageRiskScore() { return averageRiskScore; }
    public Map<String, Integer> getRiskDistribution() { return riskDistribution; }
    public Map<String, Integer> getWeatherFactors() { return weatherFactors; }
    public String getWeatherNarrative() { return weatherNarrative; }
}
