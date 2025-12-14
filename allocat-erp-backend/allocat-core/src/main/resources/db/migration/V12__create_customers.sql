CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    invoice_number VARCHAR(100),
    tax_id VARCHAR(50),
    company_name VARCHAR(200),
    contact_person VARCHAR(100),
    notes TEXT,
    is_active BOOLEAN DEFAULT true,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_customer_code_store UNIQUE (customer_code, store_id)
);

CREATE INDEX idx_customers_store_id ON customers(store_id);
CREATE INDEX idx_customers_customer_code ON customers(customer_code);
CREATE INDEX idx_customers_invoice_number ON customers(invoice_number);
CREATE INDEX idx_customers_is_active ON customers(is_active);
CREATE INDEX idx_customers_name ON customers(name);

