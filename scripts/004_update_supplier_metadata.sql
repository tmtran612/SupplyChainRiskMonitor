-- Script: 004_update_supplier_metadata.sql
-- Purpose: Populate or correct supplier metadata (location, tier, industry, contacts) when they are empty/null.
-- Safe to run multiple times (idempotent) provided supplier names are unique.

-- 1. OPTIONAL: Inspect current supplier columns (run manually in psql / Supabase SQL editor)
-- SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'suppliers';
-- SELECT id, name, location, tier, industry FROM suppliers ORDER BY id;

-- 2. (Optional) If your legacy schema used 'region' instead of 'location', migrate it:
-- ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS location VARCHAR(255);
-- UPDATE suppliers SET location = region WHERE (location IS NULL OR location = '') AND region IS NOT NULL;

-- 3. Normalize blank strings to NULL so we can target them uniformly
UPDATE suppliers SET location = NULL WHERE location = '';
UPDATE suppliers SET industry = NULL WHERE industry = '';
-- Tier is numeric; ensure 0 means unknown (adjust if your domain wants 0 retained)
UPDATE suppliers SET tier = NULL WHERE tier = 0;

-- 4. Ensure there is a unique constraint on name for deterministic upsert (skip if already exists)
-- DO NOT run if duplicates exist; clean duplicates first.
-- ALTER TABLE suppliers ADD CONSTRAINT uq_suppliers_name UNIQUE (name);

-- 5. Upsert (insert or update) the canonical semiconductor ecosystem suppliers
--    If a row with the same name exists, its metadata will be updated.
INSERT INTO suppliers (name, location, tier, industry, contact_email, contact_phone, baseline_risk)
VALUES
	('Samsung Electronics', 'Suwon, South Korea', 1, 'Electronics', 'contact@samsung.com', '+82-2-2255-0114', COALESCE((SELECT baseline_risk FROM suppliers WHERE name = 'Samsung Electronics'), 0)),
	('TSMC', 'Hsinchu, Taiwan', 1, 'Semiconductors', 'contact@tsmc.com', '+886-3-563-6688', COALESCE((SELECT baseline_risk FROM suppliers WHERE name = 'TSMC'), 0)),
	('Intel', 'Santa Clara, USA', 1, 'Semiconductors', 'contact@intel.com', '+1-408-765-8080', COALESCE((SELECT baseline_risk FROM suppliers WHERE name = 'Intel'), 0)),
	('ASML', 'Veldhoven, Netherlands', 1, 'Semiconductor Equipment', 'contact@asml.com', '+31-40-268-3000', COALESCE((SELECT baseline_risk FROM suppliers WHERE name = 'ASML'), 0)),
	('Infineon', 'Neubiberg, Germany', 1, 'Semiconductors', 'contact@infineon.com', '+49-89-234-0', COALESCE((SELECT baseline_risk FROM suppliers WHERE name = 'Infineon'), 0))
ON CONFLICT (name) DO UPDATE SET
	location       = EXCLUDED.location,
	tier           = EXCLUDED.tier,
	industry       = EXCLUDED.industry,
	contact_email  = EXCLUDED.contact_email,
	contact_phone  = EXCLUDED.contact_phone
	-- baseline_risk left as-is unless you want to also sync it: baseline_risk = EXCLUDED.baseline_risk
;

-- 6. For any existing rows with NULL metadata, patch them individually (useful if names differ slightly)
-- Example targeted updates (uncomment / adjust as needed):
-- UPDATE suppliers SET industry = 'Manufacturing' WHERE name = 'American Steel Works' AND industry IS NULL;
-- UPDATE suppliers SET industry = 'Textiles' WHERE name = 'Nordic Textiles' AND industry IS NULL;

-- 7. Verification queries (run manually):
-- SELECT name, industry, tier, location FROM suppliers ORDER BY name;

-- 8. (Optional cleanup) Drop legacy region column once fully migrated:
-- ALTER TABLE suppliers DROP COLUMN IF EXISTS region;

-- NOTE: If you do not yet have a unique constraint on name and cannot add one due to duplicates,
-- replace the upsert with individual UPDATE statements like:
-- UPDATE suppliers SET location='Suwon, South Korea', tier=1, industry='Electronics', contact_email='contact@samsung.com', contact_phone='+82-2-2255-0114' WHERE name='Samsung Electronics';
-- (repeat per supplier)

-- End of script
