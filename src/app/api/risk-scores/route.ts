// API endpoints for risk scoring operations
import { calculateSupplierRiskScore, updateAllRiskScores, calculatePortfolioRiskMetrics } from "@/lib/risk-scoring"
import { type NextRequest, NextResponse } from "next/server"

// GET /api/risk-scores - Get portfolio risk metrics
export async function GET() {
  try {
    const metrics = await calculatePortfolioRiskMetrics()
    return NextResponse.json(metrics)
  } catch (error) {
    console.error("Error fetching risk metrics:", error)
    return NextResponse.json({ error: "Failed to fetch risk metrics" }, { status: 500 })
  }
}

// POST /api/risk-scores - Update all risk scores
export async function POST(request: NextRequest) {
  try {
    const body = await request.json()

    if (body.action === "update-all") {
      await updateAllRiskScores()
      return NextResponse.json({ success: true, message: "Risk scores updated for all suppliers" })
    }

    if (body.action === "calculate-supplier" && body.supplierId) {
      const riskScore = await calculateSupplierRiskScore(body.supplierId)
      return NextResponse.json({ riskScore })
    }

    return NextResponse.json({ error: "Invalid action" }, { status: 400 })
  } catch (error) {
    console.error("Error processing risk score request:", error)
    return NextResponse.json({ error: "Failed to process request" }, { status: 500 })
  }
}
