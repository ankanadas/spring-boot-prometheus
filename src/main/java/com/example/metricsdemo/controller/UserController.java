package com.example.metricsdemo.controller;

import com.example.metricsdemo.dto.PagedResponse;
import com.example.metricsdemo.model.User;
import com.example.metricsdemo.service.UserService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private final Counter userCreationCounter;
    private final Counter userRetrievalCounter;
    private final Random random = new Random();

    public UserController(MeterRegistry meterRegistry) {
        this.userCreationCounter = Counter.builder("users_created_total")
                .description("Total number of users created")
                .register(meterRegistry);
        
        this.userRetrievalCounter = Counter.builder("users_retrieved_total")
                .description("Total number of user retrievals")
                .register(meterRegistry);
    }

    @GetMapping
    @Timed(value = "get_users_duration", description = "Time taken to get all users")
    @Counted(value = "get_users_count", description = "Number of times get all users is called")
    public List<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        userRetrievalCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        return userService.getAllUsers(page, size);
    }

    @GetMapping("/paged")
    @Timed(value = "get_users_paged_duration", description = "Time taken to get paginated users")
    public ResponseEntity<PagedResponse<User>> getAllUsersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        userRetrievalCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        Page<User> userPage = userService.getAllUsersPaged(page, size);
        
        PagedResponse<User> response = new PagedResponse<>(
            userPage.getContent(),
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Timed(value = "get_user_by_id_duration", description = "Time taken to get user by ID")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        userRetrievalCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Timed(value = "create_user_duration", description = "Time taken to create a user")
    @Counted(value = "create_user_count", description = "Number of users created")
    public User createUser(@RequestBody User user) {
        userCreationCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    @Timed(value = "update_user_duration", description = "Time taken to update a user")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        // Simulate some processing time
        simulateProcessingTime();
        
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Timed(value = "delete_user_duration", description = "Time taken to delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Simulate some processing time
        simulateProcessingTime();
        
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is healthy!");
    }

    @GetMapping("/slow")
    @Timed(value = "slow_endpoint_duration", description = "Time taken for slow endpoint")
    public ResponseEntity<String> slowEndpoint() {
        // Simulate a slow operation
        try {
            Thread.sleep(2000 + random.nextInt(3000)); // 2-5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ResponseEntity.ok("Slow operation completed");
    }

    private void simulateProcessingTime() {
        try {
            // Random processing time between 50-500ms
            Thread.sleep(50 + random.nextInt(450));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}