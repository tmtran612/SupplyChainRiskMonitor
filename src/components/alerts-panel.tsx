"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { AlertTriangle, CheckCircle, Clock } from "lucide-react"
import { useEffect, useState } from "react"

interface AlertDTO {
  id: number
  supplierName: string
  timestamp: string
  message: string
  severity: string
  supplierCurrentRiskLevel: string
}

export function AlertsPanel() {
  const [alerts, setAlerts] = useState<AlertDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchAlerts = async () => {
      try {
  const response = await fetch("http://localhost:8080/api/alerts/active")
        if (response.ok) {
          const data = await response.json()
          setAlerts(data)
        } else {
          setError("Failed to fetch alerts: " + response.statusText)
        }
      } catch (error: any) {
        setError(error?.message || "Error fetching alerts")
      } finally {
        setLoading(false)
      }
    }

    fetchAlerts()
  }, [])


  const getSeverityIcon = (severity: string) => {
    switch (severity) {
      case "critical":
        return <AlertTriangle className="h-4 w-4 text-destructive" />
      case "high":
        return <AlertTriangle className="h-4 w-4 text-orange-500" />
      case "medium":
        return <Clock className="h-4 w-4 text-yellow-500" />
      default:
        return <Clock className="h-4 w-4 text-blue-500" />
    }
  }

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case "critical":
        return "destructive"
      case "high":
        return "secondary"
      case "medium":
        return "outline"
      default:
        return "outline"
    }
  }

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Active Alerts</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="p-3 border rounded animate-pulse">
                <div className="space-y-2">
                  <div className="h-4 bg-muted rounded w-3/4"></div>
                  <div className="h-3 bg-muted rounded w-full"></div>
                  <div className="h-3 bg-muted rounded w-1/2"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Active Alerts</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-destructive text-center py-8">{error}</div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-foreground flex items-center gap-2">
          <AlertTriangle className="h-5 w-5" />
          Active Alerts
          {alerts.length > 0 && (
            <Badge variant="destructive" className="ml-auto">
              {alerts.length}
            </Badge>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent>
        {alerts.length === 0 ? (
          <div className="text-center py-8">
            <CheckCircle className="h-12 w-12 text-primary mx-auto mb-4" />
            <p className="text-muted-foreground">No active alerts</p>
          </div>
        ) : (
          <div className="space-y-3">
            {alerts.map((alert) => {
              let dateString = '—'
              try {
                const d = new Date(alert.timestamp)
                if (!isNaN(d.getTime())) {
                  const now = Date.now()
                  const diffMs = now - d.getTime()
                  const diffMin = Math.floor(diffMs / 60000)
                  if (diffMin < 60) {
                    dateString = diffMin <= 1 ? 'Just now' : diffMin + 'm ago'
                  } else {
                    dateString = d.toLocaleString()
                  }
                }
              } catch {}
              return (
              <div key={alert.id} className="p-3 border border-border rounded-lg bg-card">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3 flex-1">
                    {getSeverityIcon(alert.severity)}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <h4 className="font-medium text-sm text-foreground truncate">{alert.supplierName}</h4>
                        <Badge variant={getSeverityColor(alert.severity) as any} className="text-xs capitalize">
                          {alert.severity}
                        </Badge>
                        <Badge variant="outline" className="text-xs">
                          {alert.supplierCurrentRiskLevel}
                        </Badge>
                      </div>
                      <p className="text-xs text-muted-foreground mb-2 line-clamp-3">{alert.message}</p>
                      <p className="text-xs text-muted-foreground">{dateString}</p>
                    </div>
                  </div>
                  {/* Acknowledge removed per user request */}
                </div>
              </div>
              )})}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
