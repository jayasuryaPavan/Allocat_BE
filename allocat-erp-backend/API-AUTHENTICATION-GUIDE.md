# API Authentication Guide

## Overview
This document lists all API endpoints and their authentication requirements.

## Authentication Method
- **Type:** Bearer Token (JWT)
- **Header:** `Authorization: Bearer <token>`
- **Content-Type:** `application/json`

## Getting a JWT Token

### Login Endpoint
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "roleId": 1,
    "role": "SUPER_ADMIN",
    "storeId": null,
    "isActive": true,
    "lastLoginAt": "2025-10-23T10:30:00",
    "permissions": ["users:create", "users:read", ...]
  }
}
```

---

## Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/users/roles` | Get all roles |
| GET | `/api/health` | Health check |
| GET | `/actuator/**` | Spring Boot actuator endpoints |
| GET | `/swagger-ui/**` | Swagger UI |
| GET | `/v3/api-docs/**` | OpenAPI documentation |

---

## Protected Endpoints (Authentication Required)

### Authentication & User Profile

| Method | Endpoint | Description | Required Headers |
|--------|----------|-------------|------------------|
| GET | `/api/auth/me` | Get current user profile | `Authorization: Bearer <token>` |

**Example Request:**
```http
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### User Management

| Method | Endpoint | Description | Required Headers |
|--------|----------|-------------|------------------|
| GET | `/api/users` | Get all users | `Authorization: Bearer <token>`<br>`Content-Type: application/json` |
| POST | `/api/users` | Create a new user | `Authorization: Bearer <token>`<br>`Content-Type: application/json` |
| GET | `/api/users/{id}` | Get user by ID | `Authorization: Bearer <token>` |
| PUT | `/api/users/{id}` | Update user | `Authorization: Bearer <token>`<br>`Content-Type: application/json` |
| DELETE | `/api/users/{id}` | Delete user | `Authorization: Bearer <token>` |

**Example Request (Create User):**
```http
POST /api/users
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "username": "jane_doe",
  "email": "jane@example.com",
  "password": "SecurePass123!",
  "firstName": "Jane",
  "lastName": "Doe",
  "phone": "+1234567891",
  "roleName": "STORE_MANAGER",
  "storeId": 1
}
```

---

### Inventory Management

| Method | Endpoint | Description | Required Headers |
|--------|----------|-------------|------------------|
| GET | `/api/inventory/current` | Get current inventory | `Authorization: Bearer <token>` |
| POST | `/api/inventory/upload-csv` | Upload inventory CSV | `Authorization: Bearer <token>`<br>`Content-Type: multipart/form-data` |
| GET | `/api/inventory/product/{productId}` | Get inventory by product ID | `Authorization: Bearer <token>` |
| GET | `/api/inventory/low-stock` | Get low stock items | `Authorization: Bearer <token>` |
| GET | `/api/inventory/out-of-stock` | Get out of stock items | `Authorization: Bearer <token>` |
| POST | `/api/inventory/reserve` | Reserve inventory | `Authorization: Bearer <token>`<br>`Content-Type: application/json` |
| POST | `/api/inventory/release-reservation` | Release inventory reservation | `Authorization: Bearer <token>`<br>`Content-Type: application/json` |
| GET | `/api/inventory/received-stock` | Get all received stock | `Authorization: Bearer <token>` |
| GET | `/api/inventory/received-stock/pending` | Get pending received stock | `Authorization: Bearer <token>` |
| POST | `/api/inventory/received-stock/{id}/verify` | Verify received stock | `Authorization: Bearer <token>`<br>`Content-Type: application/json` |
| GET | `/api/inventory/discrepancies` | Get stock discrepancies | `Authorization: Bearer <token>` |

**Example Request (Get Current Inventory):**
```http
GET /api/inventory/current
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### Product Management

| Method | Endpoint | Description | Required Headers |
|--------|----------|-------------|------------------|
| GET | `/api/products` | Get all products | `Authorization: Bearer <token>` |
| POST | `/api/products` | Create a new product | `Authorization: Bearer <token>`<br>`Content-Type: application/json` |
| GET | `/api/products/{id}` | Get product by ID | `Authorization: Bearer <token>` |
| PUT | `/api/products/{id}` | Update product | `Authorization: Bearer <token>`<br>`Content-Type: application/json` |
| DELETE | `/api/products/{id}` | Delete product | `Authorization: Bearer <token>` |
| GET | `/api/products/search` | Search products | `Authorization: Bearer <token>` |
| GET | `/api/products/category/{category}` | Get products by category | `Authorization: Bearer <token>` |

