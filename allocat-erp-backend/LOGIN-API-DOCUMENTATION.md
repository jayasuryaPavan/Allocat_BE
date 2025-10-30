# Login API Documentation

## Authentication Endpoints

### 1. Login
**Endpoint:** `POST /api/auth/login`

**Description:** Authenticate a user and receive JWT tokens

**Request Body:**
```json
{
  "username": "suryajaya",
  "password": "goodboy"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "userId": 1,
    "username": "suryajaya",
    "email": "jayasurya.pandu@gmail.com",
    "firstName": "Surya",
    "lastName": "Akula",
    "phone": "+1234567890",
    "roleId": 1,
    "role": "SUPER_ADMIN",
    "storeId": null,
    "isActive": true,
    "lastLoginAt": "2025-10-22T19:20:35",
    "permissions": [
      "users:create",
      "users:read",
      "users:update",
      "users:delete",
      "products:create",
      "products:read",
      ...
    ]
  },
  "error": null
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": "Invalid username or password"
}
```

### 2. Get Current User
**Endpoint:** `GET /api/auth/me`

**Description:** Get the currently authenticated user's full details

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User details retrieved successfully",
  "data": {
    "userId": 1,
    "username": "suryajaya",
    "email": "jayasurya.pandu@gmail.com",
    "firstName": "Surya",
    "lastName": "Akula",
    "phone": "+1234567890",
    "roleId": 1,
    "role": "SUPER_ADMIN",
    "storeId": null,
    "isActive": true,
    "lastLoginAt": "2025-10-22T19:20:35",
    "permissions": [
      "users:create",
      "users:read",
      ...
    ]
  },
  "error": null
}
```

### 3. Refresh Token
**Endpoint:** `POST /api/auth/refresh`

**Description:** Get a new access token using a refresh token

**Headers:**
```
Authorization: Bearer {refreshToken}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "username": "suryajaya"
  },
  "error": null
}
```

## Frontend Integration

### Step 1: Login
```javascript
async function login(username, password) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password })
  });
  
  const result = await response.json();
  
  if (result.success) {
    // Store tokens
    localStorage.setItem('accessToken', result.data.accessToken);
    localStorage.setItem('refreshToken', result.data.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      userId: result.data.userId,
      username: result.data.username,
      email: result.data.email,
      firstName: result.data.firstName,
      lastName: result.data.lastName,
      phone: result.data.phone,
      roleId: result.data.roleId,
      role: result.data.role,
      storeId: result.data.storeId,
      isActive: result.data.isActive,
      lastLoginAt: result.data.lastLoginAt,
      permissions: result.data.permissions
    }));
    return result.data;
  } else {
    throw new Error(result.error);
  }
}
```

### Step 2: Make Authenticated Requests
```javascript
async function fetchProtectedResource() {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch('http://localhost:8080/api/some-endpoint', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    }
  });
  
  return await response.json();
}
```

### Step 3: Handle Token Expiration
```javascript
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  
  const response = await fetch('http://localhost:8080/api/auth/refresh', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${refreshToken}`,
    }
  });
  
  const result = await response.json();
  
  if (result.success) {
    localStorage.setItem('accessToken', result.data.accessToken);
    return result.data.accessToken;
  } else {
    // Refresh token expired, redirect to login
    logout();
    window.location.href = '/login';
  }
}
```

### Step 4: Logout
```javascript
function logout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  window.location.href = '/login';
}
```

## Token Expiration
- **Access Token:** 1 hour (3600000 ms)
- **Refresh Token:** 7 days (604800000 ms)

## Security Notes
1. Always use HTTPS in production
2. Store tokens in localStorage or sessionStorage (or httpOnly cookies for better security)
3. Never log tokens
4. Implement token refresh logic before access token expires
5. Clear tokens on logout

## Swagger UI
Access the interactive API documentation at: `http://localhost:8080/swagger-ui.html`

