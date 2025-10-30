# Debugging 403 Forbidden Errors

## What Changed

I've made the following improvements to help diagnose and fix your 403 errors:

### 1. Enhanced Logging in JWT Filter
The `JwtAuthenticationFilter` now logs detailed information about:
- Every request being processed
- Authorization header presence
- Token extraction
- Username extraction
- Token validation results
- Authentication success/failure

Location: `allocat-security/src/main/java/com/allocat/security/jwt/JwtAuthenticationFilter.java`

### 2. Custom Error Handlers
Added two new handlers for better error responses:

**JwtAuthenticationEntryPoint** - Handles 401 Unauthorized errors
- Returns JSON with detailed error message
- Logs why authentication failed

**JwtAccessDeniedHandler** - Handles 403 Forbidden errors
- Returns JSON with detailed error message
- Logs access denial reasons

These are now configured in `SecurityConfig.java`

### 3. Improved Security Configuration
Updated `SecurityConfig` to use the custom error handlers, providing better error messages to the frontend.

---

## How to Debug

### Step 1: Rebuild the Application

Option A - Using IDE (IntelliJ IDEA/Eclipse):
1. Right-click on `allocat-erp-backend` root folder
2. Select "Maven" → "Reload Project"
3. Select "Maven" → "Clean"
4. Select "Maven" → "Install"
5. Run the application from `AllocatApplication.java`

Option B - Using Command Line:
```bash
cd "C:\Work Space\Allocat\Backend\allocat-erp-backend"

# If you have Maven installed
mvn clean install -DskipTests

# If using Maven Wrapper (if it exists)
./mvnw clean install -DskipTests

# Run the application
mvn spring-boot:run -pl allocat-api
```

### Step 2: Check the Logs

When you make a request to `/api/inventory/current`, you should see logs like:

**Successful Authentication:**
```
2025-10-23 10:30:00 - Processing request: GET /api/inventory/current
2025-10-23 10:30:00 - Authorization header: Bearer ***
2025-10-23 10:30:00 - Extracted JWT token (first 20 chars): eyJhbGciOiJIUzI1NiJ9...
2025-10-23 10:30:00 - Extracted username from token: suryaakula
2025-10-23 10:30:00 - Token validated successfully. User: suryaakula, Role: SUPER_ADMIN
2025-10-23 10:30:00 - Successfully authenticated user: suryaakula with role: SUPER_ADMIN
```

**Failed Authentication (No Token):**
```
2025-10-23 10:30:00 - Processing request: GET /api/inventory/current
2025-10-23 10:30:00 - Authorization header: null
2025-10-23 10:30:00 - No valid Authorization header found for: GET /api/inventory/current
2025-10-23 10:30:00 - Unauthorized error: Full authentication is required to access this resource for URI: /api/inventory/current
```

**Failed Authentication (Invalid Token):**
```
2025-10-23 10:30:00 - Processing request: GET /api/inventory/current
2025-10-23 10:30:00 - Authorization header: Bearer ***
2025-10-23 10:30:00 - Cannot set user authentication: JWT signature does not match - io.jsonwebtoken.security.SignatureException
```

### Step 3: Test from Frontend

**JavaScript Console Test:**
```javascript
// Get a fresh token first
fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'suryaakula',
    password: 'your_password'
  })
})
.then(response => response.json())
.then(data => {
  console.log('Login response:', data);
  const token = data.data.accessToken;
  
  // Now test the protected endpoint
  return fetch('http://localhost:8080/api/inventory/current', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
})
.then(response => response.json())
.then(data => console.log('Inventory response:', data))
.catch(error => console.error('Error:', error));
```

### Step 4: Verify Token Format

Open browser developer tools and check the Network tab:

1. Click on the request to `/api/inventory/current`
2. Go to "Headers" tab
3. Look for "Request Headers" section
4. Verify "Authorization" header looks like:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiU1VQRVJfQURNSU4iLCJ1c2VySWQiOjE...
   ```

**Common Issues:**
- Missing "Bearer " prefix
- Extra quotes around the token: `"Bearer token"` ❌ should be `Bearer token` ✅
- Token stored with Bearer prefix in storage and added again in header: `Bearer Bearer token` ❌

---

## Common 403 Error Causes

### 1. Token Not Being Sent
**Symptom:** Backend logs show "No valid Authorization header"

**Solutions:**
- Check if token is actually in localStorage/sessionStorage
- Verify your HTTP client is configured to send headers
- Check if using an axios instance with proper interceptors

### 2. Token Format Wrong
**Symptom:** Backend logs show "No valid Authorization header" even though header is sent

**Solutions:**
- Ensure format is exactly: `Authorization: Bearer <token>`
- No extra quotes, no extra spaces
- "Bearer" is capitalized

### 3. Token Expired
**Symptom:** Backend logs show "Token validation failed"

**Solutions:**
- Check token expiration time (default: 1 hour)
- Implement token refresh logic
- Re-login to get a fresh token

### 4. JWT Secret Mismatch
**Symptom:** Backend logs show "JWT signature does not match"

**Solutions:**
- Ensure `jwt.secret` in `application.yml` hasn't changed since token was generated
- Token generated with one secret can't be validated with a different secret
- Re-login to get a token with current secret

### 5. CORS Preflight Failure
**Symptom:** Browser console shows CORS error, backend shows OPTIONS request

**Solutions:**
- Verify your origin is in `app.cors.allowed-origins`
- Check if browser is sending preflight OPTIONS request
- Ensure CORS configuration allows Authorization header

### 6. Token Not Parsed Correctly
**Symptom:** Backend logs show "Cannot set user authentication"

**Solutions:**
- Check if token is being truncated
- Verify token is not corrupted during storage/retrieval
- Test with a freshly generated token

---

## Quick Checklist

Before reporting a bug, verify:

- [ ] Application has been rebuilt with latest changes
- [ ] Fresh login performed to get new token
- [ ] Token is stored correctly in frontend
- [ ] Authorization header is being sent in format: `Bearer <token>`
- [ ] Content-Type header is set to `application/json`
- [ ] Backend logs are being monitored during request
- [ ] Token has not expired (check with jwt.io)
- [ ] Browser console shows no CORS errors
- [ ] Request is going to correct URL (http://localhost:8080)
- [ ] User has appropriate role/permissions

---

## Next Steps

1. **Rebuild the backend application** with the updated logging
2. **Login again** to get a fresh token
3. **Make a request** to `/api/inventory/current` from your frontend
4. **Check the backend console logs** for detailed authentication flow
5. **Share the logs** if the issue persists - they will show exactly where authentication is failing

---

## Expected Error Response Format

With the new error handlers, 401 and 403 errors will return JSON:

**401 Unauthorized (No/Invalid Token):**
```json
{
  "success": false,
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required. Please provide a valid token.",
  "path": "/api/inventory/current"
}
```

**403 Forbidden (Valid Token, Insufficient Permissions):**
```json
{
  "success": false,
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource.",
  "path": "/api/inventory/current"
}
```

---

## Testing Commands

### Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"suryaakula","password":"your_password"}' \
  | json_pp
```

### Test Protected Endpoint (replace TOKEN with actual token)
```bash
TOKEN="your_actual_token_here"

curl -X GET http://localhost:8080/api/inventory/current \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -v
```

The `-v` flag will show full request/response headers for debugging.

---

## Additional Resources

- See `API-AUTHENTICATION-GUIDE.md` for complete API documentation
- See `LOGIN-API-DOCUMENTATION.md` for authentication endpoint details
- Use Swagger UI at http://localhost:8080/swagger-ui.html for interactive testing

