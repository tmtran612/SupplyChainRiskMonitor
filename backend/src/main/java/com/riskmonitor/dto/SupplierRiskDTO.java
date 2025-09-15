package com.riskmonitor.dto;

import com.riskmonitor.model.Supplier;

// A DTO (Data Transfer Object) to combine Supplier and Risk Score data
public class SupplierRiskDTO {
    private Supplier supplier;
    private double overall_score;
    private double performance_score;
    private double financial_score;
    private double geopolitical_score;
    private double environmental_score;
    private String riskLevel;
    private Double previous_score; // nullable if no previous
    private Double delta; // latest - previous (nullable if no previous)
    private String reason; // latest reason
    private java.time.LocalDateTime last_updated;

    public SupplierRiskDTO(Supplier supplier, double overall_score, double performance_score, double financial_score, double geopolitical_score, double environmental_score, String riskLevel, Double previous_score, Double delta, String reason, java.time.LocalDateTime last_updated) {
        this.supplier = supplier;
        this.overall_score = overall_score;
        this.performance_score = performance_score;
        this.financial_score = financial_score;
        this.geopolitical_score = geopolitical_score;
        this.environmental_score = environmental_score;
        this.riskLevel = riskLevel;
        this.previous_score = previous_score;
        this.delta = delta;
        this.reason = reason;
        this.last_updated = last_updated;
    }

    // Getters and Setters
    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public double getOverall_score() {
        return overall_score;
    }

    public void setOverall_score(double overall_score) {
        this.overall_score = overall_score;
    }

    public double getPerformance_score() {
        return performance_score;
    }

    public void setPerformance_score(double performance_score) {
        this.performance_score = performance_score;
    }

    public double getFinancial_score() {
        return financial_score;
    }

    public void setFinancial_score(double financial_score) {
        this.financial_score = financial_score;
    }

    public double getGeopolitical_score() {
        return geopolitical_score;
    }

    public void setGeopolitical_score(double geopolitical_score) {
        this.geopolitical_score = geopolitical_score;
    }

    public double getEnvironmental_score() {
        return environmental_score;
    }

    public Double getPrevious_score() { return previous_score; }
    public Double getDelta() { return delta; }
    public String getReason() { return reason; }
    public java.time.LocalDateTime getLast_updated() { return last_updated; }

    public void setEnvironmental_score(double environmental_score) {
        this.environmental_score = environmental_score;
    }
}
