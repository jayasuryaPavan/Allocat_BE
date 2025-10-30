# Received Stock API Documentation

## Overview
The API now supports adding received stock via JSON payload instead of only CSV files. This allows the frontend to send data directly without file uploads.

## Endpoints

### 1. Add Received Stock via JSON
**POST** `/api/inventory/received-stock`

Adds received stock records by sending a JSON array of product information.

#### Request Body
```json
[
  {
    "productCode": "PROD001",
    "productName": "Laptop Pro 15",
    "expectedQuantity": 25,
    "unitPrice": 1299.99,
    "supplierName": "TechCorp",
    "supplierInvoice": "INV-2024-001",
    "batchNumber": "BATCH-001",
    "notes": "High priority delivery"
  },
  {
    "productCode": "PROD002",
    "productName": "Wireless Mouse",
    "expectedQuantity": 150,
    "unitPrice": 29.99,
    "supplierName": "TechCorp",
    "supplierInvoice": "INV-2024-001",
    "batchNumber": "BATCH-001",
    "notes": null
  }
]
```

#### Request Fields
- `productCode` (required): Unique product code
- `productName` (required): Product name
- `expectedQuantity` (required): Expected quantity received
- `unitPrice` (optional): Price per unit
- `supplierName` (optional): Supplier name
- `supplierInvoice` (optional): Invoice number
- `batchNumber` (optional): Batch or lot number
- `notes` (optional): Additional notes

#### Response (Success)
```json
{
  "success": true,
  "message": "Received stock processed successfully. 2 records created.",
  "data": [
    {
      "id": 1,
      "productCode": "PROD001",
      "productName": "Laptop Pro 15",
      "expectedQuantity": 25,
      "receivedQuantity": 0,
      "verifiedQuantity": 0,
      "unitPrice": 1299.99,
      "status": "PENDING",
      "supplierName": "TechCorp",
      "csvUploadId": "uuid-here",
      "rowNumber": 1
    },
    {
      "id": 2,
      "productCode": "PROD002",
      "productName": "Wireless Mouse",
      "expectedQuantity": 150,
      "receivedQuantity": 0,
      "verifiedQuantity": 0,
      "unitPrice": 29.99,
      "status": "PENDING",
      "supplierName": "TechCorp",
      "csvUploadId": "uuid-here",
      "rowNumber": 2
    }
  ]
}
```

#### Response (Error)
```json
{
  "success": false,
  "message": "Received stock list cannot be empty"
}
```

### 2. Get All Received Stock
**GET** `/api/inventory/received-stock`

Retrieves all received stock records (all statuses).

### 3. Get Pending Received Stock
**GET** `/api/inventory/received-stock/pending`

Retrieves all unverified stock that needs to be verified.

### 4. Verify Received Stock
**POST** `/api/inventory/received-stock/{receivedStockId}/verify?verifiedQuantity={quantity}&verifiedBy={name}`

Verifies the received quantity and adds the stock to inventory.

## Examples

### cURL Example
```bash
curl -X POST http://localhost:8080/api/inventory/received-stock \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '[
    {
      "productCode": "PROD001",
      "productName": "Laptop Pro 15",
      "expectedQuantity": 25,
      "unitPrice": 1299.99,
      "supplierName": "TechCorp",
      "supplierInvoice": "INV-2024-001"
    }
  ]'
```

### JavaScript/Fetch Example
```javascript
const receivedStockData = [
  {
    productCode: 'PROD001',
    productName: 'Laptop Pro 15',
    expectedQuantity: 25,
    unitPrice: 1299.99,
    supplierName: 'TechCorp',
    supplierInvoice: 'INV-2024-001',
    batchNumber: 'BATCH-001',
    notes: 'High priority delivery'
  },
  {
    productCode: 'PROD002',
    productName: 'Wireless Mouse',
    expectedQuantity: 150,
    unitPrice: 29.99,
    supplierName: 'TechCorp',
    supplierInvoice: 'INV-2024-001'
  }
];

fetch('http://localhost:8080/api/inventory/received-stock', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_TOKEN'
  },
  body: JSON.stringify(receivedStockData)
})
.then(response => response.json())
.then(data => {
  console.log('Success:', data);
})
.catch((error) => {
  console.error('Error:', error);
});
```

## Product Auto-Creation

If a product with the specified `productCode` doesn't exist, it will be automatically created with:
- `productCode`: The provided product code
- `name`: The provided product name
- `sku`: Set to the product code (to avoid null constraint)
- `isActive`: true

## Status Flow

1. **PENDING** - Initial status when received stock is created
2. **VERIFIED** - When stock is verified using the verify endpoint
3. **DISCREPANCY** - If there's a mismatch in quantities

## Notes

- All received stock entries start with `receivedQuantity` and `verifiedQuantity` set to 0
- The `totalValue` is automatically calculated as `unitPrice * expectedQuantity`
- Each batch of records gets a unique `csvUploadId` for tracking
- The `rowNumber` represents the position in the submitted array
