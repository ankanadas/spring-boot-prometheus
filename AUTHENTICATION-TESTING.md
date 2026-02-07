# Authentication Testing Guide

## Test Users Created

After starting the application, two test users will be created:

### Admin User
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: ADMIN
- **Permissions**: Full access to all endpoints

### Regular User
- **Username**: `testuser`
- **Password**: `password123`
- **Role**: USER
- **Permissions**: View any profile, update own profile only

## How to Test Authentication

### 1. Using curl (Command Line)

**Test public endpoint (no auth required):**
```bash
curl http://localhost:8080/api/users/search?query=test
```

**Test protected endpoint with admin credentials:**
```bash
curl -u admin:admin123 http://localhost:8080/api/users
```

**Test protected endpoint with regular user credentials:**
```bash
curl -u testuser:password123 http://localhost:8080/api/users
```

**Test with invalid credentials (should return 401):**
```bash
curl -u admin:wrongpassword http://localhost:8080/api/users
```

**Test without credentials (should return 401):**
```bash
curl http://localhost:8080/api/users
```

### 2. Using Postman

1. Open Postman
2. Create a new request to `http://localhost:8080/api/users`
3. Go to the **Authorization** tab
4. Select **Basic Auth** from the Type dropdown
5. Enter credentials:
   - Username: `admin`
   - Password: `admin123`
6. Send the request

### 3. Using Browser

When you visit a protected endpoint like `http://localhost:8080/api/users`, your browser will show a login dialog:
- Enter username: `admin`
- Enter password: `admin123`

### 4. Using HTTPie (if installed)

**With admin credentials:**
```bash
http -a admin:admin123 http://localhost:8080/api/users
```

**With regular user credentials:**
```bash
http -a testuser:password123 http://localhost:8080/api/users
```

## Expected Behavior

### Currently Implemented (Tasks 1-3):
- ✅ Public search endpoint works without authentication
- ✅ All other endpoints require authentication (return 401 without credentials)
- ✅ Valid credentials authenticate successfully
- ✅ Invalid credentials return 401 Unauthorized
- ✅ Swagger UI and Actuator endpoints are public

### Not Yet Implemented (Task 4+):
- ❌ Role-based authorization (ADMIN vs USER permissions)
- ❌ Ownership validation (USER can only update own profile)
- ❌ Specific error messages for authorization failures

## Testing Checklist

- [ ] Start the application
- [ ] Check logs for "Created test admin user" and "Created test regular user"
- [ ] Test public search endpoint without auth
- [ ] Test protected endpoint with admin credentials (should work)
- [ ] Test protected endpoint with testuser credentials (should work)
- [ ] Test protected endpoint with wrong password (should return 401)
- [ ] Test protected endpoint without credentials (should return 401)
- [ ] Access Swagger UI at http://localhost:8080/swagger-ui.html (should work without auth)

## Troubleshooting

**If you get "User not found" error:**
- Check that the database columns (username, password, role) were created
- Check application logs for "Created test admin user" message
- Verify the database schema was updated by Hibernate

**If you get "Access Denied" errors:**
- This is expected for now - role-based authorization will be implemented in Task 4

**If authentication doesn't work at all:**
- Check that Spring Security dependency was added to pom.xml
- Verify SecurityConfig bean is loaded (check logs)
- Ensure CustomUserDetailsService is registered as a Spring bean
