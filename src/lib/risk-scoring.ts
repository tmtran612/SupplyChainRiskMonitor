// This file is a placeholder for frontend-specific risk scoring utilities.
// The core scoring logic has been moved to the Spring Boot backend.

export const RISK_THRESHOLDS = {
  low: { min: 0, max: 4, color: "green" },
  medium: { min: 4, max: 7, color: "yellow" },
  high: { min: 7, max: 9, color: "orange" },
  critical: { min: 9, max: 10, color: "red" },
} as const;

export function getRiskLevel(score: number): keyof typeof RISK_THRESHOLDS {
  if (score >= RISK_THRESHOLDS.critical.min) return "critical";
  if (score >= RISK_THRESHOLDS.high.min) return "high";
  if (score >= RISK_THRESHOLDS.medium.min) return "medium";
  return "low";
}

export interface DashboardMetrics {
  totalSuppliers: number;
  activeRisks: number;
  criticalAlerts: number;
  averageRiskScore: number;
  riskDistribution: {
    low: number;
    medium: number;
    high: number;
    critical: number;
  };
}

// All mock metric logic removed. Only backend fetches should be used.