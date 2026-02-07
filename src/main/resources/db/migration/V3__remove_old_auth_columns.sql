-- Migration to remove old authentication columns from users table
-- These columns are now in user_credentials and user_roles tables

-- Drop old authentication columns from users table
ALTER TABLE userschema.users DROP COLUMN IF EXISTS username;
ALTER TABLE userschema.users DROP COLUMN IF EXISTS password;
ALTER TABLE userschema.users DROP COLUMN IF EXISTS role;
