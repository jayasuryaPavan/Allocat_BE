-- POS Kiosk Mode: Associate Credentials Table
-- This table stores associate numbers and passcodes for POS shift sign-in/sign-out

CREATE TABLE IF NOT EXISTS associate_credentials (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    associate_number VARCHAR(20) NOT NULL UNIQUE,
    passcode_hash VARCHAR(255) NOT NULL,
    store_id BIGINT REFERENCES stores(id),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_associate_credentials_user_id ON associate_credentials(user_id);
CREATE INDEX IF NOT EXISTS idx_associate_credentials_store_id ON associate_credentials(store_id);
CREATE INDEX IF NOT EXISTS idx_associate_credentials_associate_number ON associate_credentials(associate_number);

-- Sample data for testing (passcode is hashed - use BCrypt with value '1234')
-- You can generate BCrypt hashes using online tools or Spring Security's BCryptPasswordEncoder

-- Example insert (replace with actual hashed passcode):
-- INSERT INTO associate_credentials (user_id, associate_number, passcode_hash, store_id, is_active)
-- VALUES (1, '1001', '$2a$10$...', 1, true);

COMMENT ON TABLE associate_credentials IS 'Stores associate credentials for POS Kiosk mode shift sign-in/sign-out';
COMMENT ON COLUMN associate_credentials.associate_number IS '4-digit associate number for quick identification';
COMMENT ON COLUMN associate_credentials.passcode_hash IS 'BCrypt hashed 4-digit PIN for authentication';
