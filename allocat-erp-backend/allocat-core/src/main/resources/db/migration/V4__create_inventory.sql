CREATE TYPE stock_movement_type AS ENUM (
    'PURCHASE', 'SALE', 'ADJUSTMENT', 'TRANSFER_IN', 'TRANSFER_OUT', 
    'RETURN', 'DAMAGE', 'INITIAL'
);

CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT DEFAULT 0,
    min_stock_level INT DEFAULT 0,
    max_stock_level INT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_inventory_product_store UNIQUE (product_id, store_id),
    CONSTRAINT chk_quantity_positive CHECK (quantity >= 0)
);

CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    movement_type stock_movement_type NOT NULL,
    quantity INT NOT NULL,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    notes TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE stock_transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_no VARCHAR(50) NOT NULL UNIQUE,
    from_store_id BIGINT NOT NULL REFERENCES stores(id),
    to_store_id BIGINT NOT NULL REFERENCES stores(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    requested_by BIGINT,
    approved_by BIGINT,
    received_by BIGINT,
    transfer_date TIMESTAMP,
    received_date TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE stock_transfer_items (
    id BIGSERIAL PRIMARY KEY,
    transfer_id BIGINT NOT NULL REFERENCES stock_transfers(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    received_quantity INT DEFAULT 0,
    damaged_quantity INT DEFAULT 0
);

CREATE TABLE stock_adjustments (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    adjustment_type VARCHAR(20),
    quantity INT NOT NULL,
    reason VARCHAR(100),
    notes TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_product_store ON inventory(product_id, store_id);
CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at DESC);
CREATE INDEX idx_stock_transfers_status ON stock_transfers(status);

