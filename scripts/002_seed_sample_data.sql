-- Seed sample data for the Supply Chain Risk Monitor
-- This creates realistic sample suppliers and initial risk data

-- Insert sample suppliers
INSERT INTO suppliers (name, location, tier, industry, contact_email, contact_phone) VALUES
('TechComponents Ltd', 'Shenzhen, China', 1, 'Electronics', 'contact@techcomponents.com', '+86-755-1234567'),
('Global Logistics Corp', 'Hamburg, Germany', 2, 'Logistics', 'info@globallogistics.de', '+49-40-987654'),
('Pacific Materials', 'Tokyo, Japan', 1, 'Raw Materials', 'sales@pacificmaterials.jp', '+81-3-5555-0123'),
('American Steel Works', 'Pittsburgh, USA', 2, 'Manufacturing', 'orders@americansteel.com', '+1-412-555-0199'),
('Nordic Textiles', 'Stockholm, Sweden', 3, 'Textiles', 'contact@nordictextiles.se', '+46-8-123-4567'),
('Southeast Plastics', 'Bangkok, Thailand', 2, 'Plastics', 'info@southeastplastics.th', '+66-2-555-0123'),
('European Auto Parts', 'Munich, Germany', 1, 'Automotive', 'sales@euroautoparts.de', '+49-89-555-0199'),
('Indian Pharmaceuticals', 'Mumbai, India', 2, 'Pharmaceuticals', 'contact@indianpharma.in', '+91-22-5555-0123'),
('Brazilian Mining Co', 'São Paulo, Brazil', 1, 'Mining', 'info@brazilmining.com.br', '+55-11-5555-0199'),
('Australian Agriculture', 'Sydney, Australia', 3, 'Agriculture', 'sales@ausagriculture.com.au', '+61-2-5555-0123');

-- Insert sample risk events
INSERT INTO risk_events (supplier_id, event_type, severity, description, location, impact_score, probability) 
SELECT 
  s.id,
  'weather',
  'high',
  'Typhoon warning affecting manufacturing facilities',
  s.location,
  7.5,
  0.8
FROM suppliers s WHERE s.name = 'TechComponents Ltd';

INSERT INTO risk_events (supplier_id, event_type, severity, description, location, impact_score, probability) 
SELECT 
  s.id,
  'geopolitical',
  'medium',
  'Trade policy changes affecting import/export',
  s.location,
  5.2,
  0.6
FROM suppliers s WHERE s.name = 'Global Logistics Corp';

INSERT INTO risk_events (supplier_id, event_type, severity, description, location, impact_score, probability) 
SELECT 
  s.id,
  'financial',
  'low',
  'Currency fluctuation impacting costs',
  s.location,
  3.1,
  0.4
FROM suppliers s WHERE s.name = 'Pacific Materials';

INSERT INTO risk_events (supplier_id, event_type, severity, description, location, impact_score, probability) 
SELECT 
  s.id,
  'operational',
  'critical',
  'Equipment failure causing production delays',
  s.location,
  9.2,
  0.9
FROM suppliers s WHERE s.name = 'American Steel Works';

-- Insert initial risk scores for all suppliers
INSERT INTO risk_scores (supplier_id, overall_score, weather_score, geopolitical_score, financial_score, operational_score, regulatory_score)
SELECT 
  id,
  ROUND((RANDOM() * 4 + 3)::numeric, 2), -- Overall score between 3-7
  ROUND((RANDOM() * 3 + 2)::numeric, 2), -- Weather score between 2-5
  ROUND((RANDOM() * 4 + 1)::numeric, 2), -- Geopolitical score between 1-5
  ROUND((RANDOM() * 3 + 2)::numeric, 2), -- Financial score between 2-5
  ROUND((RANDOM() * 5 + 1)::numeric, 2), -- Operational score between 1-6
  ROUND((RANDOM() * 2 + 1)::numeric, 2)  -- Regulatory score between 1-3
FROM suppliers;

-- Insert sample alerts
INSERT INTO alerts (supplier_id, risk_event_id, alert_type, severity, title, message)
SELECT 
  re.supplier_id,
  re.id,
  'threshold_breach',
  re.severity,
  'High Risk Event Detected',
  'Risk threshold exceeded for ' || re.event_type || ' event: ' || re.description
FROM risk_events re
WHERE re.severity IN ('high', 'critical');
