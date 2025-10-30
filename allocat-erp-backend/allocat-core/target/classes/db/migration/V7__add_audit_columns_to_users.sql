-- Add missing audit columns to users table (missed in V6)
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

