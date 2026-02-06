package com.example.metricsdemo.service;

import com.example.metricsdemo.exception.UserNotFoundException;
import com.example.metricsdemo.model.Department;
import com.example.metricsdemo.model.User;
import com.example.metricsdemo.repository.DepartmentRepository;
import com.example.metricsdemo.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserCacheService userCacheService;

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
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.getContent();
    }

    public Page<User> getAllUsersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
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

    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        // Cache the newly created user
        userCacheService.cacheUser(savedUser);
        return savedUser;
    }

    public User updateUser(Long id, User userDetails) {
        logger.info("Attempting to update user with ID: {}", id);
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String oldName = user.getName();
            String oldEmail = user.getEmail();
            String oldDepartment = user.getDepartment() != null ? user.getDepartment().getName() : "null";
            
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setDepartment(userDetails.getDepartment());
            User updatedUser = userRepository.save(user);
            
            logger.info("User updated successfully - ID: {}, Old: [name={}, email={}, dept={}], New: [name={}, email={}, dept={}]", 
                id, oldName, oldEmail, oldDepartment, 
                updatedUser.getName(), updatedUser.getEmail(), 
                updatedUser.getDepartment() != null ? updatedUser.getDepartment().getName() : "null");
            
            // Update cache with new user data
            userCacheService.cacheUser(updatedUser);
            logger.info("Updated user {} cached in Redis", id);
            
            return updatedUser;
        }
        
        // User not found - throw exception
        logger.error("Failed to update user - User with ID {} not found", id);
        throw new UserNotFoundException(id);
    }

    public boolean deleteUser(Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        if (userRepository.existsById(id)) {
            Optional<User> user = userRepository.findById(id);
            String userName = user.map(User::getName).orElse("Unknown");
            
            userRepository.deleteById(id);
            logger.info("User deleted successfully - ID: {}, Name: {}", id, userName);
            
            // Remove from cache
            userCacheService.evictUser(id);
            logger.info("User {} evicted from Redis cache", id);
            
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
}
