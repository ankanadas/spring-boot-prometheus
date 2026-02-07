-- Migration to refactor authentication to normalized structure
-- This creates separate tables for credentials and roles with many-to-many relationship

-- Create roles table
CREATE TABLE IF NOT EXISTS userschema.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Create user_credentials table (1:1 with users)
CREATE TABLE IF NOT EXISTS userschema.user_credentials (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_id) REFERENCES userschema.users(id) ON DELETE CASCADE
);

-- Create user_roles join table (many-to-many)
CREATE TABLE IF NOT EXISTS userschema.user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES userschema.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES userschema.roles(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_credentials_username ON userschema.user_credentials(username);
CREATE INDEX IF NOT EXISTS idx_user_credentials_user_id ON userschema.user_credentials(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON userschema.user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON userschema.user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_roles_name ON userschema.roles(name);

-- Insert default roles
INSERT INTO userschema.roles (name, description) VALUES 
    ('ROLE_USER', 'Standard user with basic permissions'),
    ('ROLE_ADMIN', 'Administrator with full permissions')
ON CONFLICT (name) DO NOTHING;

-- Migrate existing users with username/password/role to new structure
-- Note: This will be handled by DataInitializer.migrateExistingUsers() at runtime
-- because we need to use the PasswordEncoder for existing passwords

-- Remove old columns from users table (if they exist)
-- We'll keep them for now to allow gradual migration, then remove in a future migration
-- ALTER TABLE userschema.users DROP COLUMN IF EXISTS username;
-- ALTER TABLE userschema.users DROP COLUMN IF EXISTS password;
-- ALTER TABLE userschema.users DROP COLUMN IF EXISTS role;
