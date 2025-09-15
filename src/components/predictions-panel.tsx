"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Brain, TrendingUp, AlertCircle, Calendar } from "lucide-react"
import { useEffect, useState } from "react"
import { type RiskPrediction } from "@/lib/ai-predictions"

export function PredictionsPanel() {
  const [predictions, setPredictions] = useState<RiskPrediction[]>([])
  const [loading, setLoading] = useState(true)
  const [generating, setGenerating] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchPredictions()
  }, [])

  const fetchPredictions = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/predictions/risk-forecast")
      if (response.ok) {
        const data = await response.json()
        // Transform the backend data to match the frontend interface
        if (data.daily_predictions && Array.isArray(data.daily_predictions)) {
          const transformedPredictions: RiskPrediction[] = data.daily_predictions.map((day: any, index: number) => ({
            predicted_risk_score: day.predicted_risk_score || 0,
            confidence: data.confidence || 0.5,
            prediction_horizon: "7_days",
            risk_factors: [`Day ${index + 1} forecast based on ${data.overall_trend} trend`],
            predicted_events: []
          }))
          setPredictions(transformedPredictions.slice(0, 3)) // Show first 3 days
        } else {
          setError("Unexpected prediction data structure from backend")
        }
      } else {
        setError("Failed to fetch predictions: " + response.statusText)
      }
    } catch (error: any) {
      setError(error?.message || "Error fetching predictions")
    } finally {
      setLoading(false)
    }
  }

  const generateNewPredictions = async () => {
    setGenerating(true)
    try {
      await fetchPredictions()
    } catch (error) {
      setError("Error generating predictions")
    } finally {
      setGenerating(false)
    }
  }

  const getConfidenceColor = (confidence: number) => {
    if (confidence >= 0.8) return "text-green-600"
    if (confidence >= 0.6) return "text-yellow-600"
    return "text-red-600"
  }

  const getRiskColor = (score: number) => {
    if (score >= 8) return "text-red-600"
    if (score >= 6) return "text-orange-600"
    if (score >= 4) return "text-yellow-600"
    return "text-green-600"
  }

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>AI Risk Predictions</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="p-4 border rounded animate-pulse">
                <div className="space-y-3">
                  <div className="h-4 bg-muted rounded w-3/4"></div>
                  <div className="h-3 bg-muted rounded w-full"></div>
                  <div className="h-3 bg-muted rounded w-1/2"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>AI Risk Predictions</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-destructive text-center py-8">{error}</div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-foreground flex items-center gap-2">
            <Brain className="h-5 w-5 text-primary" />
            AI Risk Predictions
          </CardTitle>
          <Button size="sm" onClick={generateNewPredictions} disabled={generating} variant="outline">
            {generating ? "Generating..." : "Update Predictions"}
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {predictions.length === 0 ? (
          <div className="text-center py-8">
            <Brain className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <p className="text-muted-foreground mb-4">No predictions available</p>
            <Button onClick={generateNewPredictions} disabled={generating}>
              Generate Predictions
            </Button>
          </div>
        ) : (
          <div className="space-y-4 max-h-96 overflow-y-auto">
            {predictions.slice(0, 5).map((prediction, index) => (
              <div key={index} className="p-4 border border-border rounded-lg bg-card">
                <div className="space-y-3">
                  {/* Header */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <TrendingUp className="h-4 w-4 text-primary" />
                      <span className="font-medium text-sm text-foreground">Supplier Risk Forecast</span>
                    </div>
                    <Badge variant="outline" className="text-xs">
                      <Calendar className="h-3 w-3 mr-1" />
                      {prediction.prediction_horizon.replace("_", " ")}
                    </Badge>
                  </div>

                  {/* Risk Score Prediction */}
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Predicted Risk Score:</span>
                    <span className={`font-bold ${getRiskColor(prediction.predicted_risk_score)}`}>
                      {prediction.predicted_risk_score.toFixed(1)}/10
                    </span>
                  </div>

                  {/* Confidence */}
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Confidence:</span>
                      <span className={`font-medium ${getConfidenceColor(prediction.confidence)}`}>
                        {Math.round(prediction.confidence * 100)}%
                      </span>
                    </div>
                    <Progress value={prediction.confidence * 100} className="h-2" />
                  </div>

                  {/* Predicted Events */}
                  {prediction.predicted_events.length > 0 && (
                    <div className="space-y-2">
                      <span className="text-sm font-medium text-foreground">Predicted Events:</span>
                      <div className="space-y-1">
                        {prediction.predicted_events.slice(0, 2).map((event, eventIndex) => (
                          <div key={eventIndex} className="flex items-center justify-between text-xs">
                            <div className="flex items-center gap-2">
                              <AlertCircle className="h-3 w-3 text-orange-500" />
                              <span className="text-muted-foreground capitalize">{event.event_type}</span>
                            </div>
                            <span className="text-foreground">{Math.round(event.probability * 100)}%</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Risk Factors */}
                  {prediction.risk_factors.length > 0 && (
                    <div className="space-y-2">
                      <span className="text-sm font-medium text-foreground">Key Risk Factors:</span>
                      <div className="space-y-1">
                        {prediction.risk_factors.slice(0, 2).map((factor, factorIndex) => (
                          <div key={factorIndex} className="text-xs text-muted-foreground flex items-start gap-1">
                            <span className="text-orange-500 mt-0.5">•</span>
                            <span className="line-clamp-2">{factor}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
