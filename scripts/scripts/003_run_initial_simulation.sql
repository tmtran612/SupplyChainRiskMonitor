-- This script can be used to set up initial simulation data
-- Run this after the basic schema and seed data are in place

-- Update some existing risk events to be resolved (for testing)
UPDATE risk_events 
SET resolved_at = NOW() - INTERVAL '1 day'
WHERE severity = 'low' 
AND resolved_at IS NULL 
AND RANDOM() < 0.5;

-- Add some additional risk events with different timestamps for variety
INSERT INTO risk_events (supplier_id, event_type, severity, description, location, impact_score, probability, detected_at) 
SELECT 
  s.id,
  (ARRAY['weather', 'geopolitical', 'financial', 'operational', 'regulatory'])[floor(random() * 5 + 1)],
  (ARRAY['low', 'medium', 'high'])[floor(random() * 3 + 1)],
  'Simulated risk event for testing',
  s.location,
  ROUND((RANDOM() * 6 + 2)::numeric, 2), -- Score between 2-8
  ROUND((RANDOM() * 0.6 + 0.2)::numeric, 2), -- Probability between 0.2-0.8
  NOW() - INTERVAL '1 hour' * floor(random() * 72) -- Random time in last 3 days
FROM suppliers s
WHERE RANDOM() < 0.7; -- 70% of suppliers get an additional event

-- Create some historical risk scores for trending
INSERT INTO risk_scores (supplier_id, overall_score, weather_score, geopolitical_score, financial_score, operational_score, regulatory_score, calculated_at)
SELECT 
  s.id,
  ROUND((RANDOM() * 4 + 2)::numeric, 2), -- Overall score between 2-6
  ROUND((RANDOM() * 3 + 1)::numeric, 2), -- Weather score between 1-4
  ROUND((RANDOM() * 4 + 1)::numeric, 2), -- Geopolitical score between 1-5
  ROUND((RANDOM() * 3 + 1)::numeric, 2), -- Financial score between 1-4
  ROUND((RANDOM() * 5 + 1)::numeric, 2), -- Operational score between 1-6
  ROUND((RANDOM() * 2 + 1)::numeric, 2), -- Regulatory score between 1-3
  NOW() - INTERVAL '1 day' * generate_series(1, 7) -- One score per day for last week
FROM suppliers s;
