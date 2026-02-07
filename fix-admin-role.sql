-- Fix admin user role
-- First, find the admin user and role IDs
DO $$
DECLARE
    admin_user_id BIGINT;
    admin_role_id BIGINT;
    user_role_id BIGINT;
BEGIN
    -- Get the admin user ID
    SELECT id INTO admin_user_id FROM userschema.users WHERE email = 'admin@example.com';
    
    -- Get the ROLE_ADMIN ID
    SELECT id INTO admin_role_id FROM userschema.roles WHERE name = 'ROLE_ADMIN';
    
    -- Get the ROLE_USER ID
    SELECT id INTO user_role_id FROM userschema.roles WHERE name = 'ROLE_USER';
    
    -- Delete the ROLE_USER assignment for admin
    DELETE FROM userschema.user_roles WHERE user_id = admin_user_id AND role_id = user_role_id;
    
    -- Add ROLE_ADMIN assignment for admin (if not exists)
    INSERT INTO userschema.user_roles (user_id, role_id)
    VALUES (admin_user_id, admin_role_id)
    ON CONFLICT DO NOTHING;
    
    RAISE NOTICE 'Admin user role updated successfully';
END $$;

-- Verify the change
SELECT u.id, u.name, u.email, uc.username, r.name as role 
FROM userschema.users u 
LEFT JOIN userschema.user_credentials uc ON u.id = uc.user_id 
LEFT JOIN userschema.user_roles ur ON u.id = ur.user_id 
LEFT JOIN userschema.roles r ON ur.role_id = r.id 
WHERE uc.username = 'admin';
