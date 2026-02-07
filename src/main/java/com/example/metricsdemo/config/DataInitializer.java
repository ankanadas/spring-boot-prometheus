package com.example.metricsdemo.config;

import com.example.metricsdemo.model.*;
import com.example.metricsdemo.repository.*;
import com.example.metricsdemo.service.UserSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserCredentialsRepository userCredentialsRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private UserSearchService userSearchService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // First, ensure roles exist
        initializeRoles();
        
        // Then, check if we need to migrate existing users without auth fields
        migrateExistingUsers();
        
        // Always ensure admin user exists (create if not present)
        createBootstrapAdmin();
        
        long existingUserCount = userRepository.count();
        long existingDeptCount = departmentRepository.count();
        
        if (existingUserCount > 1 || existingDeptCount > 0) {  // > 1 because admin already exists
            logger.info("Database already contains {} users and {} departments. Skipping data initialization.", 
                existingUserCount, existingDeptCount);
            return;
        }
        
        logger.info("Database is empty. Initializing sample data...");
        
        // Create departments first
        Department engineering = departmentRepository.save(new Department("Engineering", "Software development and technical teams"));
        Department marketing = departmentRepository.save(new Department("Marketing", "Marketing and brand management"));
        Department sales = departmentRepository.save(new Department("Sales", "Sales and business development"));
        Department hr = departmentRepository.save(new Department("HR", "Human resources and recruitment"));
        Department security = departmentRepository.save(new Department("Security", "Security and risk management"));
        Department photography = departmentRepository.save(new Department("Photography", "Photography and media"));
        Department legal = departmentRepository.save(new Department("Legal", "Legal and compliance"));
        Department journalism = departmentRepository.save(new Department("Journalism", "News and reporting"));
        Department leadership = departmentRepository.save(new Department("Leadership", "Executive leadership"));
        Department research = departmentRepository.save(new Department("Research", "Research and development"));
        Department operations = departmentRepository.save(new Department("Operations", "Operations and logistics"));
        
        logger.info("Created {} departments", departmentRepository.count());
        
        // Get roles
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        
        // Create a test regular user with credentials
        User testUser = new User("Test User", "test.user@example.com", engineering);
        testUser = userRepository.save(testUser);
        
        UserCredentials testCreds = new UserCredentials(testUser, "testuser", passwordEncoder.encode("password123"));
        userCredentialsRepository.save(testCreds);
        testUser.setCredentials(testCreds);
        testUser.addRole(userRole);
        testUser = userRepository.save(testUser);
        
        logger.info("✅ Created test regular user - username: testuser, password: password123");
        
        // Create sample users without credentials (old format)
        createSampleUser("John Doe", "john.doe@example.com", engineering, userRole);
        createSampleUser("Jane Smith", "jane.smith@example.com", marketing, userRole);
        createSampleUser("Bob Johnson", "bob.johnson@example.com", sales, userRole);
        createSampleUser("Alice Brown", "alice.brown@example.com", hr, userRole);
        createSampleUser("Charlie Wilson", "charlie.wilson@example.com", engineering, userRole);
        createSampleUser("Tony Stark", "tony.stark@example.com", engineering, userRole);
        createSampleUser("Bruce Wayne", "bruce.wayne@example.com", security, userRole);
        createSampleUser("Peter Parker", "peter.parker@example.com", photography, userRole);
        createSampleUser("Diana Prince", "diana.prince@example.com", legal, userRole);
        createSampleUser("Clark Kent", "clark.kent@example.com", journalism, userRole);
        createSampleUser("Natasha Romanoff", "natasha.romanoff@example.com", security, userRole);
        createSampleUser("Steve Rogers", "steve.rogers@example.com", leadership, userRole);
        createSampleUser("Wanda Maximoff", "wanda.maximoff@example.com", research, userRole);
        createSampleUser("Scott Lang", "scott.lang@example.com", engineering, userRole);
        createSampleUser("Carol Danvers", "carol.danvers@example.com", operations, userRole);

        long finalUserCount = userRepository.count();
        long finalDeptCount = departmentRepository.count();
        logger.info("✅ Sample data initialized successfully: {} users and {} departments created", 
            finalUserCount, finalDeptCount);
        
        // Index all users in Elasticsearch
        logger.info("Indexing users in Elasticsearch...");
        userSearchService.reindexAll(userRepository.findAll());
        logger.info("✅ Elasticsearch indexing complete");
    }
    
    private void initializeRoles() {
        logger.info("Initializing roles...");
        
        if (!roleRepository.existsByName("ROLE_USER")) {
            Role userRole = new Role("ROLE_USER", "Standard user with basic permissions");
            roleRepository.save(userRole);
            logger.info("Created ROLE_USER");
        }
        
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            Role adminRole = new Role("ROLE_ADMIN", "Administrator with full permissions");
            roleRepository.save(adminRole);
            logger.info("Created ROLE_ADMIN");
        }
        
        logger.info("✅ Roles initialized");
    }
    
    private void migrateExistingUsers() {
        logger.info("Checking for users without authentication fields...");
        
        var usersWithoutAuth = userRepository.findAll().stream()
            .filter(u -> u.getCredentials() == null || u.getUserRoles().isEmpty())
            .toList();
        
        if (usersWithoutAuth.isEmpty()) {
            logger.info("All users have authentication fields. No migration needed.");
            return;
        }
        
        logger.info("Found {} users without authentication fields. Starting migration...", usersWithoutAuth.size());
        
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
        
        for (User user : usersWithoutAuth) {
            if (user.getCredentials() == null) {
                // Generate username from email
                String baseUsername = user.getEmail().split("@")[0];
                String username = generateUniqueUsername(baseUsername);
                
                // Create credentials
                UserCredentials credentials = new UserCredentials(
                    user,
                    username,
                    passwordEncoder.encode("password123")
                );
                userCredentialsRepository.save(credentials);
                user.setCredentials(credentials);
                
                logger.info("Created credentials for user: {} (email: {})", username, user.getEmail());
            }
            
            if (user.getUserRoles().isEmpty()) {
                // Check if this is the admin user (by email or name)
                if (user.getEmail().equals("admin@example.com") || 
                    user.getName().equals("System Administrator")) {
                    user.addRole(adminRole);
                    logger.info("Assigned ROLE_ADMIN to admin user: {}", user.getEmail());
                } else {
                    user.addRole(userRole);
                    logger.info("Assigned ROLE_USER to user: {}", user.getEmail());
                }
            }
            
            userRepository.save(user);
        }
        
        logger.info("✅ Migration complete: {} users updated with authentication fields", usersWithoutAuth.size());
    }
    
    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int suffix = 1;
        
        while (userCredentialsRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }
        
        return username;
    }
    
    private void createBootstrapAdmin() {
        // Check if admin user already exists
        if (userCredentialsRepository.existsByUsername("admin")) {
            logger.info("Admin user already exists. Verifying admin role and password...");
            
            // Get the admin user and ensure they have ROLE_ADMIN
            UserCredentials adminCreds = userCredentialsRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin credentials not found"));
            User adminUser = adminCreds.getUser();
            
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
            
            // Check if admin has ROLE_ADMIN
            boolean hasAdminRole = adminUser.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName().equals("ROLE_ADMIN"));
            
            if (!hasAdminRole) {
                logger.info("Admin user exists but doesn't have ROLE_ADMIN. Fixing...");
                
                // Remove all existing roles
                adminUser.getUserRoles().clear();
                userRoleRepository.deleteAll(userRoleRepository.findByUserId(adminUser.getId()));
                
                // Add ROLE_ADMIN
                adminUser.addRole(adminRole);
                userRepository.save(adminUser);
                
                logger.info("✅ Admin role fixed - username: admin now has ROLE_ADMIN");
            } else {
                logger.info("Admin user already has ROLE_ADMIN.");
            }
            
            // Always ensure password is set to admin123
            String correctPassword = passwordEncoder.encode("admin123");
            if (!adminCreds.getPassword().equals(correctPassword)) {
                logger.info("Updating admin password to admin123...");
                adminCreds.setPassword(correctPassword);
                userCredentialsRepository.save(adminCreds);
                logger.info("✅ Admin password updated to admin123");
            }
            
            return;
        }
        
        logger.info("Creating bootstrap admin user...");
        
        // Get or create Leadership department
        Department leadership = departmentRepository.findAll().stream()
            .filter(d -> "Leadership".equals(d.getName()))
            .findFirst()
            .orElseGet(() -> {
                logger.info("Leadership department not found, creating it...");
                return departmentRepository.save(new Department("Leadership", "Executive leadership"));
            });
        
        // Get admin role
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
        
        // Create admin user
        User adminUser = new User("System Administrator", "admin@example.com", leadership);
        adminUser = userRepository.save(adminUser);
        
        // Create credentials
        UserCredentials adminCreds = new UserCredentials(
            adminUser,
            "admin",
            passwordEncoder.encode("admin123")
        );
        userCredentialsRepository.save(adminCreds);
        adminUser.setCredentials(adminCreds);
        
        // Assign admin role
        adminUser.addRole(adminRole);
        adminUser = userRepository.save(adminUser);
        
        // Index in Elasticsearch
        userSearchService.indexUser(adminUser);
        
        logger.info("✅ Bootstrap admin created - username: admin, password: admin123, role: ROLE_ADMIN");
    }
    
    private void createSampleUser(String name, String email, Department department, Role role) {
        User user = new User(name, email, department);
        user = userRepository.save(user);
        
        // Generate username and credentials
        String baseUsername = email.split("@")[0];
        String username = generateUniqueUsername(baseUsername);
        
        UserCredentials credentials = new UserCredentials(
            user,
            username,
            passwordEncoder.encode("password123")
        );
        userCredentialsRepository.save(credentials);
        user.setCredentials(credentials);
        
        // Assign role
        user.addRole(role);
        userRepository.save(user);
    }
}
