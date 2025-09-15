// Centralized API client utilities for frontend
// Provides typed helpers and consistent error handling.

export interface DashboardMetrics {
  totalSuppliers: number
  activeRisks: number
  criticalAlerts: number
  averageRiskScore: number
  riskDistribution: {
    low: number
    medium: number
    high: number
    critical: number
  }
}

const DEFAULT_BASE_URL = 'http://localhost:8080'

function getBaseUrl(): string {
  // Allow configuration via Vite env variable VITE_API_BASE_URL; fallback to localhost backend.
  const envUrl = (import.meta as any)?.env?.VITE_API_BASE_URL as string | undefined
  return envUrl?.replace(/\/$/, '') || DEFAULT_BASE_URL
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(`HTTP ${res.status} ${res.statusText} - ${text}`)
  }
  return (await res.json()) as T
}

export async function fetchDashboardMetrics(signal?: AbortSignal) {
  const base = getBaseUrl()
  const res = await fetch(`${base}/api/dashboard/metrics`, { signal })
  return handleResponse<DashboardMetrics>(res)
}
