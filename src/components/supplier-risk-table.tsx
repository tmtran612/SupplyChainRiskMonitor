"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { RiskScoreBadge } from "@/components/risk-score-badge"
import { Badge } from "@/components/ui/badge"
import { useEffect, useState } from "react"
import type { Supplier } from "@/lib/types"
import { AlertTriangle } from "lucide-react"

// Matches SupplierRiskDTO from backend
interface SupplierWithRisk {
  supplier: Supplier
  overall_score: number
  performance_score?: number
  financial_score?: number
  geopolitical_score?: number
  environmental_score?: number
  riskLevel: string
  reason?: string
}

export function SupplierRiskTable() {
  const [suppliers, setSuppliers] = useState<SupplierWithRisk[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchSuppliers = async () => {
      try {
        // Fetch data from the Spring Boot backend
        const response = await fetch("http://localhost:8080/api/suppliers/risk")
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        const data: SupplierWithRisk[] = await response.json()

        // Sort by overall_score and take the top 10
        const topSuppliers = [...data].sort((a, b) => b.overall_score - a.overall_score).slice(0, 10)
        setSuppliers(topSuppliers)
      } catch (error: any) {
        setError(error.message || 'Failed to load suppliers')
      } finally {
        setLoading(false)
      }
    };

    fetchSuppliers();
  }, [])

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Top Risk Suppliers</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="flex items-center justify-between p-3 border rounded animate-pulse">
                <div className="space-y-2">
                  <div className="h-4 bg-muted rounded w-32"></div>
                  <div className="h-3 bg-muted rounded w-24"></div>
                </div>
                <div className="h-6 bg-muted rounded w-16"></div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Top Risk Suppliers</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-destructive">{error}</p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-foreground">Top Risk Suppliers</CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="text-muted-foreground">Supplier</TableHead>
              <TableHead className="text-muted-foreground">Location</TableHead>
              <TableHead className="text-muted-foreground">Tier</TableHead>
              <TableHead className="text-muted-foreground">Risk</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {suppliers.map((supplier) => (
              <TableRow key={supplier.supplier.id}>
                <TableCell>
                  <div className="space-y-1">
                    <div className="font-medium text-foreground">{supplier.supplier.name}</div>
                    {(() => {
                      const reason = supplier.reason?.trim();
                      const industry = supplier.supplier.industry?.trim();
                      if (!reason && industry) {
                        return <div className="text-xs text-muted-foreground">{industry}</div>;
                      }
                      if (reason && industry && reason.toLowerCase() === industry.toLowerCase()) {
                        // Reason is just the industry already – show only once
                        return <div className="text-xs text-muted-foreground">{industry}</div>;
                      }

                      if (reason && /^weather adjustment:/i.test(reason)) {
                        // Extract detail inside parentheses for factors
                        const detailMatch = reason.match(/\((.*)\)/);
                        const factors = detailMatch ? detailMatch[1].split(/;\s*/).filter(Boolean) : [];
                        const adjMatch = reason.match(/weather adjustment:\s*\+([0-9.]+)/i);
                        const adjValue = adjMatch ? adjMatch[1] : null;
                        return (
                          <div className="space-y-0.5">
                            <div className="text-xs text-muted-foreground" title={reason}>
                              +{adjValue || ''} weather impact
                            </div>
                            {factors.length > 0 && (
                              <div className="flex flex-wrap gap-1">
                                {factors.map(f => (
                                  <span key={f} className="rounded bg-muted px-1.5 py-0.5 text-[10px] text-foreground/70 border" title={f}>{f.replace(/->.*$/, '')}</span>
                                ))}
                              </div>
                            )}
                            {industry && <div className="text-xs text-muted-foreground">{industry}</div>}
                          </div>
                        );
                      }

                      if (reason && /^weather stable:/i.test(reason)) {
                        return (
                          <div className="space-y-0.5" title={reason}>
                            <div className="text-xs text-muted-foreground">Stable conditions</div>
                            {industry && <div className="text-xs text-muted-foreground">{industry}</div>}
                          </div>
                        );
                      }

                      // Generic fallback: show reason then industry (if different)
                      return (
                        <div className="space-y-0.5">
                          {reason && <div className="text-xs text-muted-foreground line-clamp-1" title={reason}>{reason}</div>}
                          {industry && <div className="text-xs text-muted-foreground">{industry}</div>}
                        </div>
                      );
                    })()}
                  </div>
                </TableCell>
                <TableCell className="text-foreground">{supplier.supplier.location}</TableCell>
                <TableCell>
                  <Badge variant="outline" className="text-xs">
                    Tier {supplier.supplier.tier}
                  </Badge>
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-2" title={supplier.reason || undefined}>
                    <RiskScoreBadge score={supplier.overall_score} size="sm" riskLevelOverride={supplier.riskLevel} />
                    {supplier.reason?.match(/severe_precipitation|extreme_temperature|high_wind/) && (
                      <AlertTriangle className="h-3 w-3 text-blue-500" aria-label="Weather factor present" />
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}
