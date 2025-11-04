# Allocat ERP - Complete API Documentation

**Base URL:** `http://localhost:8080`  
**Version:** 1.0.0  
**Last Updated:** November 4, 2025

---

## Table of Contents

1. [Authentication](#authentication)
2. [User Management](#user-management)
3. [Store Management](#store-management)
4. [Product Management](#product-management)
5. [Inventory Management](#inventory-management)
6. [InvenGadu AI Chat](#invengadu-ai-chat)
7. [Common Patterns](#common-patterns)
8. [Error Handling](#error-handling)

---

## Authentication

All authenticated endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### POST `/api/auth/login`

Authenticate user and receive JWT tokens.

**Request:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "userId": 1,
    "username": "admin",
    "email": "admin@example.com",
    "firstName": "Admin",
    "lastName": "User",
    "phone": "+1234567890",
    "roleId": 1,
    "role": "SUPER_ADMIN",
    "storeId": 1,
    "storeCode": "STR001",
    "storeName": "Main Store",
    "isActive": true,
    "lastLoginAt": "2025-11-01T10:00:00",
    "permissions": ["ALL"]
  }
}
```

**Error (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid credentials",
  "data": null
}
```

---

### GET `/api/auth/me`

Get current authenticated user's details.

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User details retrieved successfully",
  "data": {
    "userId": 1,
    "username": "admin",
    "email": "admin@example.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "SUPER_ADMIN",
    "storeId": 1,
    "storeCode": "STR001",
    "storeName": "Main Store",
    "permissions": ["ALL"]
  }
}
```

---

### POST `/api/auth/refresh`

Refresh access token using refresh token.

**Headers:**
```
Authorization: Bearer <refresh-token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "username": "admin"
  }
}
```

---

### POST `/api/auth/logout`

Logout user and invalidate session.

**Headers:**
```
Authorization: Bearer <token> (optional)
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

**Note:** Frontend should clear tokens from storage regardless of response.

---

## User Management

### GET `/api/users`

Get all users (filtered by role permissions).

**Auth Required:** SUPER_ADMIN or ADMIN  
**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "firstName": "Admin",
      "lastName": "User",
      "phone": "+1234567890",
      "roleName": "SUPER_ADMIN",
      "storeId": 1,
      "storeCode": "STR001",
      "storeName": "Main Store",
      "active": true,
      "createdAt": "2025-11-01T10:00:00",
      "updatedAt": "2025-11-01T10:00:00"
    }
  ]
}
```

**Notes:**
- ADMIN users only see users from their own store
- SUPER_ADMIN sees all users

---

### POST `/api/users`

Create a new user.

**Auth Required:** SUPER_ADMIN or ADMIN  
**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "optional-password",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "roleName": "SALES_STAFF",
  "storeCode": "STR001"
}
```

**Alternative (Backward Compatible):**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "roleName": "SALES_STAFF",
  "storeId": 1
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 5,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "roleName": "SALES_STAFF",
    "storeId": 1,
    "storeCode": "STR001",
    "storeName": "Main Store",
    "active": true,
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": "2025-11-01T10:00:00"
  }
}
```

**Validation Rules:**
- Only SUPER_ADMIN can create ADMIN users
- ADMIN users require a store assignment
- Cannot create SUPER_ADMIN users through API
- ADMIN users can only create users for their own store
- If password is not provided, a random password is generated

---

### GET `/api/users/roles`

Get all available roles with permissions.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "SUPER_ADMIN",
      "description": "Super Administrator with full access",
      "permissions": ["ALL"]
    },
    {
      "id": 2,
      "name": "ADMIN",
      "description": "Store Administrator",
      "permissions": ["READ_USERS", "WRITE_USERS", "READ_INVENTORY", "WRITE_INVENTORY"]
    }
  ]
}
```

---

## Store Management

### GET `/api/stores`

Get all stores.

**Auth Required:** SUPER_ADMIN  
**Query Parameters:**
- `active` (boolean, optional) - Filter by active status

