import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useEffect, useState } from "react";

interface WeatherFactors {
  severe_precipitation?: number;
  extreme_temperature?: number;
  high_wind?: number;
}

interface DashboardMetrics {
  weatherFactors: WeatherFactors;
}

const factorLabels: Record<keyof WeatherFactors, string> = {
  severe_precipitation: "Severe Precipitation",
  extreme_temperature: "Extreme Temperature",
  high_wind: "High Wind",
};

export function WeatherImpactPanel() {
  const [factors, setFactors] = useState<WeatherFactors>({});
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/dashboard/metrics");
        if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
        const data: DashboardMetrics = await response.json();
        setFactors(data.weatherFactors || {});
      } catch (e: any) {
        setError(e.message);
      }
    };
    fetchMetrics();
  }, []);

  const activeFactors = Object.entries(factors).filter(([, count]) => count > 0);

  if (error) {
    return <div className="text-red-500">Error loading weather impact: {error}</div>;
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Weather-Related Risk Drivers</CardTitle>
      </CardHeader>
      <CardContent>
        {activeFactors.length === 0 ? (
          <p className="text-sm text-gray-500">No significant weather events impacting suppliers.</p>
        ) : (
          <ul className="space-y-2">
            {activeFactors.map(([factor, count]) => (
              <li key={factor} className="flex justify-between items-center">
                <span className="font-medium">{factorLabels[factor as keyof WeatherFactors]}</span>
                <span className="text-sm text-gray-600">{count} {count > 1 ? 'suppliers' : 'supplier'} affected</span>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
