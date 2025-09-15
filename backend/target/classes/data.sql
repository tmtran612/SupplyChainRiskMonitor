-- Seed sample data for the Supply Chain Risk Monitor
-- This creates realistic sample suppliers matching the UUID schema

-- Insert sample suppliers (5 suppliers as requested)
INSERT INTO suppliers (id, name, location, tier, industry, contact_email, contact_phone, created_at, updated_at) VALUES
(gen_random_uuid(), 'TechComponents Ltd', 'Shenzhen, China', 1, 'Electronics', 'contact@techcomponents.com', '+86-755-1234-5678', NOW(), NOW()),
(gen_random_uuid(), 'Global Logistics Corp', 'Hamburg, Germany', 2, 'Logistics', 'contact@globallogistics.com', '+49-40-1234-5678', NOW(), NOW()),
(gen_random_uuid(), 'Pacific Materials', 'Tokyo, Japan', 1, 'Raw Materials', 'contact@pacificmaterials.com', '+81-3-1234-5678', NOW(), NOW()),
(gen_random_uuid(), 'American Steel Works', 'Pittsburgh, USA', 2, 'Manufacturing', 'contact@americansteel.com', '+1-412-1234-5678', NOW(), NOW()),
(gen_random_uuid(), 'Nordic Textiles', 'Stockholm, Sweden', 3, 'Textiles', 'contact@nordictextiles.com', '+46-8-1234-5678', NOW(), NOW());