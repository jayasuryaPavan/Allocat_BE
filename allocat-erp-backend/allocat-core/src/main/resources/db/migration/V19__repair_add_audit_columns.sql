-- V19: Repair and add missing BaseEntity audit columns
-- This replaces the failed V18 migration with table existence checks

DO $$
DECLARE
    tables_to_update TEXT[] := ARRAY[
        'users', 'stores', 'roles', 'customers', 'user_store_access',
        'products', 'inventory', 'stock_transfers', 
        'warehouses', 'warehouse_locations',
        'shifts', 'shift_swaps', 'salesperson_logins', 'sales_orders', 'discounts',
        'loyalty_programs', 'loyalty_transactions', 'customer_loyalty'
    ];
    t TEXT;
BEGIN
    FOREACH t IN ARRAY tables_to_update
    LOOP
        -- Check if table exists
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = t) THEN
            -- Add created_at if missing
            IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = t AND column_name = 'created_at') THEN
                EXECUTE format('ALTER TABLE %I ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP', t);
                RAISE NOTICE 'Added created_at to %', t;
            END IF;
            
            -- Add created_by if missing
            IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = t AND column_name = 'created_by') THEN
                EXECUTE format('ALTER TABLE %I ADD COLUMN created_by BIGINT', t);
                RAISE NOTICE 'Added created_by to %', t;
            END IF;
            
            -- Add updated_at if missing
            IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = t AND column_name = 'updated_at') THEN
                EXECUTE format('ALTER TABLE %I ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP', t);
                RAISE NOTICE 'Added updated_at to %', t;
            END IF;
            
            -- Add updated_by if missing
            IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = t AND column_name = 'updated_by') THEN
                EXECUTE format('ALTER TABLE %I ADD COLUMN updated_by BIGINT', t);
                RAISE NOTICE 'Added updated_by to %', t;
            END IF;
            
            -- Add deleted_at if missing
            IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = t AND column_name = 'deleted_at') THEN
                EXECUTE format('ALTER TABLE %I ADD COLUMN deleted_at TIMESTAMP', t);
                RAISE NOTICE 'Added deleted_at to %', t;
            END IF;
            
            -- Backfill timestamps
            EXECUTE format('UPDATE %I SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL', t);
            EXECUTE format('UPDATE %I SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL', t);
        ELSE
            RAISE NOTICE 'Table % does not exist, skipping', t;
        END IF;
    END LOOP;
END $$;

-- Add indexes for common queries
CREATE INDEX IF NOT EXISTS idx_user_store_access_created ON user_store_access(created_at);
CREATE INDEX IF NOT EXISTS idx_warehouses_created ON warehouses(created_at);
CREATE INDEX IF NOT EXISTS idx_warehouse_locations_created ON warehouse_locations(created_at);
