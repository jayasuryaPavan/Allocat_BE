-- Add missing BaseEntity columns to user_settings table
-- These columns were missing from V20 and are required by the BaseEntity superclass

ALTER TABLE user_settings ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE user_settings ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE user_settings ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Make updated_at nullable (BaseEntity allows null)
ALTER TABLE user_settings ALTER COLUMN updated_at DROP NOT NULL;
