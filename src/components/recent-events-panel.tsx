"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Activity, Cloud, DollarSign, Cog, FileText, Globe } from "lucide-react"
import { useEffect, useState } from "react"

interface EventDTOBackend {
  id: number
  supplierName: string
  timestamp: string
  type: string
  payload: string
}

export function RecentEventsPanel() {
  const [events, setEvents] = useState<EventDTOBackend[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchEvents = async () => {
      try {
  const response = await fetch("http://localhost:8080/api/events/recent")
        if (response.ok) {
          const data = await response.json()
          setEvents(data)
        } else {
          setError("Failed to fetch events: " + response.statusText)
        }
      } catch (error: any) {
        setError(error?.message || "Error fetching events")
      } finally {
        setLoading(false)
      }
    }

    fetchEvents()
  }, [])

  const getEventIcon = (eventType: string) => {
    switch (eventType) {
      case "weather":
        return <Cloud className="h-4 w-4 text-blue-500" />
      case "weather_risk_adjustment":
        return <Cloud className="h-4 w-4 text-cyan-500" />
      case "financial":
        return <DollarSign className="h-4 w-4 text-green-500" />
      case "operational":
        return <Cog className="h-4 w-4 text-orange-500" />
      case "regulatory":
        return <FileText className="h-4 w-4 text-purple-500" />
      case "geopolitical":
        return <Globe className="h-4 w-4 text-red-500" />
      default:
        return <Activity className="h-4 w-4 text-gray-500" />
    }
  }

  // derive pseudo severity from payload/type for display (quick heuristic)
  const deriveSeverity = (e: EventDTOBackend): string => {
    if (e.type.startsWith("weather_risk_adjustment")) return "high"
    if (e.type.startsWith("weather")) return "medium"
    return "low"
  }

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case "critical": return "destructive"
      case "high": return "secondary"
      case "medium": return "outline"
      default: return "outline"
    }
  }

  const formatTime = (iso: string) => {
    try {
      const d = new Date(iso)
      if (isNaN(d.getTime())) return '—'
      const diff = Date.now() - d.getTime()
      const mins = Math.floor(diff/60000)
      if (mins < 60) return mins <= 1 ? 'Just now' : mins + 'm ago'
      return d.toLocaleString()
    } catch { return '—' }
  }

  const humanize = (e: EventDTOBackend): string => {
    if (e.type === 'weather_risk_adjustment') {
      // New payload format: adjustment=X.XX; precip=Y.YYmm(+Z.ZZ); wind=A.A(+B.BB); temp=C.CC(+D.DD); tokens=...
      const adjMatch = e.payload.match(/adjustment=([0-9.]+)/)
      const precipMatch = e.payload.match(/precip=([0-9.]+(?:\.[0-9]+)?)mm/)
      const windMatch = e.payload.match(/wind=([0-9.]+(?:\.[0-9]+)?)(?:\(|$)/)
      const tempMatch = e.payload.match(/temp=([0-9.-]+)/)
      
      const adj = adjMatch ? parseFloat(adjMatch[1]).toFixed(1) : '?'
      let details = []
      
      if (precipMatch && parseFloat(precipMatch[1]) > 0) {
        details.push(`${precipMatch[1]}mm rain`)
      }
      if (windMatch && parseFloat(windMatch[1]) > 10) {
        details.push(`${parseFloat(windMatch[1]).toFixed(1)}m/s wind`)
      }
      if (tempMatch) {
        const temp = parseFloat(tempMatch[1])
        if (temp < -5 || temp > 38) {
          details.push(`${temp.toFixed(1)}°C`)
        }
      }
      
      const summary = details.length > 0 ? details.join(', ') : 'weather factors'
      return `Weather risk +${adj} (${summary})`
    }
    return e.payload.length > 120 ? e.payload.slice(0,117) + '…' : e.payload
  }

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Recent Risk Events</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="flex items-start gap-3 p-3 border rounded animate-pulse">
                <div className="w-4 h-4 bg-muted rounded"></div>
                <div className="flex-1 space-y-2">
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
          <CardTitle>Recent Risk Events</CardTitle>
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
          <Activity className="h-5 w-5" />
          Recent Risk Events
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-3 max-h-96 overflow-y-auto">
          {events.map((event) => {
            const severity = deriveSeverity(event)
            return (
            <div key={event.id} className="flex items-start gap-3 p-3 border border-border rounded-lg bg-card">
              {getEventIcon(event.type)}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <Badge variant={getSeverityColor(severity) as any} className="text-xs capitalize">{severity}</Badge>
                  <Badge variant="outline" className="text-xs">{event.type.replace(/_/g,' ')}</Badge>
                </div>
                <p className="text-sm text-foreground font-medium mb-1 line-clamp-2">{humanize(event)}</p>
                <div className="text-xs text-muted-foreground flex flex-wrap gap-3">
                  <span>Supplier: {event.supplierName}</span>
                  <span>{formatTime(event.timestamp)}</span>
                </div>
              </div>
            </div>)
          })}
        </div>
      </CardContent>
    </Card>
  )
}
