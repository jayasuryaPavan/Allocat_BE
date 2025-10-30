# Store Management Implementation

## Overview
This document describes how store codes work in the Allocat ERP backend system, including store validation and creation.

## Store Code Format

### Alphanumeric Store Codes
Store codes are **alphanumeric** and can be in formats like:
- `STR001`
- `CRK2645`
- `NYC42`
- Any combination of letters and numbers

The store code is stored in the `stores.code` field and is unique.

## Store Validation

### When Creating Users
When creating a user, you must provide a valid store code (not a numeric ID). The system validates:

1. **Store Existence Check**: The store code must exist in the database
2. **Store Code Format**: Any alphanumeric combination is accepted
3. **Error Handling**: Clear error messages if store doesn't exist

### Example API Request
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "roleName": "SALES_STAFF",
  "storeCode": "STR001"
}
```

### Error Response if Store Doesn't Exist
```json
{
  "success": false,
  "message": "Store with code 'INVALID' does not exist. Please use a valid store code (e.g., STR001, CRK2645)."
}
```

## Superadmin Store Creation

### Endpoint
`POST /api/stores` - **SUPER_ADMIN only**

### Required Fields
- `code`: Alphanumeric store code (e.g., "STR001", "CRK2645")
- `name`: Store name
- `accessCode`: Access code for store operations

### Optional Fields
- `address`, `city`, `state`, `country`, `postalCode`
- `phone`, `email`, `taxId`
- `currency` (default: "USD")
- `timezone` (default: "UTC")

### Example Request
```json
{
  "code": "NYC42",
  "name": "New York Store",
  "accessCode": "secure123",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001",
  "phone": "+1-212-555-0123",
  "email": "nyc@example.com"
}
```

### Response
```json
{
  "success": true,
  "message": "Store created successfully",
  "data": {
    "id": 1,
    "code": "NYC42",
    "name": "New York Store",
    "isActive": true,
    ...
  }
}
```

## Store Service Methods

### Available Methods

1. **`existsByCode(String storeCode)`**: Check if store exists by code
2. **`getStoreByCode(String code)`**: Get store by code
3. **`getStoreById(Long id)`**: Get store by ID
4. **`createStore(Store store)`**: Create a new store (SUPER_ADMIN)
5. **`updateStore(...)`**: Update store details
6. **`deleteStore(...)`**: Soft delete a store

## Database Schema

### Stores Table
```sql
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,  -- Alphanumeric store code
    name VARCHAR(100) NOT NULL,
    access_code VARCHAR(50) NOT NULL,  -- Hashed access code
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(100),
    tax_id VARCHAR(50),
    currency VARCHAR(3) DEFAULT 'USD',
    timezone VARCHAR(50) DEFAULT 'UTC',
    settings JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Security Features

### Store Access Protection
- Store access codes are hashed using BCrypt
- Only SUPER_ADMIN can create new stores
- Access code required for store updates/deletes

### User Store Assignment
- Store codes are validated when assigning users to stores
- ADMIN users can only create users for their own store
- SUPER_ADMIN can assign any store to any user

## Migration Notes

If you need to add store codes to existing stores:

```sql
-- Example: Update existing stores with codes
UPDATE stores SET code = 'STR001' WHERE id = 1;
UPDATE stores SET code = 'CRK001' WHERE id = 2;
```

## API Endpoints

### Store Management (SUPER_ADMIN only)
- `POST /api/stores` - Create store
- `GET /api/stores` - Get all stores
- `GET /api/stores/{id}` - Get store by ID
- `GET /api/stores/code/{storeCode}` - Get store by code
- `PUT /api/stores/{id}` - Update store
- `DELETE /api/stores/{id}` - Delete store (soft delete)
- `POST /api/stores/validate-access` - Validate store access code

### User Creation with Store Code
- `POST /api/users` - Create user with `storeCode` field

## Examples

### Complete Workflow

1. **Superadmin creates a store:**
```bash
curl -X POST http://localhost:8080/api/stores \
  -H "Authorization: Bearer <superadmin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "STR001",
    "name": "Main Store",
    "accessCode": "pass123"
  }'
```

2. **Superadmin creates a user for that store:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <superadmin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "roleName": "SALES_STAFF",
    "storeCode": "STR001"
  }'
```

3. **Invalid store code error:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "roleName": "VIEWER",
    "storeCode": "INVALID"
  }'
```

Response:
```json
{
  "success": false,
  "message": "Store with code 'INVALID' does not exist. Please use a valid store code (e.g., STR001, CRK2645)."
}
```
