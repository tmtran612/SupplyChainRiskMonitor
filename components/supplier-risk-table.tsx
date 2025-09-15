import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { cn, getRiskColor } from "@/lib/utils";
import { useEffect, useState } from "react";

// This matches the new backend DTO
export interface SupplierRiskData {
  supplier: {
    id: number;
    name: string;
    location: string;
    tier: number;
  };
  overall_score: number;
  riskLevel: string; // The backend now provides this
}

export function SupplierRiskTable() {
  const [data, setData] = useState<SupplierRiskData[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/suppliers/risk");
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const result: SupplierRiskData[] = await response.json();
        // Sort by score descending
        result.sort((a, b) => b.overall_score - a.overall_score);
        setData(result);
      } catch (e: any) {
        setError(e.message);
      }
    };
    fetchData();
  }, []);

  if (error) {
    return <div className="text-red-500">Error loading supplier data: {error}</div>;
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Supplier</TableHead>
          <TableHead>Location</TableHead>
          <TableHead>Tier</TableHead>
          <TableHead className="text-right">Risk Score</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {data.map((item) => (
          <TableRow key={item.supplier.id}>
            <TableCell className="font-medium">{item.supplier.name}</TableCell>
            <TableCell>{item.supplier.location}</TableCell>
            <TableCell>{item.supplier.tier}</TableCell>
            <TableCell className="text-right">
              <div className="flex items-center justify-end space-x-2">
                <span>{item.overall_score.toFixed(1)}</span>
                <Badge className={cn("text-white", getRiskColor(item.riskLevel))}>
                  {item.riskLevel.toUpperCase()}
                </Badge>
              </div>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
