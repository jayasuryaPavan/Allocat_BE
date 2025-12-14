-- Phase 2: Multi-Store / Multi-Warehouse Support
-- This migration adds warehouse management and enhances multi-store capabilities

-- =====================================================
-- 1. WAREHOUSES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS warehouses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    store_id BIGINT REFERENCES stores(id) ON DELETE CASCADE,
    type VARCHAR(20) DEFAULT 'WAREHOUSE' CHECK (type IN ('WAREHOUSE', 'STOREROOM', 'DISTRIBUTION_CENTER')),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(100),
    manager_id BIGINT REFERENCES users(id),
    is_active BOOLEAN DEFAULT true,
    settings JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_warehouse_code UNIQUE (code)
);

-- =====================================================
-- 2. WAREHOUSE LOCATIONS (Bins, Shelves, etc.)
-- =====================================================
CREATE TABLE IF NOT EXISTS warehouse_locations (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    location_type VARCHAR(20) DEFAULT 'BIN' CHECK (location_type IN ('BIN', 'SHELF', 'ZONE', 'AREA', 'ROOM')),
    parent_location_id BIGINT REFERENCES warehouse_locations(id),
    is_active BOOLEAN DEFAULT true,
    capacity_limit INTEGER,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_warehouse_location_code UNIQUE (warehouse_id, code)
);

-- =====================================================
-- 3. UPDATE INVENTORY TABLE TO INCLUDE STORE_ID AND WAREHOUSE_ID
-- =====================================================
DO $$
BEGIN
    -- Add store_id if missing (should exist but checking for safety)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'inventory' AND column_name = 'store_id'
    ) THEN
        ALTER TABLE inventory ADD COLUMN store_id BIGINT REFERENCES stores(id);
        -- Backfill store_id if possible (from existing data or default store)
        UPDATE inventory i
        SET store_id = (SELECT id FROM stores LIMIT 1)
        WHERE store_id IS NULL;
        -- Make it NOT NULL after backfill
        ALTER TABLE inventory ALTER COLUMN store_id SET NOT NULL;
    END IF;

    -- Add warehouse_id
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'inventory' AND column_name = 'warehouse_id'
    ) THEN
        ALTER TABLE inventory ADD COLUMN warehouse_id BIGINT REFERENCES warehouses(id);
    END IF;

    -- Add warehouse_location_id
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'inventory' AND column_name = 'warehouse_location_id'
    ) THEN
        ALTER TABLE inventory ADD COLUMN warehouse_location_id BIGINT REFERENCES warehouse_locations(id);
    END IF;

    -- Update unique constraint to include store_id and warehouse_id
    -- Drop old constraint if exists
    IF EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_inventory_product_store'
    ) THEN
        ALTER TABLE inventory DROP CONSTRAINT uk_inventory_product_store;
    END IF;

    -- Add new constraint with store and warehouse
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_inventory_product_store_warehouse'
    ) THEN
        ALTER TABLE inventory ADD CONSTRAINT uk_inventory_product_store_warehouse 
            UNIQUE (product_id, store_id, warehouse_id, warehouse_location_id);
    END IF;
END $$;

-- =====================================================
-- 4. ENHANCE STOCK TRANSFERS TABLE
-- =====================================================
DO $$
BEGIN
    -- Add warehouse fields to stock_transfers if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'stock_transfers' AND column_name = 'from_warehouse_id'
    ) THEN
        ALTER TABLE stock_transfers 
            ADD COLUMN from_warehouse_id BIGINT REFERENCES warehouses(id),
            ADD COLUMN to_warehouse_id BIGINT REFERENCES warehouses(id),
            ADD COLUMN from_location_id BIGINT REFERENCES warehouse_locations(id),
            ADD COLUMN to_location_id BIGINT REFERENCES warehouse_locations(id),
            ADD COLUMN priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
            ADD COLUMN estimated_delivery_date TIMESTAMP,
            ADD COLUMN actual_delivery_date TIMESTAMP,
            ADD COLUMN shipping_method VARCHAR(50),
            ADD COLUMN tracking_number VARCHAR(100);
    END IF;

    -- Add transfer type
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'stock_transfers' AND column_name = 'transfer_type'
    ) THEN
        ALTER TABLE stock_transfers 
            ADD COLUMN transfer_type VARCHAR(20) DEFAULT 'STORE_TO_STORE' 
            CHECK (transfer_type IN ('STORE_TO_STORE', 'WAREHOUSE_TO_STORE', 'STORE_TO_WAREHOUSE', 'WAREHOUSE_TO_WAREHOUSE'));
    END IF;
END $$;

-- =====================================================
-- 5. USER STORE ACCESS (Role-based access per branch)
-- =====================================================
CREATE TABLE IF NOT EXISTS user_store_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    warehouse_id BIGINT REFERENCES warehouses(id) ON DELETE CASCADE,
    access_level VARCHAR(20) DEFAULT 'VIEW' CHECK (access_level IN ('VIEW', 'OPERATE', 'MANAGE', 'ADMIN')),
    is_primary BOOLEAN DEFAULT false,
    granted_by BIGINT REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    notes TEXT,
    CONSTRAINT uk_user_store_access UNIQUE (user_id, store_id, warehouse_id)
);

