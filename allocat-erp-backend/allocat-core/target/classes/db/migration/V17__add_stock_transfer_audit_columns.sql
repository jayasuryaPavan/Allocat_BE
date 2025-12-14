-- V17: Add missing audit columns to stock_transfers table
-- These columns are required by BaseEntity which StockTransfer extends

DO $$
BEGIN
    -- Add created_by column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'stock_transfers' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE stock_transfers ADD COLUMN created_by BIGINT;
    END IF;

    -- Add updated_by column  
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'stock_transfers' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE stock_transfers ADD COLUMN updated_by BIGINT;
    END IF;

    -- Add deleted_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'stock_transfers' AND column_name = 'deleted_at'
    ) THEN
        ALTER TABLE stock_transfers ADD COLUMN deleted_at TIMESTAMP;
    END IF;
END $$;

-- Update existing rows to have created_at if null
UPDATE stock_transfers SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
