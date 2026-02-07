-- Create aisles table
CREATE TABLE IF NOT EXISTS aisles (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    aisle_number VARCHAR(20) NOT NULL,
    aisle_name VARCHAR(100),
    product_type VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    UNIQUE(store_id, aisle_number)
);

CREATE INDEX IF NOT EXISTS idx_aisles_store ON aisles(store_id);
CREATE INDEX IF NOT EXISTS idx_aisles_product_type ON aisles(product_type);

-- Add new columns to inventory table
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS storage_quantity INTEGER DEFAULT 0;
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS aisle_quantity INTEGER DEFAULT 0;
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS aisle_id BIGINT REFERENCES aisles(id);

-- Migrate existing data: all current_quantity goes to storage_quantity initially
-- Only update if storage_quantity is 0 (to avoid overwriting if run multiple times)
UPDATE inventory 
SET storage_quantity = current_quantity 
WHERE storage_quantity = 0 AND current_quantity > 0;

-- Create computed column function for inventory
CREATE OR REPLACE FUNCTION calculate_total_quantity()
RETURNS TRIGGER AS $$
BEGIN
    -- Calculate current_quantity based on storage and aisle quantities
    NEW.current_quantity := COALESCE(NEW.storage_quantity, 0) + COALESCE(NEW.aisle_quantity, 0);
    -- Update available_quantity
    NEW.available_quantity := NEW.current_quantity - COALESCE(NEW.reserved_quantity, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop existing trigger if it exists to replace with new logic
DROP TRIGGER IF EXISTS trigger_update_available_quantity ON inventory;
DROP TRIGGER IF EXISTS trigger_calculate_total_quantity ON inventory;

-- Create new trigger
CREATE TRIGGER trigger_calculate_total_quantity
    BEFORE INSERT OR UPDATE ON inventory
    FOR EACH ROW
    EXECUTE FUNCTION calculate_total_quantity();

CREATE INDEX IF NOT EXISTS idx_inventory_aisle ON inventory(aisle_id);

-- Create inventory_movements table
CREATE TABLE IF NOT EXISTS inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL REFERENCES inventory(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    from_location VARCHAR(50) NOT NULL,
    to_location VARCHAR(50) NOT NULL,
    from_aisle_id BIGINT REFERENCES aisles(id),
    to_aisle_id BIGINT REFERENCES aisles(id),
    quantity INTEGER NOT NULL,
    moved_by BIGINT NOT NULL REFERENCES users(id),
    moved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    CHECK (quantity > 0)
    -- CHECK (from_location != to_location OR from_aisle_id != to_aisle_id) -- constraint can be tricky during validation
);

CREATE INDEX IF NOT EXISTS idx_movements_inventory ON inventory_movements(inventory_id);
CREATE INDEX IF NOT EXISTS idx_movements_product ON inventory_movements(product_id);
CREATE INDEX IF NOT EXISTS idx_movements_moved_at ON inventory_movements(moved_at);

-- Create inventory_write_offs table
CREATE TABLE IF NOT EXISTS inventory_write_offs (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL REFERENCES inventory(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    location VARCHAR(50) NOT NULL,
    aisle_id BIGINT REFERENCES aisles(id),
    quantity INTEGER NOT NULL,
    reason VARCHAR(50) NOT NULL,
    description TEXT,
    written_off_by BIGINT NOT NULL REFERENCES users(id),
    written_off_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_writeoffs_inventory ON inventory_write_offs(inventory_id);
CREATE INDEX IF NOT EXISTS idx_writeoffs_product ON inventory_write_offs(product_id);
CREATE INDEX IF NOT EXISTS idx_writeoffs_reason ON inventory_write_offs(reason);

-- Update timestamp trigger for aisles
CREATE TRIGGER trigger_aisles_updated_at
    BEFORE UPDATE ON aisles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
