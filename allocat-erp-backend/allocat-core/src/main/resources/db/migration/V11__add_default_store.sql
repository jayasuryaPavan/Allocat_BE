-- Insert a default store if none exists
INSERT INTO stores (code, name, access_code, address, city, state, country, currency, timezone, is_active, created_at, updated_at)
SELECT 'MAIN', 'Main Store', 'DEFAULT123', 'Main Office', 'Default City', 'Default State', 'USA', 'USD', 'UTC', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM stores LIMIT 1);