**Example Request (Get All Products):**
```http
GET /api/products
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Troubleshooting 403 Forbidden Errors

If you're receiving 403 errors despite sending a valid token, check the following:

### 1. Token Format
Ensure the token is in the correct format:
```
Authorization: Bearer <actual_token_here>
```

**Common Mistakes:**
- ❌ `Authorization: <token>` (missing "Bearer")
- ❌ `Authorization: bearer <token>` (lowercase "bearer")
- ❌ `Authorization: Bearer<token>` (missing space after "Bearer")
- ✅ `Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...`

### 2. Token Expiration
Access tokens expire after 1 hour (3600000ms). Check if your token is still valid:
- Decode your JWT token at https://jwt.io/
- Check the `exp` claim (expiration timestamp)
- If expired, use the refresh token to get a new access token

### 3. CORS Issues
Ensure your frontend origin is in the allowed origins list:
- `http://localhost:4200` (Angular)
- `http://localhost:3000` (React)
- `http://localhost:5173` (Vite)
- `http://localhost:8081` (Custom)

### 4. Content-Type Header
For POST/PUT requests, always include:
```
Content-Type: application/json
```

### 5. Check Backend Logs
The backend now logs detailed authentication information. Look for:
```
Processing request: GET /api/inventory/current
Authorization header: Bearer ***
Extracted username from token: john_doe
Token validated successfully. User: john_doe, Role: SUPER_ADMIN
Successfully authenticated user: john_doe with role: SUPER_ADMIN
```

### 6. Verify User Role and Permissions
Some endpoints may require specific roles or permissions. Check your user's role:
```http
GET /api/auth/me
Authorization: Bearer <token>
```

---

## Frontend Integration Examples

### JavaScript (Fetch API)
```javascript
const token = localStorage.getItem('accessToken');

fetch('http://localhost:8080/api/inventory/current', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));
```

### JavaScript (Axios)
```javascript
import axios from 'axios';

const token = localStorage.getItem('accessToken');

axios.get('http://localhost:8080/api/inventory/current', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
  .then(response => console.log(response.data))
  .catch(error => console.error('Error:', error));
```

### Angular (HttpClient)
```typescript
import { HttpClient, HttpHeaders } from '@angular/common/http';

const token = localStorage.getItem('accessToken');
const headers = new HttpHeaders({
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
});

this.http.get('http://localhost:8080/api/inventory/current', { headers })
  .subscribe(
    data => console.log(data),
    error => console.error('Error:', error)
  );
```

### React (Axios with Interceptor)
```javascript
import axios from 'axios';

// Create axios instance
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add request interceptor to include token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// Add response interceptor to handle 401 errors
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // Token expired or invalid - redirect to login
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Use the api instance
api.get('/api/inventory/current')
  .then(response => console.log(response.data))
  .catch(error => console.error('Error:', error));
```

---

## Testing with cURL

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "password": "your_password"
  }'
```

### Access Protected Endpoint
```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl -X GET http://localhost:8080/api/inventory/current \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

---

## Testing with Postman

1. **Login to get token:**
   - Method: POST
   - URL: `http://localhost:8080/api/auth/login`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "username": "your_username",
       "password": "your_password"
     }
     ```
   - Copy the `accessToken` from the response

2. **Access protected endpoint:**
   - Method: GET
   - URL: `http://localhost:8080/api/inventory/current`
   - Headers:
     - `Authorization`: `Bearer <paste_your_token_here>`
     - `Content-Type`: `application/json`

---

## Token Refresh

When your access token expires, use the refresh token to get a new one:

```http
POST /api/auth/refresh
Authorization: Bearer <refresh_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000
  }
}
```

---

## Security Best Practices

1. **Never expose tokens in URLs** - Always use headers
2. **Store tokens securely** - Use httpOnly cookies or secure storage
3. **Implement token refresh** - Don't let users re-login unnecessarily
4. **Clear tokens on logout** - Remove from storage immediately
5. **Use HTTPS in production** - Never send tokens over unencrypted connections
6. **Implement CSRF protection** - When using cookies for token storage
7. **Monitor token usage** - Log and alert on suspicious activity

---

## Contact & Support

For issues or questions, please check the backend logs at:
- Development: Console output
- Production: Application log files

Check the Swagger UI for interactive API testing:
- URL: http://localhost:8080/swagger-ui.html

