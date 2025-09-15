"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { TrendingUp, TrendingDown, AlertTriangle, Users, Activity } from "lucide-react"
import { useEffect, useState } from "react"
import { fetchDashboardMetrics } from "@/lib/api"

interface DashboardMetrics {
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

export function DashboardStats() {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const data = await fetchDashboardMetrics()
        setMetrics(data)
      } catch (error: any) {
        setError(error?.message || "Failed to fetch dashboard metrics")
      } finally {
        setLoading(false)
      }
    }

    fetchMetrics()
  }, [])

  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {[...Array(4)].map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader className="pb-2">
              <div className="h-4 bg-muted rounded w-3/4"></div>
            </CardHeader>
            <CardContent>
              <div className="h-8 bg-muted rounded w-1/2 mb-2"></div>
              <div className="h-3 bg-muted rounded w-full"></div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  if (error) {
    return <div className="text-destructive text-center py-8">{error}</div>
  }

  if (!metrics) return null

  const riskTrend = metrics.averageRiskScore > 5 ? "up" : "down"
  const criticalPercentage =
    metrics.totalSuppliers > 0 ? Math.round((metrics.riskDistribution.critical / metrics.totalSuppliers) * 100) : 0

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Total Suppliers</CardTitle>
          <Users className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-foreground">{metrics.totalSuppliers}</div>
          <p className="text-xs text-muted-foreground">{metrics.riskDistribution.critical} critical risk</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Active Risks</CardTitle>
          <Activity className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-foreground">{metrics.activeRisks}</div>
          <p className="text-xs text-muted-foreground">Unresolved risk events</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Critical Alerts</CardTitle>
          <AlertTriangle className="h-4 w-4 text-destructive" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-destructive">{metrics.criticalAlerts}</div>
          <p className="text-xs text-muted-foreground">Require immediate attention</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">Avg Risk Score</CardTitle>
          {riskTrend === "up" ? (
            <TrendingUp className="h-4 w-4 text-destructive" />
          ) : (
            <TrendingDown className="h-4 w-4 text-primary" />
          )}
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold text-foreground">{metrics.averageRiskScore.toFixed(1)}</div>
          <p className="text-xs text-muted-foreground">{criticalPercentage}% suppliers at critical risk</p>
        </CardContent>
      </Card>
    </div>
  )
}
