-- Seed default roles with appropriate permissions for ERP system

-- First, add missing audit columns to roles table
ALTER TABLE roles ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS updated_by BIGINT;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Insert default roles
INSERT INTO roles (name, description, permissions, created_at, updated_at) VALUES
('SUPER_ADMIN', 'Full system access - can manage everything including users and system settings', 
 ARRAY['users:create', 'users:read', 'users:update', 'users:delete', 'users:manage_roles',
       'products:create', 'products:read', 'products:update', 'products:delete',
       'inventory:create', 'inventory:read', 'inventory:update', 'inventory:delete', 'inventory:adjust',
       'orders:create', 'orders:read', 'orders:update', 'orders:delete', 'orders:approve',
       'reports:view', 'reports:export',
       'settings:read', 'settings:update',
       'stores:create', 'stores:read', 'stores:update', 'stores:delete'],
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('ADMIN', 'Administrator - can manage operational data and users',
 ARRAY['users:create', 'users:read', 'users:update',
       'products:create', 'products:read', 'products:update', 'products:delete',
       'inventory:create', 'inventory:read', 'inventory:update', 'inventory:adjust',
       'orders:create', 'orders:read', 'orders:update', 'orders:approve',
       'reports:view', 'reports:export',
       'stores:read', 'stores:update'],
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('STORE_MANAGER', 'Store Manager - can manage store operations and inventory',
 ARRAY['products:read', 'products:update',
       'inventory:create', 'inventory:read', 'inventory:update', 'inventory:adjust',
       'orders:create', 'orders:read', 'orders:update',
       'reports:view',
       'stores:read'],
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('INVENTORY_MANAGER', 'Inventory Manager - can manage inventory, products, and stock',
 ARRAY['products:create', 'products:read', 'products:update',
       'inventory:create', 'inventory:read', 'inventory:update', 'inventory:adjust',
       'orders:read',
       'reports:view'],
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('WAREHOUSE_STAFF', 'Warehouse Staff - can receive stock, update inventory',
 ARRAY['products:read',
       'inventory:read', 'inventory:update',
       'orders:read'],
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('SALES_STAFF', 'Sales Staff - can create orders and view inventory',
 ARRAY['products:read',
       'inventory:read',
       'orders:create', 'orders:read', 'orders:update'],
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('ACCOUNTANT', 'Accountant - can view reports and financial data',
 ARRAY['products:read',
       'inventory:read',
       'orders:read',
       'reports:view', 'reports:export'],
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('VIEWER', 'Read-only access to basic information',
 ARRAY['products:read',
       'inventory:read',
       'orders:read'],
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Update the first user (if exists) to be SUPER_ADMIN
UPDATE users 
SET role_id = (SELECT id FROM roles WHERE name = 'SUPER_ADMIN')
WHERE id = 1;

