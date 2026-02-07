-- Migration script for adding authentication fields to users table
-- Note: Hibernate ddl-auto=update will create the columns automatically
-- This script creates the indexes for performance

-- Create index on username for fast authentication lookups
CREATE INDEX IF NOT EXISTS idx_users_username ON userschema.users(username);

-- Create index on role for role-based queries
CREATE INDEX IF NOT EXISTS idx_users_role ON userschema.users(role);
