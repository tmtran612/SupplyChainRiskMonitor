import { DashboardHeader } from "@/components/dashboard-header"
import { DashboardStats } from "@/components/dashboard-stats"
import { RiskOverviewChart } from "@/components/risk-overview-chart"
import { SupplierRiskTable } from "@/components/supplier-risk-table"
import { AlertsPanel } from "@/components/alerts-panel"
import { RecentEventsPanel } from "@/components/recent-events-panel"
import { WeatherImpactPanel } from "@/components/weather-impact-panel"

export default function DashboardPage() {
  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />

      <main className="container mx-auto px-4 py-6 space-y-6">
        {/* Key Metrics */}
        <DashboardStats />

        {/* Main Dashboard Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Risk Overview Chart - Takes 2 columns on large screens */}
          <div className="lg:col-span-2">
            <RiskOverviewChart />
          </div>

          {/* Alerts Panel */}
          <div className="lg:col-span-1">
            <AlertsPanel />
          </div>
        </div>

        {/* Secondary Grid */}
        <div className="grid grid-cols-1 xl:grid-cols-4 gap-6">
          {/* Supplier Risk Table */}
          <div className="xl:col-span-1">
            <SupplierRiskTable />
          </div>

          {/* Recent Events */}
          <div className="xl:col-span-1">
            <RecentEventsPanel />
          </div>
          <div className="xl:col-span-1">
            <WeatherImpactPanel />
          </div>
        </div>
      </main>
    </div>
  )
}
