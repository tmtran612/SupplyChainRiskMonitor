// API endpoint to trigger risk simulation
import { runRiskSimulation } from "@/lib/simulation"
import { NextResponse } from "next/server"

export async function POST() {
  try {
    await runRiskSimulation()
    return NextResponse.json({ success: true, message: "Risk simulation completed" })
  } catch (error) {
    console.error("Simulation error:", error)
    return NextResponse.json({ success: false, error: "Failed to run simulation" }, { status: 500 })
  }
}

export async function GET() {
  // Allow GET requests to trigger simulation for testing
  return POST()
}