**Example:** `GET /api/stores?active=true`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Stores retrieved successfully",
  "data": [
    {
      "id": 1,
      "code": "STR001",
      "name": "Main Store",
      "address": "123 Main St",
      "city": "New York",
      "state": "NY",
      "country": "USA",
      "postalCode": "10001",
      "phone": "+1234567890",
      "email": "store@example.com",
      "taxId": "TAX123",
      "currency": "USD",
      "timezone": "America/New_York",
      "isActive": true,
      "createdAt": "2025-11-01T10:00:00",
      "updatedAt": "2025-11-01T10:00:00"
    }
  ]
}
```

---

### POST `/api/stores`

Create a new store.

**Auth Required:** SUPER_ADMIN

**Request:**
```json
{
  "code": "STR002",
  "name": "Branch Store",
  "accessCode": "SECURE_PASSWORD",
  "address": "456 Branch Ave",
  "city": "Los Angeles",
  "state": "CA",
  "country": "USA",
  "postalCode": "90001",
  "phone": "+1234567891",
  "email": "branch@example.com",
  "taxId": "TAX456",
  "currency": "USD",
  "timezone": "America/Los_Angeles"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Store created successfully",
  "data": {
    "id": 2,
    "code": "STR002",
    "name": "Branch Store",
    ...
  }
}
```

---

### GET `/api/stores/{id}`

Get store by ID.

**Auth Required:** SUPER_ADMIN  
**Path Parameters:** `id` (Long) - Store ID

**Example:** `GET /api/stores/1`

---

### GET `/api/stores/code/{storeCode}`

Get store by code.

**Auth Required:** SUPER_ADMIN  
**Path Parameters:** `storeCode` (String) - Store code

**Example:** `GET /api/stores/code/STR001`

---

### PUT `/api/stores/{id}`

Update store details.

**Auth Required:** SUPER_ADMIN  
**Path Parameters:** `id` (Long) - Store ID

**Request:**
```json
{
  "name": "Updated Store Name",
  "accessCode": "REQUIRED_FOR_UPDATE",
  "address": "New Address",
  "city": "New City",
  "phone": "+1234567892",
  "isActive": true
}
```

**Note:** `accessCode` is required for security verification.

---

### DELETE `/api/stores/{id}`

Soft delete a store.

**Auth Required:** SUPER_ADMIN  
**Path Parameters:** `id` (Long) - Store ID

**Request:**
```json
{
  "accessCode": "REQUIRED_FOR_DELETE"
}
```

---

### POST `/api/stores/validate-access`

Validate store access code.

**Auth Required:** SUPER_ADMIN

**Request:**
```json
{
  "storeId": 1,
  "accessCode": "ACCESS_CODE_HERE"
}
```

**Or:**
```json
{
  "storeCode": "STR001",
  "accessCode": "ACCESS_CODE_HERE"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Access code is valid",
  "data": true
}
```

---

## Product Management

### GET `/api/products`

Get all products with pagination, sorting, and filtering.

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 20 | Items per page |
| `sortBy` | string | `id` | Sort field: `name`, `productCode`, `unitPrice`, `category` |
| `sortDirection` | string | `asc` | Sort direction: `asc` or `desc` |
| `search` | string | - | Search by name, code, or barcode |
| `category` | string | - | Filter by category |
| `supplier` | string | - | Filter by supplier name |
| `active` | boolean | - | Filter by active status |

**Examples:**
```
GET /api/products
GET /api/products?page=0&size=10
GET /api/products?sortBy=name&sortDirection=asc
GET /api/products?search=laptop
GET /api/products?category=Electronics&sortBy=unitPrice&sortDirection=desc
GET /api/products?active=true&sortBy=name
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Products retrieved successfully. Page 1 of 5",
  "data": {
    "content": [
      {
        "id": 1,
        "productCode": "PROD001",
        "name": "Laptop Pro",
        "description": "High-performance laptop",
        "category": "Electronics",
        "unitPrice": 1299.99,
        "unitOfMeasure": "piece",
        "minimumStockLevel": 10,
        "maximumStockLevel": 100,
        "isActive": true,
        "supplierName": "Tech Supplier",
        "supplierContact": "+1234567890",
        "barcode": "123456789",
        "sku": "SKU001",
        "brand": "TechBrand",
        "model": "Pro2024",
        "createdAt": "2025-11-01T10:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true
      }
    },
    "totalPages": 5,
    "totalElements": 95,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false,
    "empty": false
  }
}
```

---

### GET `/api/products/{id}`

Get product by ID.

**Path Parameters:** `id` (Long) - Product ID

**Example:** `GET /api/products/1`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product retrieved successfully",
  "data": {
    "id": 1,
    "productCode": "PROD001",
    "name": "Laptop Pro",
    ...
  }
}
```

