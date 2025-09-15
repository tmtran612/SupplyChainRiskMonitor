// API endpoints for AI/ML predictions
import { generateRiskPredictions, generateAllSupplierPredictions } from "@/lib/ai-predictions"
import { type NextRequest, NextResponse } from "next/server"

// GET /api/predictions - Get predictions for all suppliers
export async function GET(request: NextRequest) {
  try {
    const url = new URL(request.url)
    const horizon = (url.searchParams.get("horizon") as "7_days" | "30_days" | "90_days") || "30_days"

    const predictions = await generateAllSupplierPredictions(horizon)
    return NextResponse.json({ predictions })
  } catch (error) {
    console.error("Error fetching predictions:", error)
    return NextResponse.json({ error: "Failed to fetch predictions" }, { status: 500 })
  }
}

// POST /api/predictions - Generate new predictions
export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const { supplierId, horizon = "30_days" } = body

    if (supplierId) {
      // Generate prediction for specific supplier
      const prediction = await generateRiskPredictions(supplierId, horizon)
      return NextResponse.json({ prediction })
    } else {
      // Generate predictions for all suppliers
      const predictions = await generateAllSupplierPredictions(horizon)
      return NextResponse.json({ predictions, count: predictions.length })
    }
  } catch (error) {
    console.error("Error generating predictions:", error)
    return NextResponse.json({ error: "Failed to generate predictions" }, { status: 500 })
  }
}
