// Type definitions for the Supply Chain Risk Monitor

export interface Supplier {
  id: string
  name: string
  location: string
  tier: 1 | 2 | 3
  industry: string
  contact_email?: string
  contact_phone?: string
  created_at: string
  updated_at: string
}

export interface RiskEvent {
  id: string
  supplier_id: string
  event_type: "weather" | "geopolitical" | "financial" | "operational" | "regulatory"
  severity: "low" | "medium" | "high" | "critical"
  description: string
  location: string
  impact_score: number
  probability: number
  detected_at: string
  resolved_at?: string
  created_at: string
  supplier?: Supplier
}

export interface RiskScore {
  id: string
  supplier_id: string
  overall_score: number
  weather_score: number
  geopolitical_score: number
  financial_score: number
  operational_score: number
  regulatory_score: number
  calculated_at: string
  supplier?: Supplier
}

export interface Alert {
  id: string
  supplier_id: string
  risk_event_id: string
  alert_type: "threshold_breach" | "new_risk" | "escalation" | "resolution"
  severity: "low" | "medium" | "high" | "critical"
  title: string
  message: string
  acknowledged: boolean
  acknowledged_at?: string
  created_at: string
  supplier?: Supplier
  risk_event?: RiskEvent
}

export interface DashboardStats {
  totalSuppliers: number
  activeRisks: number
  criticalAlerts: number
  averageRiskScore: number
}
