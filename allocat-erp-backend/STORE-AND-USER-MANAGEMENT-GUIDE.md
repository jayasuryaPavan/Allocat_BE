# Store and User Management Guide

## Overview
This document describes the Store Management and enhanced User Management features for the Allocat ERP system.

---

## Table of Contents
1. [Store Management](#store-management)
2. [User Management](#user-management)
3. [Role-Based Access Control](#role-based-access-control)
4. [API Endpoints](#api-endpoints)
5. [Usage Examples](#usage-examples)

---

## Store Management

### Concept
- **Stores** are individual locations/branches in the ERP system
- Each store has a unique **code** and **access code**
- Only **SUPER_ADMIN** can manage stores
- Store access code is required for any store modifications (security feature)

### Store Entity
```java
{
  "id": 1,
  "code": "ST001",              // Unique store identifier
  "name": "Main Street Store",   // Display name
  "accessCode": "***",           // Hidden, required for modifications
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001",
  "phone": "+1234567890",
  "email": "store@example.com",
  "taxId": "TAX-123456",
  "currency": "USD",
  "timezone": "America/New_York",
  "isActive": true,
  "createdAt": "2025-10-23T10:00:00",
  "updatedAt": "2025-10-23T10:00:00"
}
```

### Key Features
1. **Access Code Protection**: All store modifications require the correct access code
2. **Secure Storage**: Access codes are hashed using BCrypt (same as passwords)
3. **Soft Delete**: Stores are deactivated rather than permanently deleted
4. **SUPER_ADMIN Only**: Only SUPER_ADMIN role can access store management

---

## User Management

### Enhanced Features

#### 1. Store-Specific Admin Assignment
- **SUPER_ADMIN** can assign **ADMIN** role to users
- **ADMIN** role must be tied to a specific store
- **ADMIN** users can only manage users within their assigned store

#### 2. Role Assignment Restrictions

| Current User Role | Can Create Roles | Store Restriction |
|-------------------|------------------|-------------------|
| SUPER_ADMIN | All except SUPER_ADMIN | Can assign any store |
| ADMIN | All except SUPER_ADMIN, ADMIN | Only their assigned store |
| Other Roles | Cannot create users | N/A |

#### 3. User Visibility

| User Role | Can View Users |
|-----------|----------------|
| SUPER_ADMIN | All users system-wide |
| ADMIN | Only users in their assigned store |
| Other Roles | No access to user list |

---

## Role-Based Access Control

### Role Hierarchy
```
SUPER_ADMIN (System-wide)
    └── ADMIN (Store-specific)
        └── STORE_MANAGER (Store-specific)
            └── SALES_STAFF (Store-specific)
                └── INVENTORY_STAFF (Store-specific)
                    └── VIEWER (Read-only)
```

### Permission Rules

**SUPER_ADMIN**:
- Full system access
- Can create/manage stores
- Can create ADMIN users (store-specific)
- Can create other role users
- Can view all users
- Not tied to any store

**ADMIN**:
- Manage users in their assigned store only
- Cannot create other ADMIN users
- Cannot create SUPER_ADMIN users
- Can only view users in their store
- Must be tied to a store

**Other Roles**:
- Cannot create users
- Cannot access user management
- Store-specific operations only

---

## API Endpoints

### Store Management (SUPER_ADMIN Only)

#### Create Store
```http
POST /api/stores
Authorization: Bearer <super_admin_token>
Content-Type: application/json

{
  "code": "ST001",
  "name": "Main Street Store",
  "accessCode": "SecureAccessCode123!",
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001",
  "phone": "+1234567890",
  "email": "store@example.com",
  "taxId": "TAX-123456",
  "currency": "USD",
  "timezone": "America/New_York"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Store created successfully",
  "data": {
    "id": 1,
    "code": "ST001",
    "name": "Main Street Store",
    ...
  }
}
```

#### Get All Stores
```http
GET /api/stores
Authorization: Bearer <super_admin_token>
```

**Optional Query Parameters:**
- `?active=true` - Filter only active stores

#### Get Store by ID
```http
GET /api/stores/{storeId}
Authorization: Bearer <super_admin_token>
```

#### Get Store by Code
```http
GET /api/stores/code/{storeCode}
Authorization: Bearer <super_admin_token>
```

#### Update Store
```http
PUT /api/stores/{storeId}
Authorization: Bearer <super_admin_token>
Content-Type: application/json

{
  "accessCode": "SecureAccessCode123!",  // Required for verification
  "name": "Main Street Store - Updated",
  "address": "123 Main Street, Suite 100",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001",
  "phone": "+1234567890",
  "email": "store@example.com",
  "currency": "USD",
  "timezone": "America/New_York",
  "isActive": true
}
```

#### Validate Store Access Code
```http
POST /api/stores/validate-access
Authorization: Bearer <super_admin_token>
Content-Type: application/json

{
  "storeId": 1,              // Use either storeId
  "storeCode": "ST001",       // or storeCode
  "accessCode": "SecureAccessCode123!"
}
```

**Response (Valid):**
```json
{
  "success": true,
  "message": "Access code is valid",
  "data": true
}
```

**Response (Invalid):**
```json
{
  "success": false,
  "message": "Invalid access code",
  "data": null
}
```

#### Delete Store (Soft Delete)
```http
DELETE /api/stores/{storeId}
Authorization: Bearer <super_admin_token>
Content-Type: application/json

{
  "accessCode": "SecureAccessCode123!"
}
```

---

### User Management

#### Create User (Enhanced with Role Validation)
```http
POST /api/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",  // Optional, auto-generated if not provided
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "roleName": "ADMIN",           // Role to assign
  "storeId": 1                   // Required for ADMIN role
}
```

**SUPER_ADMIN Creating ADMIN:**
```json
{
  "username": "store_admin",
  "email": "admin@store1.com",
  "firstName": "Store",
  "lastName": "Admin",
  "roleName": "ADMIN",
  "storeId": 1                   // Required!
}
```

**ADMIN Creating Staff:**
```json
{
  "username": "sales_person",
  "email": "sales@store1.com",
  "firstName": "Sales",
  "lastName": "Person",
  "roleName": "SALES_STAFF",
  "storeId": 1                   // Automatically set to ADMIN's store
}
```

#### Get All Users
```http
GET /api/users
Authorization: Bearer <token>
```

**Behavior:**
- **SUPER_ADMIN**: Returns all users system-wide
- **ADMIN**: Returns only users from their assigned store

---

## Usage Examples

### Scenario 1: SUPER_ADMIN Setting Up a New Store

**Step 1: Create the Store**
```bash
curl -X POST http://localhost:8080/api/stores \
  -H "Authorization: Bearer <super_admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ST001",
    "name": "Downtown Branch",
    "accessCode": "Downtown2024!",
    "address": "100 Main St",
    "city": "Los Angeles",
    "state": "CA",
    "country": "USA"
  }'
```

**Step 2: Create an ADMIN for the Store**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <super_admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "downtown_admin",
    "email": "admin@downtown.com",
    "firstName": "Admin",
    "lastName": "User",
    "roleName": "ADMIN",
    "storeId": 1
  }'
```

### Scenario 2: ADMIN Creating Store Staff

**Login as ADMIN first, then:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "store_manager",
    "email": "manager@downtown.com",
    "firstName": "John",
    "lastName": "Manager",
    "roleName": "STORE_MANAGER"
  }'
```
*Note: `storeId` is automatically set to ADMIN's assigned store*

### Scenario 3: SUPER_ADMIN Updating Store

**Step 1: Validate Access (Optional)**
```bash
curl -X POST http://localhost:8080/api/stores/validate-access \
  -H "Authorization: Bearer <super_admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": 1,
    "accessCode": "Downtown2024!"
  }'
```

**Step 2: Update Store**
```bash
curl -X PUT http://localhost:8080/api/stores/1 \
  -H "Authorization: Bearer <super_admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "accessCode": "Downtown2024!",
    "name": "Downtown Branch - Premium",
    "phone": "+1-555-0100"
  }'
```

### Scenario 4: Switch Store Context (SUPER_ADMIN)

**View Store 1:**
```bash
curl -X POST http://localhost:8080/api/stores/validate-access \
  -H "Authorization: Bearer <super_admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": 1,
    "accessCode": "Store1AccessCode"
  }'
```

**Switch to Store 2:**
```bash
curl -X POST http://localhost:8080/api/stores/validate-access \
  -H "Authorization: Bearer <super_admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": 2,
    "accessCode": "Store2AccessCode"
  }'
```

---

## Security Considerations

### Access Code Security
1. **Hashing**: Access codes are hashed using BCrypt with a strength of 12
2. **Never Returned**: Access codes are never included in API responses
3. **Required for Modifications**: All store updates/deletes require access code
4. **Validation**: Access code must be provided to validate before viewing/editing store data

### Role Assignment Security
1. **SUPER_ADMIN Creation**: Cannot be created through API (must be seeded in database)
2. **ADMIN Assignment**: Only SUPER_ADMIN can assign ADMIN role
3. **Store Restriction**: ADMIN must be tied to a store
4. **Scope Enforcement**: ADMIN can only manage users in their store

### Best Practices
1. **Strong Access Codes**: Use complex passwords for store access codes
2. **Rotate Codes**: Periodically update access codes
3. **Audit Logging**: Monitor who creates/modifies stores
4. **Principle of Least Privilege**: Assign minimum necessary roles

---

## Error Handling

### Common Errors

**1. Creating ADMIN without store:**
```json
{
  "success": false,
  "message": "ADMIN role requires a store assignment"
}
```

**2. ADMIN trying to create another ADMIN:**
```json
{
  "success": false,
  "message": "Only SUPER_ADMIN can create ADMIN users"
}
```

**3. ADMIN creating user for different store:**
```json
{
  "success": false,
  "message": "ADMIN can only create users for their assigned store"
}
```

**4. Invalid access code:**
```json
{
  "success": false,
  "message": "Invalid access code"
}
```

**5. Duplicate store code:**
```json
{
  "success": false,
  "message": "Store with code ST001 already exists"
}
```

---

## Database Schema

### Stores Table
```sql
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    access_code VARCHAR(50) NOT NULL,  -- BCrypt hashed
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);
```

### Users Table (Updated)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    role_id BIGINT,
    store_id BIGINT,                    -- Links user to store
    is_active BOOLEAN DEFAULT true,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (store_id) REFERENCES stores(id)
);
```

---

## Frontend Integration

### Store Selection Component
```typescript
// Store Context for SUPER_ADMIN
interface StoreContext {
  currentStore: Store | null;
  validateAndSelectStore: (storeId: number, accessCode: string) => Promise<boolean>;
  clearStore: () => void;
}

// Example usage
const selectStore = async (storeId: number, accessCode: string) => {
  const response = await fetch('/api/stores/validate-access', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ storeId, accessCode })
  });
  
  if (response.ok) {
    const store = await fetch(`/api/stores/${storeId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    setCurrentStore(await store.json());
    return true;
  }
  return false;
};
```

### User Creation with Role Validation
```typescript
const createUser = async (userData: CreateUserRequest) => {
  // Frontend should validate based on current user role
  const currentUserRole = getCurrentUserRole();
  
  if (currentUserRole === 'ADMIN' && userData.roleName === 'ADMIN') {
    alert('You cannot create other ADMIN users');
    return;
  }
  
  if (userData.roleName === 'ADMIN' && !userData.storeId) {
    alert('ADMIN role requires a store assignment');
    return;
  }
  
  const response = await fetch('/api/users', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(userData)
  });
  
  return response.json();
};
```

---

## Summary

### Key Points
1. ✅ Only **SUPER_ADMIN** can manage stores
2. ✅ Store **access code** required for all modifications
3. ✅ Only **SUPER_ADMIN** can create **ADMIN** users
4. ✅ **ADMIN** must be assigned to a specific store
5. ✅ **ADMIN** can only manage users in their assigned store
6. ✅ **SUPER_ADMIN** sees all users, **ADMIN** sees only their store's users
7. ✅ No one can create **SUPER_ADMIN** through API

### Next Steps
1. Build frontend store selection UI for SUPER_ADMIN
2. Implement user list filtering by store for ADMIN
3. Add role dropdown validation in user creation form
4. Create store management dashboard
5. Add audit logging for store/user creation

---

## Support & References
- See `API-AUTHENTICATION-GUIDE.md` for authentication details
- See `DEBUGGING-403-ERRORS.md` for troubleshooting
- See `FIXES-SUMMARY.md` for recent changes

