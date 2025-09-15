import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn, getRiskColor, formatTimestamp } from "@/lib/utils";
import { useEffect, useState } from "react";

// Matches the new backend DTO
export interface AlertData {
  id: number;
  supplierName: string;
  timestamp: string;
  message: string;
  severity: string;
  supplierCurrentRiskLevel: string;
}

export function AlertsPanel() {
  const [alerts, setAlerts] = useState<AlertData[]>([]);
  const [error, setError] = useState<string | null>(null);

  const fetchAlerts = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/alerts/active");
      if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
      const data: AlertData[] = await response.json();
      setAlerts(data);
    } catch (e: any) {
      setError(e.message);
    }
  };

  useEffect(() => {
    fetchAlerts();
  }, []);

  const handleAcknowledge = async (alertId: number) => {
    try {
      await fetch(`http://localhost:8080/api/alerts/${alertId}/acknowledge`, {
        method: "POST",
      });
      // Refresh list after acknowledging
      fetchAlerts();
    } catch (e) {
      console.error("Failed to acknowledge alert", e);
    }
  };

  if (error) {
    return <div className="text-red-500">Error loading alerts: {error}</div>;
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Active Alerts</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {alerts.length === 0 && <p className="text-sm text-gray-500">No active alerts.</p>}
        {alerts.map((alert) => (
          <div key={alert.id} className="flex items-start justify-between">
            <div>
              <p className="font-semibold">
                <span className={cn("mr-2 px-2 py-0.5 rounded-md text-white text-xs", getRiskColor(alert.severity))}>
                  {alert.severity.toUpperCase()}
                </span>
                {alert.supplierName}
              </p>
              <p className="text-sm text-gray-600">{alert.message}</p>
              <p className="text-xs text-gray-400">
                Alert at: {formatTimestamp(alert.timestamp)} | Supplier's current risk:
                <span className={cn("ml-1 font-bold", getRiskColor(alert.supplierCurrentRiskLevel))}>
                  {alert.supplierCurrentRiskLevel.toUpperCase()}
                </span>
              </p>
            </div>
            <Button size="sm" variant="outline" onClick={() => handleAcknowledge(alert.id)}>
              Ack
            </Button>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
