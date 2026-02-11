package com.example.metricsdemo.service;

import com.example.metricsdemo.exception.UserNotFoundException;
import com.example.metricsdemo.model.*;
import com.example.metricsdemo.repository.*;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
    private UserCacheService userCacheService;
    
    // @Autowired
    // private UserSearchService userSearchService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        
        // Register a gauge to track total number of users
        Gauge.builder("users_total", this, UserService::getUserCount)
                .description("Total number of users in the system")
                .register(meterRegistry);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").ascending());
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.getContent();
    }

    public Page<User> getAllUsersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").ascending());
        return userRepository.findAll(pageable);
    }

    public Page<User> searchUsers(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.searchUsers(searchTerm, pageable);
    }

    public User getUserById(Long id) {
        // Try to get from cache first
        User cachedUser = userCacheService.getCachedUser(id);
        if (cachedUser != null) {
            return cachedUser;
        }
        
        // If not in cache, get from database
        logger.info("Fetching user {} from PostgreSQL database", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            // Cache the user for future requests
            userCacheService.cacheUser(user.get());
            return user.get();
        }
        
        // User not found - throw exception
        throw new UserNotFoundException(id);
    }

    @Transactional
    public User createUser(String username, String password, String name, String email, Long departmentId, Set<String> roleNames) {
        logger.info("Creating user: username={}, name={}, email={}, departmentId={}, roles={}", 
            username, name, email, departmentId, roleNames);
        
        // Validate inputs
        if (departmentId == null) {
            throw new IllegalArgumentException("Department ID cannot be null");
        }
        
        // Get department
        Department department = getDepartmentById(departmentId);
        logger.info("Found department: {}", department.getName());
        
        // Create user entity
        User user = new User(name, email, department);
        User savedUser = userRepository.save(user);
        logger.info("Saved user entity with ID: {}", savedUser.getId());
        
        // Create credentials
        UserCredentials credentials = new UserCredentials(
            savedUser,
            username,
            passwordEncoder.encode(password)
        );
        userCredentialsRepository.save(credentials);
        savedUser.setCredentials(credentials);
        logger.info("Created credentials for user: {}", username);
        
        // Assign roles (default to ROLE_USER if none provided)
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = Set.of("ROLE_USER");
        }
        
        for (String roleName : roleNames) {
            logger.info("Looking up role: {}", roleName);
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            
            // Create UserRole manually to avoid cascade issues
            UserRole userRole = new UserRole(savedUser, role);
            userRoleRepository.save(userRole);
            savedUser.getUserRoles().add(userRole);
            logger.info("Added role {} to user {}", roleName, username);
        }
        
        // Save user again to update relationships
        savedUser = userRepository.save(savedUser);
        
        // Cache the newly created user
        userCacheService.cacheUser(savedUser);
        // Index in Elasticsearch - disabled for t2.micro
        // userSearchService.indexUser(savedUser);
        
        logger.info("Created user: {} with roles: {}", username, roleNames);
        return savedUser;
    }

    @Transactional
    public User updateUser(Long id, String name, String email, Long departmentId, String password, Set<String> roleNames) {
        logger.info("Attempting to update user with ID: {}", id);
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String oldName = user.getName();
            String oldEmail = user.getEmail();
            String oldDepartment = user.getDepartment() != null ? user.getDepartment().getName() : "null";
            
            // Update basic fields
            if (name != null) {
                user.setName(name);
            }
            if (email != null) {
                user.setEmail(email);
            }
            if (departmentId != null) {
                Department department = getDepartmentById(departmentId);
                user.setDepartment(department);
            }
            
            // Update password if provided
            if (password != null && !password.isEmpty()) {
                UserCredentials credentials = user.getCredentials();
                if (credentials != null) {
                    credentials.setPassword(passwordEncoder.encode(password));
                    userCredentialsRepository.save(credentials);
                }
            }
            
            // Update roles if provided
            if (roleNames != null && !roleNames.isEmpty()) {
                // Clear existing roles
                user.getUserRoles().clear();
                userRoleRepository.deleteAll(userRoleRepository.findByUserId(id));
                
                // Add new roles
                for (String roleName : roleNames) {
                    Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                    user.addRole(role);
                }
            }
            
            User updatedUser = userRepository.save(user);
            
            logger.info("User updated successfully - ID: {}, Old: [name={}, email={}, dept={}], New: [name={}, email={}, dept={}]", 
                id, oldName, oldEmail, oldDepartment, 
                updatedUser.getName(), updatedUser.getEmail(), 
                updatedUser.getDepartment() != null ? updatedUser.getDepartment().getName() : "null");
            
            // Update cache with new user data
            userCacheService.cacheUser(updatedUser);
            logger.info("Updated user {} cached in Redis", id);
            
            // Update Elasticsearch index - disabled for t2.micro
            // userSearchService.indexUser(updatedUser);
            
            return updatedUser;
        }
        
        // User not found - throw exception
        logger.error("Failed to update user - User with ID {} not found", id);
        throw new UserNotFoundException(id);
    }

    @Transactional
    public boolean deleteUser(Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        if (userRepository.existsById(id)) {
            Optional<User> user = userRepository.findById(id);
            String userName = user.map(User::getName).orElse("Unknown");
            
            // Check if this is the admin user - prevent deletion
            if (user.isPresent()) {
                UserCredentials credentials = user.get().getCredentials();
                if (credentials != null && "admin".equals(credentials.getUsername())) {
                    logger.warn("Attempted to delete admin user - operation blocked");
                    throw new IllegalArgumentException("Cannot delete the admin user");
                }
            }
            
            // Delete credentials (cascade will handle UserRole)
            userCredentialsRepository.findByUserId(id).ifPresent(userCredentialsRepository::delete);
            
            userRepository.deleteById(id);
            logger.info("User deleted successfully - ID: {}, Name: {}", id, userName);
            
            // Remove from cache
            userCacheService.evictUser(id);
            logger.info("User {} evicted from Redis cache", id);
            
            // Remove from Elasticsearch - disabled for t2.micro
            // userSearchService.deleteUser(id);
            
            return true;
        }
        
        // User not found - throw exception
        logger.error("Failed to delete user - User with ID {} not found", id);
        throw new UserNotFoundException(id);
    }

    public double getUserCount() {
        return userRepository.count();
    }
    
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
    }
    
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
    
    public Page<?> fuzzySearchUsers(String searchTerm, int page, int size) {
        // Elasticsearch disabled for t2.micro - return empty page
        logger.warn("Fuzzy search not available - Elasticsearch disabled");
        return Page.empty();
        /*
        Pageable pageable = PageRequest.of(page, size);
        return userSearchService.fuzzySearch(searchTerm, pageable);
        */
    }
    
    public Page<User> fuzzySearchUsersAsUsers(String searchTerm, int page, int size) {
        // Elasticsearch disabled for t2.micro - return empty page
        logger.warn("Fuzzy search not available - Elasticsearch disabled");
        return Page.empty();
        /*
        Pageable pageable = PageRequest.of(page, size);
        Page<com.example.metricsdemo.document.UserDocument> searchResults = 
            userSearchService.fuzzySearch(searchTerm, pageable);
        
        // Convert UserDocument to User entities with full data from database
        List<User> users = searchResults.getContent().stream()
            .map(doc -> userRepository.findById(doc.getId()).orElse(null))
            .filter(user -> user != null)
            .toList();
        
        return new org.springframework.data.domain.PageImpl<>(
            users,
            pageable,
            searchResults.getTotalElements()
        );
        */
    }
    
    public long reindexAllUsers() {
        // Elasticsearch disabled for t2.micro
        logger.warn("Reindex not available - Elasticsearch disabled");
        return 0;
        /*
        List<User> allUsers = userRepository.findAll();
        userSearchService.reindexAll(allUsers);
        return allUsers.size();
        */
    }
    
    // Authentication-specific methods
    
    public User getUserByUsername(String username) {
        UserCredentials credentials = userCredentialsRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return credentials.getUser();
    }
    
    @Transactional
    public User updateUserRoles(Long id, Set<String> roleNames) {
        logger.info("Attempting to update roles for user with ID: {}", id);
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Set<String> oldRoles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
            
            // Clear existing roles
            user.getUserRoles().clear();
            userRoleRepository.deleteAll(userRoleRepository.findByUserId(id));
            
            // Add new roles
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                user.addRole(role);
            }
            
            User updatedUser = userRepository.save(user);
            
            logger.info("User roles updated successfully - ID: {}, Old roles: {}, New roles: {}", 
                id, oldRoles, roleNames);
            
            // Update cache
            userCacheService.cacheUser(updatedUser);
            
            return updatedUser;
        }
        
        logger.error("Failed to update roles - User with ID {} not found", id);
        throw new UserNotFoundException(id);
    }
}
