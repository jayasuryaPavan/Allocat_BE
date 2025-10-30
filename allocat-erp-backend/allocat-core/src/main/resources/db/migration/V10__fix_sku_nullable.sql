-- Make sku column nullable to fix constraint violation
ALTER TABLE products ALTER COLUMN sku DROP NOT NULL;
