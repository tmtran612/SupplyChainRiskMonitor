-- PostgreSQL Schema for Supply Chain Risk Monitor
-- Use this with your Spring Boot application

-- Create database (run as superuser)
-- CREATE DATABASE supply_chain_risk_monitor;

-- Connect to the database and create tables
\c supply_chain_risk_monitor;

-- Suppliers table
CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    tier VARCHAR(20) CHECK (tier IN ('tier_1', 'tier_2', 'tier_3')) NOT NULL,
    location VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    risk_score DECIMAL(5,2) DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Risk events table
CREATE TABLE risk_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id UUID REFERENCES suppliers(id) ON DELETE CASCADE,
    event_type VARCHAR(50) CHECK (event_type IN ('supply_disruption', 'quality_issue', 'financial_risk', 'geopolitical', 'natural_disaster', 'cyber_security')) NOT NULL,
    severity VARCHAR(20) CHECK (severity IN ('low', 'medium', 'high', 'critical')) NOT NULL,
    description TEXT NOT NULL,
    impact_score DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE
);

-- Risk scores table (historical tracking)
CREATE TABLE risk_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id UUID REFERENCES suppliers(id) ON DELETE CASCADE,
    score DECIMAL(5,2) NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    factors JSONB
);

-- Alerts table
CREATE TABLE alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id UUID REFERENCES suppliers(id) ON DELETE CASCADE,
    alert_type VARCHAR(50) CHECK (alert_type IN ('risk_threshold', 'new_event', 'supplier_degradation')) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) CHECK (severity IN ('low', 'medium', 'high', 'critical')) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_at TIMESTAMP WITH TIME ZONE
);

-- Risk predictions table
CREATE TABLE risk_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id UUID REFERENCES suppliers(id) ON DELETE CASCADE,
    predicted_risk_score DECIMAL(5,2) NOT NULL,
    confidence DECIMAL(5,2) NOT NULL,
    prediction_horizon_days INTEGER NOT NULL,
    factors TEXT[] NOT NULL,
    explanation TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_suppliers_risk_score ON suppliers(risk_score DESC);
CREATE INDEX idx_risk_events_supplier_id ON risk_events(supplier_id);
CREATE INDEX idx_risk_events_created_at ON risk_events(created_at DESC);
CREATE INDEX idx_alerts_supplier_id ON alerts(supplier_id);
CREATE INDEX idx_alerts_acknowledged ON alerts(acknowledged);
CREATE INDEX idx_risk_scores_supplier_id ON risk_scores(supplier_id);
CREATE INDEX idx_risk_predictions_supplier_id ON risk_predictions(supplier_id);

-- Insert sample data
INSERT INTO suppliers (name, tier, location, contact_email, risk_score) VALUES
('TechCorp Manufacturing', 'tier_1', 'Shanghai, China', 'contact@techcorp.com', 75.5),
('Global Components Ltd', 'tier_2', 'Mumbai, India', 'info@globalcomp.com', 45.2),
('Precision Parts Inc', 'tier_1', 'Detroit, USA', 'sales@precisionparts.com', 32.1),
('European Suppliers SA', 'tier_2', 'Berlin, Germany', 'contact@eurosuppliers.com', 58.7),
('Asian Electronics Co', 'tier_3', 'Seoul, South Korea', 'info@asianelec.com', 67.3);