---

### GET `/api/products/code/{productCode}`

Get product by product code.

**Path Parameters:** `productCode` (String) - Product code

**Example:** `GET /api/products/code/PROD001`

---

### POST `/api/products`

Create a new product.

**Request:**
```json
{
  "productCode": "PROD003",
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "category": "Accessories",
  "unitPrice": 29.99,
  "unitOfMeasure": "piece",
  "minimumStockLevel": 20,
  "maximumStockLevel": 200,
  "supplierName": "Accessory Supplier",
  "barcode": "987654321",
  "sku": "SKU003",
  "brand": "MouseBrand",
  "isActive": true
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 3,
    "productCode": "PROD003",
    ...
  }
}
```

---

### PUT `/api/products/{id}`

Update an existing product.

**Path Parameters:** `id` (Long) - Product ID

**Request:** (same as POST, all fields optional)
```json
{
  "name": "Updated Product Name",
  "unitPrice": 34.99,
  "isActive": true
}
```

---

### DELETE `/api/products/{id}`

Soft delete a product (sets `isActive` to false).

**Path Parameters:** `id` (Long) - Product ID

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product deleted successfully",
  "data": null
}
```

---

### GET `/api/products/search`

Search products by name, code, or barcode.

**Query Parameters:**
- `searchTerm` (string, required) - Search term

**Example:** `GET /api/products/search?searchTerm=laptop`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Search results retrieved successfully",
  "data": [
    {
      "id": 1,
      "productCode": "PROD001",
      "name": "Laptop Pro",
      ...
    }
  ]
}
```

---

### GET `/api/products/categories`

Get all unique product categories.

**Example:** `GET /api/products/categories`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    "Electronics",
    "Accessories",
    "Furniture",
    "Office Supplies"
  ]
}
```

---

## Inventory Management

### GET `/api/inventory/current`

Get current inventory levels with pagination and sorting.

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 20 | Items per page |
| `sortBy` | string | `id` | Sort field: `availableQuantity`, `currentQuantity`, `product.name` |
| `sortDirection` | string | `asc` | Sort direction: `asc` or `desc` |

**Examples:**
```
GET /api/inventory/current
GET /api/inventory/current?page=0&size=10
GET /api/inventory/current?sortBy=availableQuantity&sortDirection=asc
GET /api/inventory/current?sortBy=product.name&sortDirection=asc
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Current inventory retrieved successfully. Page 1 of 10",
  "data": {
    "content": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "productCode": "PROD001",
          "name": "Laptop Pro"
        },
        "currentQuantity": 50,
        "reservedQuantity": 5,
        "availableQuantity": 45,
        "unitCost": 1000.00,
        "totalValue": 50000.00,
        "location": "Main Warehouse",
        "warehouse": "Main Warehouse",
        "batchNumber": "BATCH001",
        "supplierName": "Tech Supplier",
        "lastUpdated": "2025-11-01T10:00:00",
        "lastUpdatedBy": "admin"
      }
    ],
    "totalPages": 10,
    "totalElements": 195,
    "size": 20,
    "number": 0
  }
}
```

---

### GET `/api/inventory/low-stock`

Get items below minimum stock level with pagination.

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number |
| `size` | int | 20 | Items per page |
| `sortBy` | string | `availableQuantity` | Sort field |
| `sortDirection` | string | `asc` | Sort direction |

**Example:** `GET /api/inventory/low-stock?page=0&size=10&sortBy=availableQuantity&sortDirection=asc`

---

### GET `/api/inventory/out-of-stock`

Get items with zero inventory with pagination.

**Query Parameters:** Same as low-stock

**Example:** `GET /api/inventory/out-of-stock?page=0&size=10`

---

### GET `/api/inventory/product/{productId}`

Get inventory for a specific product.

**Path Parameters:** `productId` (Long) - Product ID

**Example:** `GET /api/inventory/product/1`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product inventory retrieved successfully",
  "data": {
    "id": 1,
    "product": {...},
    "currentQuantity": 50,
    "availableQuantity": 45,
    "reservedQuantity": 5
  }
}
```

