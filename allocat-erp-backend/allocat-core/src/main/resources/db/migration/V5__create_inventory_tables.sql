-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    unit_price DECIMAL(10,2),
    unit_of_measure VARCHAR(50),
    minimum_stock_level INTEGER,
    maximum_stock_level INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    supplier_name VARCHAR(255),
    supplier_contact VARCHAR(255),
    barcode VARCHAR(100) UNIQUE,
    sku VARCHAR(100) UNIQUE,
    brand VARCHAR(100),
    model VARCHAR(100),
    color VARCHAR(50),
    size VARCHAR(50),
    weight DECIMAL(10,3),
    dimensions VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ensure compatibility when products table already exists from earlier migrations
-- Add product_code column if missing and backfill from sku
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'products' AND column_name = 'product_code'
    ) THEN
        ALTER TABLE products ADD COLUMN product_code VARCHAR(50);
        -- Prefer existing sku value if present
        UPDATE products SET product_code = sku WHERE product_code IS NULL AND sku IS NOT NULL;
        -- Fallback: generate a code based on id
        UPDATE products SET product_code = 'P' || id WHERE product_code IS NULL;
    END IF;

    -- Add denormalized category column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'products' AND column_name = 'category'
    ) THEN
        ALTER TABLE products ADD COLUMN category VARCHAR(100);
        -- Backfill from categories.name if categories table and category_id exist
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'products' AND column_name = 'category_id'
        ) THEN
            BEGIN
                UPDATE products p
                SET category = c.name
                FROM categories c
                WHERE p.category_id = c.id AND p.category IS NULL;
            EXCEPTION WHEN undefined_table THEN
                -- categories table not present; skip backfill
                NULL;
            END;
        END IF;
    END IF;

    -- Add other optional columns used by the new model if missing
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'unit_of_measure') THEN
        ALTER TABLE products ADD COLUMN unit_of_measure VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'minimum_stock_level') THEN
        ALTER TABLE products ADD COLUMN minimum_stock_level INTEGER;
        -- Backfill from legacy min_stock_level if present
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'min_stock_level') THEN
            UPDATE products SET minimum_stock_level = min_stock_level WHERE minimum_stock_level IS NULL;
        END IF;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'maximum_stock_level') THEN
        ALTER TABLE products ADD COLUMN maximum_stock_level INTEGER;
        -- Backfill from legacy max_stock_level if present
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'max_stock_level') THEN
            UPDATE products SET maximum_stock_level = max_stock_level WHERE maximum_stock_level IS NULL;
        END IF;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'supplier_name') THEN
        ALTER TABLE products ADD COLUMN supplier_name VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'supplier_contact') THEN
        ALTER TABLE products ADD COLUMN supplier_contact VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'brand') THEN
        ALTER TABLE products ADD COLUMN brand VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'model') THEN
        ALTER TABLE products ADD COLUMN model VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'color') THEN
        ALTER TABLE products ADD COLUMN color VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'size') THEN
        ALTER TABLE products ADD COLUMN size VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'weight') THEN
        ALTER TABLE products ADD COLUMN weight DECIMAL(10,3);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'dimensions') THEN
        ALTER TABLE products ADD COLUMN dimensions VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = 'notes') THEN
        ALTER TABLE products ADD COLUMN notes TEXT;
    END IF;
END $$;

