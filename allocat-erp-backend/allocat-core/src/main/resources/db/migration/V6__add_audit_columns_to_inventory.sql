-- Add missing audit columns from BaseEntity to all entity tables

-- Users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Inventory table
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Received stock table
ALTER TABLE received_stock ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE received_stock ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE received_stock ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