---

### POST `/api/inventory/reserve`

Reserve inventory for a product.

**Query Parameters:**
- `productId` (Long, required) - Product ID
- `quantity` (Integer, required) - Quantity to reserve
- `reservedBy` (String, required) - Name of person reserving

**Example:** `POST /api/inventory/reserve?productId=1&quantity=10&reservedBy=admin`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Inventory reserved successfully",
  "data": {
    "id": 1,
    "currentQuantity": 50,
    "reservedQuantity": 15,
    "availableQuantity": 35
  }
}
```

---

### POST `/api/inventory/release-reservation`

Release a reserved quantity.

**Query Parameters:**
- `productId` (Long, required) - Product ID
- `quantity` (Integer, required) - Quantity to release
- `releasedBy` (String, required) - Name of person releasing

**Example:** `POST /api/inventory/release-reservation?productId=1&quantity=5&releasedBy=admin`

---

### GET `/api/inventory/discrepancies`

Get inventory discrepancies (differences between expected and actual stock).

**Example:** `GET /api/inventory/discrepancies`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Stock discrepancies retrieved successfully",
  "data": [
    {
      "id": 1,
      "product": {...},
      "expectedQuantity": 100,
      "verifiedQuantity": 95,
      "status": "DISCREPANCY",
      "shortageQuantity": 5
    }
  ]
}
```

---

### POST `/api/inventory/received-stock`

Add received stock (from supplier).

**Request:**
```json
[
  {
    "productCode": "PROD001",
    "productName": "Laptop Pro",
    "expectedQuantity": 100,
    "unitPrice": 1000.00,
    "supplierName": "Tech Supplier",
    "supplierInvoice": "INV-001",
    "batchNumber": "BATCH-001",
    "notes": "Initial stock"
  }
]
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Received stock processed successfully. 1 records created.",
  "data": [
    {
      "id": 1,
      "productCode": "PROD001",
      "expectedQuantity": 100,
      "receivedQuantity": 0,
      "verifiedQuantity": 0,
      "status": "PENDING",
      "batchNumber": "BATCH-001"
    }
  ]
}
```

---

### GET `/api/inventory/received-stock`

Get all received stock records.

**Example:** `GET /api/inventory/received-stock`

---

### GET `/api/inventory/received-stock/pending`

Get pending received stock (awaiting verification).

**Example:** `GET /api/inventory/received-stock/pending`

---

### POST `/api/inventory/received-stock/{receivedStockId}/verify`

Verify received stock and add to inventory.

**Path Parameters:** `receivedStockId` (Long) - Received stock ID  
**Query Parameters:**
- `verifiedQuantity` (Integer, required) - Actual quantity received
- `verifiedBy` (String, required) - Name of person verifying

**Example:** `POST /api/inventory/received-stock/1/verify?verifiedQuantity=95&verifiedBy=admin`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Stock verified and added to inventory successfully",
  "data": {
    "id": 1,
    "currentQuantity": 95,
    "availableQuantity": 95
  }
}
```

---

## InvenGadu AI Chat

AI-powered inventory assistant using Ollama.

### POST `/api/chat`

Send a message to the AI assistant.

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <token> (optional)
```

