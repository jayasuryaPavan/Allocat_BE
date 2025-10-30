# 404 Error Handling Implementation

## Problem
Previously, when clients made requests to non-existent API endpoints, Spring Security would return a **401 Unauthorized** error instead of a proper **404 Not Found** error. This was confusing because it suggested an authentication problem rather than a missing endpoint.

## Solution
Implemented proper error handling to ensure that 404 errors are returned for non-existent endpoints, regardless of authentication status.

---

## Changes Made

### 1. Custom Error Controller
**File:** `allocat-api/src/main/java/com/allocat/api/controller/CustomErrorController.java`

Created a custom error controller that intercepts Spring Boot's default error handling and returns proper JSON responses for various HTTP status codes.

**Features:**
- Returns JSON response for 404 Not Found
- Returns JSON response for 403 Forbidden
- Returns JSON response for 401 Unauthorized
- Returns JSON response for 405 Method Not Allowed
- Returns JSON response for 500 Internal Server Error
- Provides descriptive error messages

**Example 404 Response:**
```json
{
  "success": false,
  "message": "API endpoint does not exist: /api/nonexistent",
  "data": null
}
```

### 2. Global Exception Handler
**File:** `allocat-api/src/main/java/com/allocat/api/exception/GlobalExceptionHandler.java`

Created a global exception handler using `@RestControllerAdvice` to catch and handle various exceptions before they reach the default error controller.

**Handles:**
- `NoHandlerFoundException` - Returns 404 with custom message
- `MethodArgumentNotValidException` - Returns 400 with validation errors
- `IllegalArgumentException` - Returns 400 with custom message
- `AccessDeniedException` - Returns 403 with permission message
- `AuthenticationException` - Returns 401 with auth message
- `BadCredentialsException` - Returns 401 for invalid credentials
- `Exception` - Returns 500 for unexpected errors

### 3. Application Configuration
**File:** `allocat-core/src/main/resources/application.yml`

Added Spring MVC configuration to throw exceptions for missing handlers:

```yaml
spring:
  mvc:
    throw-exception-if-no-handler-found: true
  web:
    resources:
      add-mappings: false
```

**What this does:**
- `throw-exception-if-no-handler-found: true` - Throws `NoHandlerFoundException` when no controller method is found
- `add-mappings: false` - Disables default static resource mappings to ensure our handler catches all requests

### 4. JWT Filter Enhancement
**File:** `allocat-security/src/main/java/com/allocat/security/jwt/JwtAuthenticationFilter.java`

Added an attribute to the request to help distinguish between missing authentication and missing endpoints:

```java
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    request.setAttribute("missing-auth-header", true);
    filterChain.doFilter(request, response);
    return;
}
```

---

## Error Response Examples

### 404 - API Endpoint Does Not Exist
```http
GET /api/nonexistent
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": false,
  "message": "API endpoint does not exist: /api/nonexistent",
  "data": null
}
```

**Status Code:** 404 Not Found

---

### 401 - Authentication Required
```http
GET /api/users
# No Authorization header
```

**Response:**
```json
{
  "success": false,
  "message": "Authentication required. Please provide a valid token.",
  "data": null
}
```

**Status Code:** 401 Unauthorized

---

### 403 - Forbidden
```http
GET /api/stores
Authorization: Bearer <admin_token>
# ADMIN trying to access SUPER_ADMIN only endpoint
```

**Response:**
```json
{
  "success": false,
  "message": "You don't have permission to access this resource",
  "data": null
}
```

**Status Code:** 403 Forbidden

---

### 400 - Validation Error
```http
POST /api/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "",
  "email": "invalid-email"
}
```

**Response:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username cannot be blank",
    "email": "Email must be valid"
  }
}
```

**Status Code:** 400 Bad Request

---

### 405 - Method Not Allowed
```http
DELETE /api/users/roles
Authorization: Bearer <token>
# Endpoint exists but DELETE method not supported
```

**Response:**
```json
{
  "success": false,
  "message": "HTTP method not allowed for this endpoint",
  "data": null
}
```

**Status Code:** 405 Method Not Allowed

---

### 500 - Internal Server Error
```http
GET /api/users
Authorization: Bearer <token>
# Database connection error occurs
```

**Response:**
```json
{
  "success": false,
  "message": "An unexpected error occurred: Connection refused",
  "data": null
}
```

**Status Code:** 500 Internal Server Error

---

## How It Works

### Request Flow

1. **Client makes request** → `/api/nonexistent`

2. **JWT Filter** → Processes authentication (if header present)

3. **Spring MVC** → Tries to find a controller method

4. **No handler found** → Throws `NoHandlerFoundException`

5. **Global Exception Handler** → Catches exception, returns 404 JSON response

6. **Client receives** → 404 with proper error message

### Request Flow (Authenticated Endpoint)

1. **Client makes request** → `/api/users` (no auth header)

2. **JWT Filter** → Sets attribute `missing-auth-header = true`, continues

3. **Spring Security** → Realizes user is not authenticated

4. **Authentication Entry Point** → Returns 401 JSON response

5. **Client receives** → 401 with authentication required message

---

## Testing

### Test 404 - Non-existent Endpoint
```bash
curl -X GET http://localhost:8080/api/nonexistent \
  -H "Authorization: Bearer <valid_token>"
