// Reusable component for displaying risk scores with color coding
import { Badge } from "@/components/ui/badge"
// Backend uses 0-100 scoring with thresholds: <25 low, <50 medium, <75 high, else critical.
// We keep a lightweight local helper but allow overriding via explicit riskLevel passed from API.
import { getRiskLevel, RISK_THRESHOLDS } from "@/lib/risk-scoring"
import { cn } from "@/lib/utils"

interface RiskScoreBadgeProps {
  score: number
  size?: "sm" | "md" | "lg"
  showScore?: boolean
  className?: string
  riskLevelOverride?: string // when provided, use server-calculated level instead of client classification
}

export function RiskScoreBadge({ score, size = "md", showScore = true, className, riskLevelOverride }: RiskScoreBadgeProps) {
  const derived = getRiskLevel(score)
  const riskLevel = (riskLevelOverride || derived) as keyof typeof RISK_THRESHOLDS
  const threshold = RISK_THRESHOLDS[riskLevel]

  const sizeClasses = {
    sm: "text-xs px-2 py-1",
    md: "text-sm px-3 py-1",
    lg: "text-base px-4 py-2",
  }

  const colorClasses = {
    green:
      "bg-green-100 text-green-800 border-green-200 dark:bg-green-900/20 dark:text-green-400 dark:border-green-800",
    yellow:
      "bg-yellow-100 text-yellow-800 border-yellow-200 dark:bg-yellow-900/20 dark:text-yellow-400 dark:border-yellow-800",
    orange:
      "bg-orange-100 text-orange-800 border-orange-200 dark:bg-orange-900/20 dark:text-orange-400 dark:border-orange-800",
    red: "bg-red-100 text-red-800 border-red-200 dark:bg-red-900/20 dark:text-red-400 dark:border-red-800",
  }

  return (
    <Badge variant="outline" className={cn(sizeClasses[size], colorClasses[threshold.color], "font-medium", className)}>
      {riskLevel.toUpperCase()}
      {showScore && ` (${score.toFixed(1)})`}
    </Badge>
  )
}