**Request:**
```json
{
  "message": "What items are low on stock?",
  "conversationId": "user123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Chat response generated successfully",
  "data": {
    "message": "Here are the items currently low on stock:\n\n1. Widget A (ID: 5) - Current Stock: 8\n2. Gadget B (ID: 12) - Current Stock: 15\n\nThese items should be restocked soon.",
    "conversationId": "user123",
    "metadata": {
      "model": "ollama-llama3.2:1b",
      "tools_used": true
    }
  }
}
```

**Supported Queries:**
- "Show me inventory statistics"
- "What items are low on stock?"
- "What's out of stock?"
- "Search for products containing 'laptop'"
- "Show me product ID 5"
- "Are there any discrepancies?"

**Note:** Response may take 10-120 seconds depending on the model and query complexity.

---

### GET `/api/chat/health`

Check if InvenGadu AI is ready.

**Example:** `GET /api/chat/health`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Health check passed",
  "data": {
    "status": "healthy",
    "ollama": "connected",
    "message": "InvenGadu is ready"
  }
}
```

**Response (503 Service Unavailable):**
```json
{
  "success": false,
  "message": "Health check failed",
  "data": {
    "status": "unhealthy",
    "ollama": "disconnected",
    "error": "Connection refused..."
  }
}
```

---

### POST `/api/chat/new`

Start a new conversation (clears history).

**Query Parameters:**
- `conversationId` (string, optional) - Conversation ID to reset

**Example:** `POST /api/chat/new?conversationId=user123`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "New conversation started",
  "data": {
    "success": "true",
    "message": "New conversation started",
    "conversation_id": "user123"
  }
}
```

---

## Common Patterns

### Pagination Response Format

All paginated endpoints return this structure:

```json
{
  "success": true,
  "message": "...",
  "data": {
    "content": [],           // Array of items
    "pageable": {...},       // Pagination info
    "totalPages": 5,         // Total number of pages
    "totalElements": 95,     // Total items
    "size": 20,              // Items per page
    "number": 0,             // Current page (0-based)
    "first": true,           // Is first page?
    "last": false,           // Is last page?
    "empty": false,          // Is page empty?
    "numberOfElements": 20   // Items in current page
  }
}
```

### Pagination Helper (Frontend)

```javascript
// Calculate pagination details
const {
  content: items,
  totalPages,
  totalElements,
  number: currentPage,
  first: isFirstPage,
  last: isLastPage
} = response.data;

const hasNextPage = !isLastPage;
const hasPreviousPage = !isFirstPage;
const showing = `${currentPage * size + 1}-${currentPage * size + items.length} of ${totalElements}`;
```

---

### Standard Response Format

All endpoints follow this format:

**Success Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "error": "Detailed error message"
}
```

---

## Error Handling

### Common HTTP Status Codes

| Code | Meaning | When It Occurs |
|------|---------|----------------|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server-side error |
| 503 | Service Unavailable | External service (Ollama) unavailable |

---

### Common Error Scenarios

**1. Invalid Token:**
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "data": null
}
```

**2. Insufficient Permissions:**
```json
{
  "success": false,
  "message": "Only SUPER_ADMIN can create ADMIN users",
  "data": null
}
```

**3. Validation Error:**
```json
{
  "success": false,
  "message": "Store with code 'STR999' does not exist. Please use a valid store code.",
  "data": null
}
```

**4. Duplicate Entry:**
```json
{
  "success": false,
  "message": "Product with code PROD001 already exists",
  "data": null
}
```

**5. Not Found:**
```json
{
  "success": false,
  "message": "Resource not found",
  "data": null
}
```

---

## Frontend Integration Examples

### Authentication Flow

```javascript
// 1. Login
const login = async (username, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  
  const data = await response.json();
  
  if (data.success) {
    // Store tokens
    localStorage.setItem('accessToken', data.data.accessToken);
    localStorage.setItem('refreshToken', data.data.refreshToken);
    localStorage.setItem('user', JSON.stringify(data.data));
    return data.data;
  }
  throw new Error(data.message);
};

// 2. Make authenticated request
const fetchUsers = async () => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch('http://localhost:8080/api/users', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return await response.json();
};

// 3. Logout
const logout = async () => {
  const token = localStorage.getItem('accessToken');
  
  await fetch('http://localhost:8080/api/auth/logout', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  // Clear local storage
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
};
```

