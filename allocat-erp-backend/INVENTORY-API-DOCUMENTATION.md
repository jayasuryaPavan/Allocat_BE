# Allocat ERP - Inventory Management API Documentation

This document describes the complete inventory allocation and management system for the Allocat ERP backend, based on the reference [pinnacleBackend](https://github.com/jayasuryaPavan/pinnacleBackend) structure.

## Overview

The inventory management system handles the complete flow from JSON-based received stock upload to verified inventory management:

1. **Received Stock Upload**: Frontend uploads JSON array with unverified stock data
2. **Received Stock Storage**: Data is stored as "received stock" with PENDING status
3. **Verification**: Client verifies actual received quantities
4. **Inventory Update**: Verified stock is added to inventory

## API Endpoints

### 1. Received Stock Upload

#### Add Received Stock via JSON
```http
POST /api/inventory/received-stock
Content-Type: application/json
```

**Request Body:**
```json
[
  {
    "productCode": "PROD001",
    "productName": "Widget A",
    "expectedQuantity": 100,
    "unitPrice": 25.50,
    "supplierName": "Supplier ABC",
    "supplierInvoice": "INV-001",
    "batchNumber": "BATCH-001",
    "notes": "Initial stock"
  },
  {
    "productCode": "PROD002",
    "productName": "Widget B",
    "expectedQuantity": 50,
    "unitPrice": 15.75,
    "supplierName": "Supplier XYZ",
    "supplierInvoice": "INV-002",
    "batchNumber": "BATCH-002",
    "notes": "Replacement stock"
  }
]
```

**Response:**
```json
{
  "success": true,
  "message": "Received stock processed successfully. 2 records created.",
  "data": [
    {
      "id": 1,
      "productCode": "PROD001",
      "productName": "Widget A",
      "expectedQuantity": 100,
      "receivedQuantity": 0,
      "verifiedQuantity": 0,
      "status": "PENDING",
      "batchNumber": "BATCH-001",
      "supplierName": "Supplier ABC"
    }
  ]
}
```

### 2. Received Stock Management

#### Get All Received Stock
```http
GET /api/inventory/received-stock
```

#### Get Pending Received Stock
```http
GET /api/inventory/received-stock/pending
```

**Response:**
```json
{
  "success": true,
  "message": "Pending received stock retrieved successfully",
  "data": [
    {
      "id": 1,
      "productCode": "PROD001",
      "productName": "Widget A",
      "expectedQuantity": 100,
      "receivedQuantity": 0,
      "verifiedQuantity": 0,
      "status": "PENDING",
      "batchNumber": "BATCH-001",
      "supplierName": "Supplier ABC",
      "receivedDate": "2024-01-15T10:30:00"
    }
  ]
}
```

#### Verify Received Stock
```http
POST /api/inventory/received-stock/{receivedStockId}/verify
Content-Type: application/x-www-form-urlencoded

verifiedQuantity=95&verifiedBy=John Doe
```

**Response:**
```json
{
  "success": true,
  "message": "Stock verified and added to inventory successfully",
  "data": {
    "id": 1,
    "product": {
      "id": 1,
      "productCode": "PROD001",
      "name": "Widget A"
    },
    "currentQuantity": 95,
    "availableQuantity": 95,
    "reservedQuantity": 0,
    "unitCost": 25.50,
    "totalValue": 2422.50,
    "location": "Main Warehouse",
    "batchNumber": "BATCH-001"
  }
}
```

### 3. Inventory Management

#### Get Current Inventory
```http
GET /api/inventory/current
```

#### Get Inventory by Product
```http
GET /api/inventory/product/{productId}
```

#### Get Low Stock Items
```http
GET /api/inventory/low-stock
```

#### Get Out of Stock Items
```http
GET /api/inventory/out-of-stock
```

#### Reserve Inventory
```http
POST /api/inventory/reserve
Content-Type: application/x-www-form-urlencoded

productId=1&quantity=10&reservedBy=Sales Team
```

#### Release Reservation
```http
POST /api/inventory/release-reservation
Content-Type: application/x-www-form-urlencoded

productId=1&quantity=5&releasedBy=Sales Team
```

### 4. Product Management

#### Get All Products
```http
GET /api/products?page=0&size=20&search=widget&category=electronics&supplier=ABC
```

#### Get Product by ID
```http
GET /api/products/{id}
```

#### Get Product by Code
```http
GET /api/products/code/{productCode}
```

#### Create Product
```http
POST /api/products
Content-Type: application/json

{
  "productCode": "PROD003",
  "name": "Widget C",
  "description": "High-quality widget",
  "category": "Electronics",
  "unitPrice": 30.00,
  "unitOfMeasure": "pcs",
  "minimumStockLevel": 10,
  "maximumStockLevel": 1000,
  "supplierName": "Supplier DEF",
  "barcode": "1234567890123",
  "sku": "WIDGET-C-001"
}
```

#### Update Product
```http
PUT /api/products/{id}
Content-Type: application/json

{
  "productCode": "PROD003",
  "name": "Widget C Updated",
  "description": "Updated description",
  "category": "Electronics",
  "unitPrice": 32.00
}
```

#### Delete Product (Soft Delete)
```http
DELETE /api/products/{id}
```

#### Search Products
```http
GET /api/products/search?searchTerm=widget
```

#### Get Categories
```http
GET /api/products/categories
```

### 5. Stock Discrepancies

#### Get Stock Discrepancies
```http
GET /api/inventory/discrepancies
```

**Response:**
```json
{
  "success": true,
  "message": "Stock discrepancies retrieved successfully",
  "data": [
    {
      "id": 1,
      "productCode": "PROD001",
      "productName": "Widget A",
      "expectedQuantity": 100,
      "receivedQuantity": 95,
      "verifiedQuantity": 95,
      "status": "DISCREPANCY",
      "shortageQuantity": 5,
      "notes": "5 units damaged during transport"
    }
  ]
}
```

## Database Schema

### Products Table
- `id`: Primary key
- `product_code`: Unique product identifier
- `name`: Product name
- `description`: Product description
- `category`: Product category
- `unit_price`: Price per unit
- `unit_of_measure`: Unit of measurement
- `minimum_stock_level`: Minimum stock threshold
- `maximum_stock_level`: Maximum stock threshold
- `is_active`: Active status
- `supplier_name`: Supplier information
- `barcode`: Product barcode
- `sku`: Stock keeping unit
- Additional fields for brand, model, color, size, weight, dimensions

### Received Stock Table
- `id`: Primary key
- `product_id`: Reference to products table
- `product_code`: Product code
- `product_name`: Product name
- `expected_quantity`: Expected quantity from CSV
- `received_quantity`: Actually received quantity
- `verified_quantity`: Verified quantity after inspection
- `unit_price`: Price per unit
- `total_value`: Total value
- `status`: PENDING, VERIFIED, REJECTED, PARTIAL, DISCREPANCY
- `batch_number`: Batch identifier
- `supplier_name`: Supplier information
- `supplier_invoice_number`: Invoice number
- `delivery_date`: Actual delivery date
- `received_date`: Date when stock was received
- `verified_date`: Date when stock was verified
- `received_by`: Person who received the stock
- `verified_by`: Person who verified the stock
- `csv_upload_id`: Upload batch identifier
- `row_number`: Row number in the upload batch

### Inventory Table
- `id`: Primary key
- `product_id`: Reference to products table
- `current_quantity`: Current stock quantity
- `reserved_quantity`: Reserved quantity
- `available_quantity`: Available quantity (current - reserved)
- `unit_cost`: Cost per unit
- `total_value`: Total inventory value
- `last_updated`: Last update timestamp
- `last_updated_by`: Person who last updated
- `location`: Storage location
- `warehouse`: Warehouse identifier
- `shelf`: Shelf identifier
- `bin`: Bin identifier
- `batch_number`: Batch identifier
- `expiry_date`: Product expiry date
- `supplier_name`: Supplier information
- `purchase_order_number`: Purchase order reference
- `received_stock_id`: Reference to received stock

## Workflow

### 1. Received Stock Upload Process
1. Frontend sends JSON array via `POST /api/inventory/received-stock`
2. System processes JSON and creates ReceivedStock records with PENDING status
3. Products are created automatically if they don't exist
4. Response includes all created received stock records

### 2. Stock Verification Process
1. Frontend retrieves pending received stock via `/api/inventory/received-stock/pending`
2. User verifies actual received quantities
3. Frontend calls `/api/inventory/received-stock/{id}/verify` with verified quantity
4. System updates ReceivedStock status and creates/updates Inventory record
5. Available quantity is automatically calculated

### 3. Inventory Management
1. Current inventory can be viewed via `/api/inventory/current`
2. Low stock and out-of-stock items can be monitored
3. Inventory can be reserved and released as needed
4. Discrepancies are tracked and can be reviewed

## Error Handling

All API endpoints return consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

Common HTTP status codes:
- `200 OK`: Success
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## Security Considerations

- All endpoints require proper authentication
- JSON payload size limits are enforced
- Input validation is performed on all data
- SQL injection protection through JPA
- XSS protection through input sanitization

## Performance Optimizations

- Database indexes on frequently queried fields
- Pagination for large result sets
- Lazy loading for entity relationships
- Caching for frequently accessed data
- Batch operations for bulk updates

## Testing

The API can be tested using:
- Swagger UI at `/swagger-ui.html`
- Postman collection
- Unit tests for all service methods
- Integration tests for API endpoints

## Deployment

The application can be deployed as:
- Executable JAR file
- Windows executable (.exe)
- Docker container
- Traditional web application

See the build documentation for detailed deployment instructions.

