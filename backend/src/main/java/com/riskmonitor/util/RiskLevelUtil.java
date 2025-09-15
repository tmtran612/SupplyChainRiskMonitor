package com.riskmonitor.util;

public class RiskLevelUtil {

    public enum RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

    public static RiskLevel classify(double score) {
        if (score >= 75) return RiskLevel.CRITICAL;
        if (score >= 50) return RiskLevel.HIGH;
        if (score >= 25) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    public static String classifyLabel(double score) {
        return classify(score).name().toLowerCase();
    }
}