---

### Pagination Example

```javascript
const fetchProducts = async (page = 0, size = 20, sortBy = 'name', sortDirection = 'asc') => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sortBy: sortBy,
    sortDirection: sortDirection
  });

  const response = await fetch(`http://localhost:8080/api/products?${params}`);
  const data = await response.json();

  return {
    items: data.data.content,
    totalPages: data.data.totalPages,
    totalItems: data.data.totalElements,
    currentPage: data.data.number,
    hasNext: !data.data.last,
    hasPrevious: !data.data.first
  };
};

// Usage
const { items, totalPages, currentPage } = await fetchProducts(0, 10, 'unitPrice', 'desc');
```

---

### Error Handling

```javascript
const apiCall = async (url, options) => {
  try {
    const response = await fetch(url, options);
    const data = await response.json();
    
    if (!data.success) {
      // Handle API error
      throw new Error(data.message || 'API request failed');
    }
    
    return data.data;
    
  } catch (error) {
    // Handle network error
    if (error.name === 'TypeError') {
      throw new Error('Network error. Please check your connection.');
    }
    throw error;
  }
};
```

---

### Chat API with Loading State

```javascript
const chatWithAI = async (message, conversationId = 'default') => {
  const token = localStorage.getItem('accessToken');
  
  // Show loading indicator
  showLoading('AI is thinking... (this may take up to 2 minutes)');
  
  try {
    const response = await fetch('http://localhost:8080/api/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ message, conversationId })
      // No timeout - let backend handle it (up to 3 minutes)
    });

    const data = await response.json();
    
    if (data.success) {
      return data.data.message;
    } else {
      throw new Error(data.message);
    }
  } finally {
    hideLoading();
  }
};
```

---

## Best Practices

### 1. Always Check `success` Field
```javascript
if (data.success) {
  // Handle success
} else {
  // Handle error using data.message
}
```

### 2. Store Tokens Securely
```javascript
// Use httpOnly cookies in production
// For development, localStorage is acceptable
localStorage.setItem('accessToken', token);
```

### 3. Handle Token Expiration
```javascript
// Refresh token when access token expires
if (response.status === 401) {
  const newToken = await refreshAccessToken();
  // Retry original request with new token
}
```

### 4. Use Pagination for Large Lists
```javascript
// Don't fetch all items at once
// Use pagination to load data as needed
const products = await fetchProducts(page, 20);
```

### 5. Provide User Feedback
```javascript
// Show loading states
setLoading(true);
const data = await apiCall();
setLoading(false);

// Show error messages
if (!data.success) {
  showError(data.message);
}
```

---

## Quick Reference

### Base URLs
- **Development:** `http://localhost:8080`
- **Production:** `https://api.yourdomain.com`

### Authentication Header
```
Authorization: Bearer <your-jwt-token>
```

### Content Type
```
Content-Type: application/json
```

### CORS Allowed Origins
- `http://localhost:3000`
- `http://localhost:4200`
- `http://localhost:5173`
- `http://localhost:8081`

---

## Additional Resources

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Health Check:** `GET /api/health`
- **Test Endpoint:** `GET /api/test`

---

## Rate Limits & Performance

- **Chat API Response Time:** 10-120 seconds (depending on model)
- **Timeout Settings:** Up to 3 minutes
- **Recommended Page Size:** 20-50 items
- **Max Page Size:** 100 items

---

## Support

For issues or questions:
- Check application logs: `logs/allocat-erp.log`
- Use Swagger UI for interactive testing
- Verify authentication token is valid
- Check CORS configuration for frontend origin

---

**Last Updated:** November 4, 2025  
**API Version:** 1.0.0  
**Spring Boot Version:** 3.3.4

