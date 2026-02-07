-- Create admin user manually
-- Connect to your database first: psql -U your_username -d userdb

-- Check if admin user already exists
SELECT id, username, email, role FROM userschema.users WHERE username = 'admin';

-- If admin doesn't exist, insert it
-- Note: This password hash is for "admin123" using BCrypt
INSERT INTO userschema.users (username, password, name, email, department_id, role)
SELECT 
    'admin',
    '$2a$10$rN8qvM7VZ5FqKx5x5x5x5eO5x5x5x5x5x5x5x5x5x5x5x5x5x5x5x',  -- This is a placeholder
    'System Administrator',
    'admin@example.com',
    (SELECT id FROM userschema.departments WHERE name = 'Leadership' LIMIT 1),
    'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM userschema.users WHERE username = 'admin'
);

-- Verify the admin user was created
SELECT id, username, email, role FROM userschema.users WHERE username = 'admin';