-- Create received_stock table
CREATE TABLE IF NOT EXISTS received_stock (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    expected_quantity INTEGER NOT NULL,
    received_quantity INTEGER DEFAULT 0,
    verified_quantity INTEGER DEFAULT 0,
    unit_price DECIMAL(10,2),
    total_value DECIMAL(12,2),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'VERIFIED', 'REJECTED', 'PARTIAL', 'DISCREPANCY')),
    batch_number VARCHAR(100),
    supplier_name VARCHAR(255),
    supplier_invoice_number VARCHAR(100),
    delivery_date TIMESTAMP,
    expected_delivery_date TIMESTAMP,
    received_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_date TIMESTAMP,
    received_by VARCHAR(100),
    verified_by VARCHAR(100),
    notes TEXT,
    quality_issues TEXT,
    damage_quantity INTEGER DEFAULT 0,
    shortage_quantity INTEGER DEFAULT 0,
    excess_quantity INTEGER DEFAULT 0,
    csv_upload_id VARCHAR(100),
    row_number INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    current_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    unit_cost DECIMAL(10,2),
    total_value DECIMAL(12,2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_by VARCHAR(100),
    location VARCHAR(100),
    warehouse VARCHAR(100),
    shelf VARCHAR(50),
    bin VARCHAR(50),
    batch_number VARCHAR(100),
    expiry_date TIMESTAMP,
    supplier_name VARCHAR(255),
    purchase_order_number VARCHAR(100),
    received_stock_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ensure compatibility when inventory table already exists from earlier migrations
DO $$
BEGIN
    -- current_quantity (backfill from legacy quantity)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'current_quantity') THEN
        ALTER TABLE inventory ADD COLUMN current_quantity INTEGER NOT NULL DEFAULT 0;
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'quantity') THEN
            UPDATE inventory SET current_quantity = quantity;
        END IF;
    END IF;

    -- reserved_quantity
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'reserved_quantity') THEN
        ALTER TABLE inventory ADD COLUMN reserved_quantity INTEGER DEFAULT 0;
    END IF;

    -- available_quantity
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'available_quantity') THEN
        ALTER TABLE inventory ADD COLUMN available_quantity INTEGER NOT NULL DEFAULT 0;
        UPDATE inventory SET available_quantity = COALESCE(current_quantity,0) - COALESCE(reserved_quantity,0);
    END IF;

    -- unit_cost, total_value
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'unit_cost') THEN
        ALTER TABLE inventory ADD COLUMN unit_cost DECIMAL(10,2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'total_value') THEN
        ALTER TABLE inventory ADD COLUMN total_value DECIMAL(12,2);
    END IF;

    -- last_updated, last_updated_by
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'last_updated') THEN
        ALTER TABLE inventory ADD COLUMN last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'last_updated_by') THEN
        ALTER TABLE inventory ADD COLUMN last_updated_by VARCHAR(100);
    END IF;

    -- location fields
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'location') THEN
        ALTER TABLE inventory ADD COLUMN location VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'warehouse') THEN
        ALTER TABLE inventory ADD COLUMN warehouse VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'shelf') THEN
        ALTER TABLE inventory ADD COLUMN shelf VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'bin') THEN
        ALTER TABLE inventory ADD COLUMN bin VARCHAR(50);
    END IF;

    -- batch/expiry
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'batch_number') THEN
        ALTER TABLE inventory ADD COLUMN batch_number VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'expiry_date') THEN
        ALTER TABLE inventory ADD COLUMN expiry_date TIMESTAMP;
    END IF;

    -- supplier/purchase references
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'supplier_name') THEN
        ALTER TABLE inventory ADD COLUMN supplier_name VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'purchase_order_number') THEN
        ALTER TABLE inventory ADD COLUMN purchase_order_number VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'received_stock_id') THEN
        ALTER TABLE inventory ADD COLUMN received_stock_id BIGINT;
    END IF;

    -- notes, created_at, updated_at
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'notes') THEN
        ALTER TABLE inventory ADD COLUMN notes TEXT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'created_at') THEN
        ALTER TABLE inventory ADD COLUMN created_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'created_by') THEN
        ALTER TABLE inventory ADD COLUMN created_by BIGINT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'updated_at') THEN
        ALTER TABLE inventory ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'updated_by') THEN
        ALTER TABLE inventory ADD COLUMN updated_by BIGINT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'deleted_at') THEN
        ALTER TABLE inventory ADD COLUMN deleted_at TIMESTAMP;
    END IF;
END $$;

-- Create indexes for better performance
CREATE UNIQUE INDEX IF NOT EXISTS idx_products_product_code ON products(product_code);
CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(barcode);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_supplier ON products(supplier_name);
CREATE INDEX IF NOT EXISTS idx_products_active ON products(is_active);

CREATE INDEX IF NOT EXISTS idx_received_stock_product_id ON received_stock(product_id);
CREATE INDEX IF NOT EXISTS idx_received_stock_status ON received_stock(status);
CREATE INDEX IF NOT EXISTS idx_received_stock_csv_upload ON received_stock(csv_upload_id);
CREATE INDEX IF NOT EXISTS idx_received_stock_batch ON received_stock(batch_number);
CREATE INDEX IF NOT EXISTS idx_received_stock_supplier ON received_stock(supplier_name);
CREATE INDEX IF NOT EXISTS idx_received_stock_received_date ON received_stock(received_date);

-- Create indexes conditionally when columns exist
DO $$
BEGIN
    PERFORM 1;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'product_id') THEN
        CREATE INDEX IF NOT EXISTS idx_inventory_product_id ON inventory(product_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'location') THEN
        CREATE INDEX IF NOT EXISTS idx_inventory_location ON inventory(location);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'warehouse') THEN
        CREATE INDEX IF NOT EXISTS idx_inventory_warehouse ON inventory(warehouse);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'batch_number') THEN
        CREATE INDEX IF NOT EXISTS idx_inventory_batch ON inventory(batch_number);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'supplier_name') THEN
        CREATE INDEX IF NOT EXISTS idx_inventory_supplier ON inventory(supplier_name);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'available_quantity') THEN
        CREATE INDEX IF NOT EXISTS idx_inventory_available_quantity ON inventory(available_quantity);
    END IF;
END $$;

-- Create trigger to automatically update available_quantity
CREATE OR REPLACE FUNCTION update_available_quantity()
RETURNS TRIGGER AS $$
BEGIN
    NEW.available_quantity = NEW.current_quantity - NEW.reserved_quantity;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_available_quantity
    BEFORE INSERT OR UPDATE ON inventory
    FOR EACH ROW
    EXECUTE FUNCTION update_available_quantity();

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_received_stock_updated_at
    BEFORE UPDATE ON received_stock
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_inventory_updated_at
    BEFORE UPDATE ON inventory
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

