# Quick Reference - Store and User Management

## Quick Start

### 1. Setup New Store (SUPER_ADMIN)

```bash
# Create store
POST /api/stores
{
  "code": "ST001",
  "name": "Main Street Store",
  "accessCode": "SecurePass123!",
  "city": "New York",
  "state": "NY"
}
```

### 2. Create Store Admin (SUPER_ADMIN)

```bash
# Create ADMIN for the store
POST /api/users
{
  "username": "store_admin",
  "email": "admin@store.com",
  "firstName": "Store",
  "lastName": "Admin",
  "roleName": "ADMIN",
  "storeId": 1  # Must match the store created above
}
```

### 3. ADMIN Creates Staff

```bash
# Login as ADMIN first, then:
POST /api/users
{
  "username": "staff_member",
  "email": "staff@store.com",
  "firstName": "Staff",
  "lastName": "Member",
  "roleName": "SALES_STAFF"
  # storeId automatically set to ADMIN's store
}
```

---

## Role Matrix

| Action | SUPER_ADMIN | ADMIN | Others |
|--------|-------------|-------|--------|
| Create Store | ✅ | ❌ | ❌ |
| Update Store | ✅ (with access code) | ❌ | ❌ |
| Create ADMIN | ✅ | ❌ | ❌ |
| Create Other Users | ✅ (any store) | ✅ (own store only) | ❌ |
| View All Users | ✅ | ❌ (own store only) | ❌ |
| Manage Inventory | ✅ (all stores) | ✅ (own store) | Role-dependent |

---

## Key Endpoints

### Store Management
```
POST   /api/stores                    Create store
GET    /api/stores                    List all stores
GET    /api/stores/{id}               Get store details
PUT    /api/stores/{id}               Update store (requires access code)
DELETE /api/stores/{id}               Delete store (requires access code)
POST   /api/stores/validate-access   Validate access code
```

### User Management
```
GET    /api/users                     List users (filtered by role)
POST   /api/users                     Create user (role-validated)
GET    /api/users/roles               List all roles
```

---

## Common Payloads

### Create Store
```json
{
  "code": "ST001",
  "name": "Store Name",
  "accessCode": "SecureCode123!",
  "address": "123 Main St",
  "city": "City",
  "state": "State",
  "country": "Country",
  "postalCode": "12345",
  "phone": "+1234567890",
  "email": "store@example.com"
}
```

### Update Store
```json
{
  "accessCode": "SecureCode123!",  // Required!
  "name": "Updated Name",
  "phone": "+9876543210",
  "isActive": true
}
```

### Create ADMIN User
```json
{
  "username": "admin_user",
  "email": "admin@store.com",
  "password": "Optional123!",
  "firstName": "First",
  "lastName": "Last",
  "roleName": "ADMIN",
  "storeId": 1  // Required for ADMIN
}
```

### Create Staff User (as ADMIN)
```json
{
  "username": "staff_user",
  "email": "staff@store.com",
  "firstName": "First",
  "lastName": "Last",
  "roleName": "SALES_STAFF"
  // storeId auto-set
}
```

### Validate Access Code
```json
{
  "storeId": 1,
  "accessCode": "SecureCode123!"
}
```

---

## Validation Rules

### Store Creation
- ✅ `code`: Required, unique, max 20 chars
- ✅ `name`: Required, max 100 chars
- ✅ `accessCode`: Required, min 6 chars, max 50 chars
- ✅ All other fields optional

### User Creation
- ✅ `username`: Required, unique
- ✅ `email`: Required, unique
- ✅ `password`: Optional (auto-generated if not provided)
- ✅ `roleName`: Optional (defaults to "VIEWER")
- ✅ `storeId`: Required if roleName is "ADMIN"
- ✅ SUPER_ADMIN can assign any role except SUPER_ADMIN
- ✅ ADMIN can assign any role except ADMIN and SUPER_ADMIN

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "ADMIN role requires a store assignment"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Invalid access code"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Only SUPER_ADMIN can create ADMIN users"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Store not found with ID: 1"
}
```

---

## Testing Checklist

- [ ] SUPER_ADMIN can create stores
- [ ] Store access code is hashed
- [ ] Store requires access code to update
- [ ] SUPER_ADMIN can create ADMIN users
- [ ] ADMIN must have storeId
- [ ] ADMIN can create users only for their store
- [ ] ADMIN cannot create other ADMIN users
- [ ] GET /api/users filters by store for ADMIN
- [ ] GET /api/users shows all for SUPER_ADMIN
- [ ] Invalid access code returns 401
- [ ] Duplicate store code returns 400

---

## Database Quick Reference

### Check Stores
```sql
SELECT id, code, name, is_active FROM stores;
```

### Check Users by Store
```sql
SELECT u.id, u.username, u.email, r.name as role, u.store_id 
FROM users u 
JOIN roles r ON u.role_id = r.id
WHERE u.store_id = 1;
```

### Verify Access Code Hashing
```sql
SELECT code, access_code FROM stores;
-- access_code should look like: $2a$12$...
```

### Count Users per Store
```sql
SELECT s.name, COUNT(u.id) as user_count
FROM stores s
LEFT JOIN users u ON s.id = u.store_id
GROUP BY s.id, s.name;
```

---

## Troubleshooting

### Issue: "Store not found"
**Solution**: Ensure store ID exists and is active

### Issue: "Invalid access code"
**Solution**: Verify the access code matches what was set during creation

### Issue: "Only SUPER_ADMIN can create ADMIN users"
**Solution**: Login as SUPER_ADMIN or assign a different role

### Issue: "ADMIN role requires a store assignment"
**Solution**: Add `storeId` to the request payload

### Issue: "ADMIN can only create users for their assigned store"
**Solution**: Remove `storeId` from request (auto-set) or login as SUPER_ADMIN

---

## Quick Commands

### Rebuild & Run
```bash
cd allocat-erp-backend
mvn clean install -DskipTests
mvn spring-boot:run -pl allocat-api
```

### Run Migration Only
```bash
cd allocat-erp-backend/allocat-core
mvn flyway:migrate
```

### Check Flyway Status
```bash
cd allocat-erp-backend/allocat-core
mvn flyway:info
```

---

## Next Steps

1. ✅ Run database migration (`mvn flyway:migrate`)
2. ✅ Rebuild application (`mvn clean install`)
3. ✅ Start application
4. ✅ Login as SUPER_ADMIN
5. ✅ Create first store
6. ✅ Create ADMIN for the store
7. ✅ Test ADMIN creating users
8. ✅ Build frontend UI for store/user management

---

## Support

- **Full Guide**: See `STORE-AND-USER-MANAGEMENT-GUIDE.md`
- **Implementation Details**: See `IMPLEMENTATION-SUMMARY.md`
- **Authentication**: See `API-AUTHENTICATION-GUIDE.md`
- **Troubleshooting**: See `DEBUGGING-403-ERRORS.md`

