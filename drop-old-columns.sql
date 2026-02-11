-- Manual script to drop old authentication columns from users table
-- These columns are no longer used - data is now in user_credentials and user_roles tables

-- Connect to your database first:
-- psql -U ankanadas -d metrics_demo

-- Drop old columns
ALTER TABLE userschema.users DROP COLUMN IF EXISTS username;
ALTER TABLE userschema.users DROP COLUMN IF EXISTS password;
ALTER TABLE userschema.users DROP COLUMN IF EXISTS role;

-- Verify the columns are dropped
\d userschema.users

-- You should now see only these columns:
-- id, name, email, department_id
