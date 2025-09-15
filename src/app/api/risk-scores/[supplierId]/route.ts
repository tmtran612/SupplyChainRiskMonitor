// API endpoint for individual supplier risk scores
import { calculateSupplierRiskScore, getRiskScoreHistory } from "@/lib/risk-scoring"
import { type NextRequest, NextResponse } from "next/server"

export async function GET(request: NextRequest, { params }: { params: Promise<{ supplierId: string }> }) {
  try {
    const { supplierId } = await params
    const url = new URL(request.url)
    const includeHistory = url.searchParams.get("history") === "true"
    const days = Number.parseInt(url.searchParams.get("days") || "30")

    const riskScore = await calculateSupplierRiskScore(supplierId)

    if (!riskScore) {
      return NextResponse.json({ error: "Supplier not found" }, { status: 404 })
    }

    const response: any = { riskScore }

    if (includeHistory) {
      const history = await getRiskScoreHistory(supplierId, days)
      response.history = history
    }

    return NextResponse.json(response)
  } catch (error) {
    console.error("Error fetching supplier risk score:", error)
    return NextResponse.json({ error: "Failed to fetch risk score" }, { status: 500 })
  }
}
