package com.example.metricsdemo.service;

import com.example.metricsdemo.model.User;
import com.example.metricsdemo.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    
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

    public User getUserById(Long id) {
        // Try to get from cache first
        User cachedUser = userCacheService.getCachedUser(id);
        if (cachedUser != null) {
            return cachedUser;
        }
        
        // If not in cache, get from database
        logger.info("Fetching user {} from H2 database", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            // Cache the user for future requests
            userCacheService.cacheUser(user.get());
            return user.get();
        }
        return null;
    }

    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        // Cache the newly created user
        userCacheService.cacheUser(savedUser);
        return savedUser;
    }

    public User updateUser(Long id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setDepartment(userDetails.getDepartment());
            User updatedUser = userRepository.save(user);
            
            // Update cache with new user data
            userCacheService.cacheUser(updatedUser);
            return updatedUser;
        }
        return null;
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            // Remove from cache
            userCacheService.evictUser(id);
            return true;
        }
        return false;
    }

    public double getUserCount() {
        return userRepository.count();
    }
}