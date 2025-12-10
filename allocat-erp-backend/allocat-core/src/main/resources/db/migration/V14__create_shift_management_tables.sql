-- Create shift management tables

-- Shifts table
CREATE TABLE IF NOT EXISTS shifts (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    shift_date DATE NOT NULL,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    expected_start_time TIMESTAMP,
    expected_end_time TIMESTAMP,
    starting_cash_amount NUMERIC(12,2) DEFAULT 0,
    ending_cash_amount NUMERIC(12,2),
    expected_cash_amount NUMERIC(12,2),
    cash_difference NUMERIC(12,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    ended_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shifts_store_date ON shifts(store_id, shift_date);
CREATE INDEX IF NOT EXISTS idx_shifts_status ON shifts(status);

-- Shift swaps table
CREATE TABLE IF NOT EXISTS shift_swaps (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    original_shift_id BIGINT NOT NULL REFERENCES shifts(id),
    requested_by_user_id BIGINT NOT NULL REFERENCES users(id),
    requested_to_user_id BIGINT NOT NULL REFERENCES users(id),
    original_shift_date DATE NOT NULL,
    swap_shift_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason TEXT,
    manager_notes TEXT,
    approved_by BIGINT,
    rejected_by BIGINT,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shift_swaps_store ON shift_swaps(store_id);
CREATE INDEX IF NOT EXISTS idx_shift_swaps_status ON shift_swaps(status);

-- Sales person logins table
CREATE TABLE IF NOT EXISTS sales_person_logins (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    shift_id BIGINT REFERENCES shifts(id),
    login_time TIMESTAMP NOT NULL,
    logout_time TIMESTAMP,
    login_type VARCHAR(20) NOT NULL DEFAULT 'SHIFT_START',
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sales_person_logins_user ON sales_person_logins(user_id);
CREATE INDEX IF NOT EXISTS idx_sales_person_logins_store ON sales_person_logins(store_id);
CREATE INDEX IF NOT EXISTS idx_sales_person_logins_shift ON sales_person_logins(shift_id);
