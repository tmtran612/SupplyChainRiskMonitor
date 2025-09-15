package com.riskmonitor.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class SupplierRiskDetailDTO {
    private UUID supplierId;
    private String supplierName;
    private double currentScore;
    private String currentLevel;
    private String reason;
    private LocalDateTime timestamp;
    private String weatherFactors;

    public SupplierRiskDetailDTO(UUID supplierId, String supplierName, double currentScore, String currentLevel, String reason, LocalDateTime timestamp, String rawReason) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.currentScore = currentScore;
        this.currentLevel = currentLevel;
        this.reason = reason;
        this.timestamp = timestamp;
            // Derive weatherFactors from rawReason tokens regardless of containing keyword 'weather'
            if (rawReason != null) {
                String lower = rawReason.toLowerCase();
                StringBuilder factors = new StringBuilder();
                if (lower.contains("severe_precipitation")) factors.append("severe_precipitation,");
                if (lower.contains("extreme_temperature")) factors.append("extreme_temperature,");
                if (lower.contains("high_wind")) factors.append("high_wind,");
                if (factors.length() > 0) {
                    factors.setLength(factors.length() - 1); // trim trailing comma
                    this.weatherFactors = factors.toString();
                }
            }
            this.weatherFactors = null; // Initialize to null if no factors found
    }

    public UUID getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public double getCurrentScore() { return currentScore; }
    public String getCurrentLevel() { return currentLevel; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
        public String getWeatherFactors() { return weatherFactors; }
}
