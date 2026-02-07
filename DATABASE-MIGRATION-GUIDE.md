# Database Migration Guide

## Issue
When adding new NOT NULL columns to an existing table with data, Hibernate's `ddl-auto=update` fails because it can't add NOT NULL columns to rows that already exist.

## Solution Applied

### 1. Made Columns Nullable
Changed the User entity fields to be nullable initially:
- `username` - nullable, unique
- `password` - nullable  
- `role` - nullable

### 2. Automatic Migration
The DataInitializer now automatically migrates existing users:
- Generates username from email (e.g., "john.doe@example.com" → "john.doe")
- Sets default password: "password123" (BCrypt hashed)
- Assigns default role: USER
- Handles username conflicts with numeric suffixes

## Steps to Fix Your Database

### Option 1: Let Migration Handle It (Recommended)
1. **Restart the application** - it should start successfully now
2. The migration will run automatically and update existing users
3. Check logs for: "Migration complete: X users updated with authentication fields"
4. Test users will be created if database is empty

### Option 2: Fresh Start (If You Want Clean Data)
If you want to start fresh:

```sql
-- Connect to PostgreSQL
psql -U your_username -d userdb

-- Drop and recreate the schema
DROP SCHEMA IF EXISTS userschema CASCADE;
CREATE SCHEMA userschema;

-- Exit psql
\q
```

Then restart the application - it will create everything from scratch with test users.

## What Happens on Restart

1. **Migration runs first**: Existing users get username/password/role
2. **Check for data**: If database has users/departments, skip initialization
3. **If empty**: Create departments and test users

## Expected Log Output

```
Checking for users without authentication fields...
Found 15 users without authentication fields. Starting migration...
Migrated user: john.doe (email: john.doe@example.com)
Migrated user: jane.smith (email: jane.smith@example.com)
...
✅ Migration complete: 15 users updated with authentication fields
Database already contains 15 users and 11 departments. Skipping data initialization.
```

OR if database is empty:

```
Checking for users without authentication fields...
All users have authentication fields. No migration needed.
Database is empty. Initializing sample data...
Created 11 departments
✅ Created test admin user - username: admin, password: admin123
✅ Created test regular user - username: testuser, password: password123
✅ Sample data initialized successfully: 17 users and 11 departments created
```

## Verify Migration

After restart, check your database:

```sql
-- Connect to database
psql -U your_username -d userdb

-- Check users table
SELECT id, username, email, role FROM userschema.users;

-- You should see:
-- - All users have username, password, and role
-- - Test users: admin (ADMIN) and testuser (USER)
-- - Migrated users have role USER
```

## Test Authentication

After successful migration:

```bash
# Test with migrated user (username from email)
curl -u john.doe:password123 http://localhost:8080/api/users

# Test with admin
curl -u admin:admin123 http://localhost:8080/api/users

# Test with testuser
curl -u testuser:password123 http://localhost:8080/api/users
```

## Troubleshooting

**If migration fails:**
- Check application logs for specific error
- Verify PostgreSQL is running
- Check database connection in application.yml

**If you still see "NOT NULL" errors:**
- The columns might still be marked as NOT NULL in the database
- Run Option 2 (Fresh Start) to recreate the schema

**If test users aren't created:**
- They're only created if database is empty
- If you have existing data, use the migrated users instead
- Or drop the schema and restart for fresh data with test users
