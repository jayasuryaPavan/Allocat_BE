-- V18: Add missing BaseEntity audit columns to all tables
-- This migration ensures all tables used by entities extending BaseEntity have the required audit columns
-- Required columns: created_at, created_by, updated_at, updated_by, deleted_at

-- =====================================================
-- FUNCTION: Add audit columns to a table if it exists and columns are missing
-- =====================================================
CREATE OR REPLACE FUNCTION add_audit_columns_if_missing(table_name_param TEXT)
RETURNS VOID AS $$
BEGIN
    -- Check if table exists first
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = table_name_param
    ) THEN
        RAISE NOTICE 'Table % does not exist, skipping', table_name_param;
        RETURN;
    END IF;

    -- Add created_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = table_name_param AND column_name = 'created_at'
    ) THEN
        EXECUTE format('ALTER TABLE %I ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP', table_name_param);
        RAISE NOTICE 'Added created_at to %', table_name_param;
    END IF;

    -- Add created_by column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = table_name_param AND column_name = 'created_by'
    ) THEN
        EXECUTE format('ALTER TABLE %I ADD COLUMN created_by BIGINT', table_name_param);
        RAISE NOTICE 'Added created_by to %', table_name_param;
    END IF;

    -- Add updated_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = table_name_param AND column_name = 'updated_at'
    ) THEN
        EXECUTE format('ALTER TABLE %I ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP', table_name_param);
        RAISE NOTICE 'Added updated_at to %', table_name_param;
    END IF;

    -- Add updated_by column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = table_name_param AND column_name = 'updated_by'
    ) THEN
        EXECUTE format('ALTER TABLE %I ADD COLUMN updated_by BIGINT', table_name_param);
        RAISE NOTICE 'Added updated_by to %', table_name_param;
    END IF;

    -- Add deleted_at column
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = table_name_param AND column_name = 'deleted_at'
    ) THEN
        EXECUTE format('ALTER TABLE %I ADD COLUMN deleted_at TIMESTAMP', table_name_param);
        RAISE NOTICE 'Added deleted_at to %', table_name_param;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- Apply audit columns to all tables with BaseEntity
-- =====================================================

-- Auth tables
SELECT add_audit_columns_if_missing('users');
SELECT add_audit_columns_if_missing('stores');
SELECT add_audit_columns_if_missing('roles');
SELECT add_audit_columns_if_missing('customers');
SELECT add_audit_columns_if_missing('user_store_access');

-- Inventory tables
SELECT add_audit_columns_if_missing('products');
SELECT add_audit_columns_if_missing('inventory');
SELECT add_audit_columns_if_missing('received_stocks');
SELECT add_audit_columns_if_missing('stock_transfers');
SELECT add_audit_columns_if_missing('warehouses');
SELECT add_audit_columns_if_missing('warehouse_locations');

-- POS tables
SELECT add_audit_columns_if_missing('shifts');
SELECT add_audit_columns_if_missing('shift_swaps');
SELECT add_audit_columns_if_missing('salesperson_logins');
SELECT add_audit_columns_if_missing('sales_orders');
SELECT add_audit_columns_if_missing('discounts');

-- Loyalty tables
SELECT add_audit_columns_if_missing('loyalty_programs');
SELECT add_audit_columns_if_missing('loyalty_transactions');
SELECT add_audit_columns_if_missing('customer_loyalty');

-- =====================================================
-- Backfill existing records with current timestamp
-- =====================================================
DO $$
DECLARE
    tables TEXT[] := ARRAY[
        'users', 'stores', 'roles', 'customers', 'user_store_access',
        'products', 'inventory', 'received_stocks', 'stock_transfers', 
        'warehouses', 'warehouse_locations',
        'shifts', 'shift_swaps', 'salesperson_logins', 'sales_orders', 'discounts',
        'loyalty_programs', 'loyalty_transactions', 'customer_loyalty'
    ];
    t TEXT;
BEGIN
    FOREACH t IN ARRAY tables
    LOOP
        -- Check if table exists before updating
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = t) THEN
            EXECUTE format('UPDATE %I SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL', t);
            EXECUTE format('UPDATE %I SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL', t);
        END IF;
    END LOOP;
END $$;

-- =====================================================
-- Clean up the helper function
-- =====================================================
DROP FUNCTION IF EXISTS add_audit_columns_if_missing(TEXT);

-- =====================================================
-- Add indexes for common queries
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_user_store_access_created ON user_store_access(created_at);
CREATE INDEX IF NOT EXISTS idx_warehouses_created ON warehouses(created_at);
CREATE INDEX IF NOT EXISTS idx_warehouse_locations_created ON warehouse_locations(created_at);
