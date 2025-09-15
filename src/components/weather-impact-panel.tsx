import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useEffect, useState } from "react";

interface WeatherFactors {
  severe_precipitation?: number;
  extreme_temperature?: number;
  high_wind?: number;
}

interface DashboardMetricsWeatherOnly {
  weatherFactors: WeatherFactors;
  weatherNarrative?: string;
}

const factorLabels: Record<keyof WeatherFactors, string> = {
  severe_precipitation: "Severe Precipitation",
  extreme_temperature: "Extreme Temperature",
  high_wind: "High Wind",
};

export function WeatherImpactPanel() {
  const [factors, setFactors] = useState<WeatherFactors>({});
  const [error, setError] = useState<string | null>(null);
  const [narrative, setNarrative] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/dashboard/metrics");
        if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
  const data: DashboardMetricsWeatherOnly = await response.json();
  setFactors(data.weatherFactors || {});
  setNarrative(data.weatherNarrative || "");
      } catch (e: any) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    };
    fetchMetrics();
  }, []);

  const activeFactors = Object.entries(factors).filter(([, count]) => count && count > 0);

  return (
    <Card>
      <CardHeader>
        <CardTitle>Weather-Related Risk Drivers</CardTitle>
        {narrative && (
          <p className="text-xs text-muted-foreground mt-1 leading-relaxed">{narrative}</p>
        )}
      </CardHeader>
      <CardContent>
        {loading && <p className="text-sm text-muted-foreground">Loading weather impact...</p>}
        {!loading && error && <p className="text-sm text-destructive">{error}</p>}
        {!loading && !error && activeFactors.length === 0 && (
          <p className="text-sm text-muted-foreground">No significant weather events impacting suppliers.</p>
        )}
        {!loading && !error && activeFactors.length > 0 && (
          <ul className="space-y-2">
            {activeFactors.map(([factor, count]) => (
              <li key={factor} className="flex justify-between items-center">
                <span className="font-medium">{factorLabels[factor as keyof WeatherFactors]}</span>
                <span className="text-sm text-muted-foreground">{count} {count && count > 1 ? 'suppliers' : 'supplier'} affected</span>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
