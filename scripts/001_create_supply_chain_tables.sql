-- Supply Chain Risk Monitor Database Schema
-- Creates tables for suppliers, risk events, risk scores, and alerts

-- Suppliers table
CREATE TABLE IF NOT EXISTS suppliers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  location TEXT NOT NULL,
  tier INTEGER NOT NULL CHECK (tier IN (1, 2, 3)),
  industry TEXT NOT NULL,
  contact_email TEXT,
  contact_phone TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Risk events table
CREATE TABLE IF NOT EXISTS risk_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  supplier_id UUID REFERENCES suppliers(id) ON DELETE CASCADE,
  event_type TEXT NOT NULL CHECK (event_type IN ('weather', 'geopolitical', 'financial', 'operational', 'regulatory')),
  severity TEXT NOT NULL CHECK (severity IN ('low', 'medium', 'high', 'critical')),
  description TEXT NOT NULL,
  location TEXT NOT NULL,
  impact_score DECIMAL(3,2) NOT NULL CHECK (impact_score >= 0 AND impact_score <= 10),
  probability DECIMAL(3,2) NOT NULL CHECK (probability >= 0 AND probability <= 1),
  detected_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  resolved_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Risk scores table (historical tracking)
CREATE TABLE IF NOT EXISTS risk_scores (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  supplier_id UUID REFERENCES suppliers(id) ON DELETE CASCADE,
  overall_score DECIMAL(3,2) NOT NULL CHECK (overall_score >= 0 AND overall_score <= 10),
  weather_score DECIMAL(3,2) NOT NULL CHECK (weather_score >= 0 AND weather_score <= 10),
  geopolitical_score DECIMAL(3,2) NOT NULL CHECK (geopolitical_score >= 0 AND geopolitical_score <= 10),
  financial_score DECIMAL(3,2) NOT NULL CHECK (financial_score >= 0 AND financial_score <= 10),
  operational_score DECIMAL(3,2) NOT NULL CHECK (operational_score >= 0 AND operational_score <= 10),
  regulatory_score DECIMAL(3,2) NOT NULL CHECK (regulatory_score >= 0 AND regulatory_score <= 10),
  calculated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Alerts table
CREATE TABLE IF NOT EXISTS alerts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  supplier_id UUID REFERENCES suppliers(id) ON DELETE CASCADE,
  risk_event_id UUID REFERENCES risk_events(id) ON DELETE CASCADE,
  alert_type TEXT NOT NULL CHECK (alert_type IN ('threshold_breach', 'new_risk', 'escalation', 'resolution')),
  severity TEXT NOT NULL CHECK (severity IN ('low', 'medium', 'high', 'critical')),
  title TEXT NOT NULL,
  message TEXT NOT NULL,
  acknowledged BOOLEAN DEFAULT FALSE,
  acknowledged_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_suppliers_tier ON suppliers(tier);
CREATE INDEX IF NOT EXISTS idx_suppliers_location ON suppliers(location);
CREATE INDEX IF NOT EXISTS idx_risk_events_supplier_id ON risk_events(supplier_id);
CREATE INDEX IF NOT EXISTS idx_risk_events_event_type ON risk_events(event_type);
CREATE INDEX IF NOT EXISTS idx_risk_events_severity ON risk_events(severity);
CREATE INDEX IF NOT EXISTS idx_risk_events_detected_at ON risk_events(detected_at);
CREATE INDEX IF NOT EXISTS idx_risk_scores_supplier_id ON risk_scores(supplier_id);
CREATE INDEX IF NOT EXISTS idx_risk_scores_calculated_at ON risk_scores(calculated_at);
CREATE INDEX IF NOT EXISTS idx_alerts_supplier_id ON alerts(supplier_id);
CREATE INDEX IF NOT EXISTS idx_alerts_acknowledged ON alerts(acknowledged);
CREATE INDEX IF NOT EXISTS idx_alerts_created_at ON alerts(created_at);

-- Enable Row Level Security (RLS) for all tables
ALTER TABLE suppliers ENABLE ROW LEVEL SECURITY;
ALTER TABLE risk_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE risk_scores ENABLE ROW LEVEL SECURITY;
ALTER TABLE alerts ENABLE ROW LEVEL SECURITY;

-- Create policies for public access (since this is a monitoring system)
-- In production, you would want more restrictive policies based on user roles
CREATE POLICY "Allow public read access to suppliers" ON suppliers FOR SELECT USING (true);
CREATE POLICY "Allow public read access to risk_events" ON risk_events FOR SELECT USING (true);
CREATE POLICY "Allow public read access to risk_scores" ON risk_scores FOR SELECT USING (true);
CREATE POLICY "Allow public read access to alerts" ON alerts FOR SELECT USING (true);

-- Allow inserts for the simulation engine
CREATE POLICY "Allow public insert to suppliers" ON suppliers FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public insert to risk_events" ON risk_events FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public insert to risk_scores" ON risk_scores FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public insert to alerts" ON alerts FOR INSERT WITH CHECK (true);

-- Allow updates for alert acknowledgment
CREATE POLICY "Allow public update to alerts" ON alerts FOR UPDATE USING (true);
