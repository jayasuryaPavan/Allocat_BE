-- Run this SQL in PostgreSQL to fix Flyway checksum mismatches
-- Connect to allocat_db database first

-- Update V5 checksum
UPDATE flyway_schema_history 
SET checksum = -327006705 
WHERE version = '5';

-- Update V6 checksum
UPDATE flyway_schema_history 
SET checksum = -851231376 
WHERE version = '6';

-- Verify the updates
SELECT version, description, checksum, installed_on, success 
FROM flyway_schema_history 
ORDER BY installed_rank;