```

**Expected:**
- Status: 404
- Message: "API endpoint does not exist: /api/nonexistent"

### Test 404 - Non-existent Endpoint (No Auth)
```bash
curl -X GET http://localhost:8080/api/nonexistent
```

**Expected:**
- Status: 404 (not 401!)
- Message: "API endpoint does not exist: /api/nonexistent"

### Test 401 - Missing Auth
```bash
curl -X GET http://localhost:8080/api/users
```

**Expected:**
- Status: 401
- Message: "Authentication required. Please provide a valid token."

### Test 403 - Insufficient Permissions
```bash
# Login as ADMIN
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"pass"}' \
  | jq -r '.data.accessToken')

# Try to access SUPER_ADMIN only endpoint
curl -X GET http://localhost:8080/api/stores \
  -H "Authorization: Bearer $TOKEN"
```

**Expected:**
- Status: 403
- Message: "You don't have permission to access this resource"

### Test 405 - Wrong Method
```bash
curl -X DELETE http://localhost:8080/api/users/roles \
  -H "Authorization: Bearer <token>"
```

**Expected:**
- Status: 405
- Message: "HTTP method not allowed for this endpoint"

---

## Benefits

### 1. Clear Error Messages
Clients now receive descriptive JSON error messages that clearly indicate what went wrong.

### 2. Proper HTTP Status Codes
Each error type returns the appropriate HTTP status code:
- 400: Bad Request (validation errors)
- 401: Unauthorized (missing/invalid auth)
- 403: Forbidden (insufficient permissions)
- 404: Not Found (endpoint doesn't exist)
- 405: Method Not Allowed (wrong HTTP method)
- 500: Internal Server Error (unexpected errors)

### 3. Consistent Response Format
All errors use the same `ApiResponse` format:
```json
{
  "success": false,
  "message": "Error description",
  "data": null or error details
}
```

### 4. Better Developer Experience
Frontend developers can now:
- Easily distinguish between authentication errors and missing endpoints
- Parse error responses consistently
- Display appropriate error messages to users
- Debug API integration issues faster

---

## Files Created

1. `allocat-api/src/main/java/com/allocat/api/controller/CustomErrorController.java`
2. `allocat-api/src/main/java/com/allocat/api/exception/GlobalExceptionHandler.java`
3. `404-ERROR-HANDLING.md` (this file)

## Files Modified

1. `allocat-core/src/main/resources/application.yml`
   - Added `spring.mvc.throw-exception-if-no-handler-found: true`
   - Added `spring.web.resources.add-mappings: false`

2. `allocat-security/src/main/java/com/allocat/security/jwt/JwtAuthenticationFilter.java`
   - Added request attribute for missing auth header

---

## Migration

No database changes required. Simply rebuild and restart the application:

```bash
cd allocat-erp-backend
mvn clean install
mvn spring-boot:run -pl allocat-api
```

---

## Frontend Integration

### Example: Handling Errors

```typescript
const makeApiCall = async (url: string) => {
  try {
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    const data = await response.json();
    
    if (!data.success) {
      switch (response.status) {
        case 404:
          console.error('Endpoint does not exist:', data.message);
          showToast('Invalid API endpoint', 'error');
          break;
        case 401:
          console.error('Authentication required:', data.message);
          redirectToLogin();
          break;
        case 403:
          console.error('Access denied:', data.message);
          showToast('You don\'t have permission', 'error');
          break;
        case 400:
          console.error('Validation error:', data.data);
          showValidationErrors(data.data);
          break;
        default:
          console.error('Error:', data.message);
          showToast(data.message, 'error');
      }
    }
    
    return data;
  } catch (error) {
    console.error('Network error:', error);
    showToast('Network error occurred', 'error');
  }
};
```

---

## Summary

✅ **404 errors now properly returned** for non-existent endpoints  
✅ **Clear distinction** between 401 (auth required) and 404 (endpoint missing)  
✅ **Consistent JSON format** for all error responses  
✅ **Better error messages** for debugging and user feedback  
✅ **Global exception handling** for consistent error responses  
✅ **No breaking changes** to existing API responses  

**Status:** Ready for testing! 🎉

