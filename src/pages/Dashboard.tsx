import React from 'react';
import { DashboardHeader } from "@/components/dashboard-header";
import { DashboardStats } from "@/components/dashboard-stats";
import { RiskOverviewChart } from "@/components/risk-overview-chart";
import { SupplierRiskTable } from "@/components/supplier-risk-table";
import { AlertsPanel } from "@/components/alerts-panel";
import { RecentEventsPanel } from "@/components/recent-events-panel";
import { PredictionsPanel } from "@/components/predictions-panel";

const Dashboard: React.FC = () => {
  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />
      <main className="container mx-auto px-4 py-6 space-y-6">
        <DashboardStats />
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <RiskOverviewChart />
          </div>
          <div className="lg:col-span-1">
            <AlertsPanel />
          </div>
        </div>
        <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
          <div className="xl:col-span-1">
            <SupplierRiskTable />
          </div>
          <div className="xl:col-span-1">
            <RecentEventsPanel />
          </div>
          <div className="xl:col-span-1">
            <PredictionsPanel />
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;