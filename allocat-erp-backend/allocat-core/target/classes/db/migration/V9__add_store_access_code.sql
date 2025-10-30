-- Add access_code column to stores table
ALTER TABLE stores ADD COLUMN IF NOT EXISTS access_code VARCHAR(50);

-- Set a default access code for existing stores (should be changed by admin)
UPDATE stores SET access_code = 'CHANGE_ME_' || id WHERE access_code IS NULL;

-- Make access_code NOT NULL after setting defaults
ALTER TABLE stores ALTER COLUMN access_code SET NOT NULL;

-- Create index on access_code for faster lookups
CREATE INDEX IF NOT EXISTS idx_stores_access_code ON stores(access_code);

-- Add audit columns if they don't exist
ALTER TABLE stores ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