-- =====================================================
-- 6. INDEXES FOR PERFORMANCE
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_warehouses_store_id ON warehouses(store_id);
CREATE INDEX IF NOT EXISTS idx_warehouses_code ON warehouses(code);
CREATE INDEX IF NOT EXISTS idx_warehouses_active ON warehouses(is_active);

CREATE INDEX IF NOT EXISTS idx_warehouse_locations_warehouse_id ON warehouse_locations(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_warehouse_locations_parent ON warehouse_locations(parent_location_id);
CREATE INDEX IF NOT EXISTS idx_warehouse_locations_code ON warehouse_locations(warehouse_id, code);

CREATE INDEX IF NOT EXISTS idx_inventory_store_id ON inventory(store_id);
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inventory_store_warehouse ON inventory(store_id, warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inventory_location ON inventory(warehouse_location_id);

CREATE INDEX IF NOT EXISTS idx_stock_transfers_from_store ON stock_transfers(from_store_id);
CREATE INDEX IF NOT EXISTS idx_stock_transfers_to_store ON stock_transfers(to_store_id);
CREATE INDEX IF NOT EXISTS idx_stock_transfers_from_warehouse ON stock_transfers(from_warehouse_id);
CREATE INDEX IF NOT EXISTS idx_stock_transfers_to_warehouse ON stock_transfers(to_warehouse_id);
CREATE INDEX IF NOT EXISTS idx_stock_transfers_status ON stock_transfers(status);
CREATE INDEX IF NOT EXISTS idx_stock_transfers_type ON stock_transfers(transfer_type);
CREATE INDEX IF NOT EXISTS idx_stock_transfers_date ON stock_transfers(transfer_date);

CREATE INDEX IF NOT EXISTS idx_user_store_access_user ON user_store_access(user_id);
CREATE INDEX IF NOT EXISTS idx_user_store_access_store ON user_store_access(store_id);
CREATE INDEX IF NOT EXISTS idx_user_store_access_warehouse ON user_store_access(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_user_store_access_active ON user_store_access(is_active);

-- =====================================================
-- 7. TRIGGERS
-- =====================================================
CREATE TRIGGER trigger_warehouses_updated_at
    BEFORE UPDATE ON warehouses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_warehouse_locations_updated_at
    BEFORE UPDATE ON warehouse_locations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_stock_transfers_updated_at
    BEFORE UPDATE ON stock_transfers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 8. VIEWS FOR CONSOLIDATED REPORTING
-- =====================================================

-- Consolidated inventory view across all stores
CREATE OR REPLACE VIEW v_consolidated_inventory AS
SELECT 
    p.id AS product_id,
    p.product_code,
    p.name AS product_name,
    p.sku,
    p.barcode,
    SUM(i.current_quantity) AS total_quantity,
    SUM(i.reserved_quantity) AS total_reserved,
    SUM(i.available_quantity) AS total_available,
    COUNT(DISTINCT i.store_id) AS store_count,
    COUNT(DISTINCT i.warehouse_id) AS warehouse_count,
    MIN(i.available_quantity) AS min_available,
    MAX(i.available_quantity) AS max_available,
    AVG(i.unit_cost) AS avg_unit_cost,
    SUM(i.total_value) AS total_value
FROM products p
LEFT JOIN inventory i ON p.id = i.product_id
WHERE p.is_active = true
GROUP BY p.id, p.product_code, p.name, p.sku, p.barcode;

-- Store-level inventory summary
CREATE OR REPLACE VIEW v_store_inventory_summary AS
SELECT 
    s.id AS store_id,
    s.code AS store_code,
    s.name AS store_name,
    COUNT(DISTINCT i.product_id) AS product_count,
    SUM(i.current_quantity) AS total_quantity,
    SUM(i.available_quantity) AS total_available,
    SUM(i.total_value) AS total_inventory_value,
    COUNT(DISTINCT i.warehouse_id) AS warehouse_count
FROM stores s
LEFT JOIN inventory i ON s.id = i.store_id
WHERE s.is_active = true
GROUP BY s.id, s.code, s.name;

-- Warehouse inventory summary
CREATE OR REPLACE VIEW v_warehouse_inventory_summary AS
SELECT 
    w.id AS warehouse_id,
    w.code AS warehouse_code,
    w.name AS warehouse_name,
    w.store_id,
    s.name AS store_name,
    COUNT(DISTINCT i.product_id) AS product_count,
    SUM(i.current_quantity) AS total_quantity,
    SUM(i.available_quantity) AS total_available,
    SUM(i.total_value) AS total_inventory_value
FROM warehouses w
LEFT JOIN stores s ON w.store_id = s.id
LEFT JOIN inventory i ON w.id = i.warehouse_id
WHERE w.is_active = true
GROUP BY w.id, w.code, w.name, w.store_id, s.name;

-- =====================================================
-- 9. COMMENTS
-- =====================================================
COMMENT ON TABLE warehouses IS 'Physical warehouses and storage locations';
COMMENT ON TABLE warehouse_locations IS 'Specific locations within warehouses (bins, shelves, zones)';
COMMENT ON TABLE user_store_access IS 'Role-based access control for users per store/warehouse';
COMMENT ON VIEW v_consolidated_inventory IS 'Consolidated inventory view across all stores and warehouses';
COMMENT ON VIEW v_store_inventory_summary IS 'Inventory summary per store';
COMMENT ON VIEW v_warehouse_inventory_summary IS 'Inventory summary per warehouse';
