package com.example.metricsdemo.controller;

import com.example.metricsdemo.dto.PagedResponse;
import com.example.metricsdemo.model.User;
import com.example.metricsdemo.service.UserService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users with Redis caching and Prometheus metrics")
public class UserController {

    @Autowired
    private UserService userService;

    private final Counter userCreationCounter;
    private final Counter userRetrievalCounter;
    private final Counter userUpdateCounter;
    private final Counter userDeletionCounter;
    private final Random random = new Random();

    public UserController(MeterRegistry meterRegistry) {
        this.userCreationCounter = Counter.builder("users_created_total")
                .description("Total number of users created")
                .register(meterRegistry);
        
        this.userRetrievalCounter = Counter.builder("users_retrieved_total")
                .description("Total number of user retrievals")
                .register(meterRegistry);
        
        this.userUpdateCounter = Counter.builder("users_updated_total")
                .description("Total number of users updated")
                .register(meterRegistry);
        
        this.userDeletionCounter = Counter.builder("users_deleted_total")
                .description("Total number of users deleted")
                .register(meterRegistry);
    }

    @GetMapping
    @Timed(value = "get_users_duration", description = "Time taken to get all users")
    @Counted(value = "get_users_count", description = "Number of times get all users is called")
    @Operation(summary = "Get all users (paginated)", description = "Retrieve a paginated list of users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    })
    public List<User> getAllUsers(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of users per page") @RequestParam(defaultValue = "5") int size) {
        userRetrievalCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        return userService.getAllUsers(page, size);
    }

    @GetMapping("/paged")
    @Timed(value = "get_users_paged_duration", description = "Time taken to get paginated users")
    @Operation(summary = "Get users with pagination metadata", description = "Retrieve users with full pagination information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated users")
    })
    public ResponseEntity<PagedResponse<User>> getAllUsersPaged(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of users per page") @RequestParam(defaultValue = "5") int size) {
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

    @GetMapping("/search")
    @Timed(value = "search_users_duration", description = "Time taken to search users")
    @Operation(summary = "Search users with fuzzy matching", description = "Search users by name, email, or department with typo tolerance using Elasticsearch")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    })
    public ResponseEntity<PagedResponse<User>> searchUsers(
            @Parameter(description = "Search query string (handles typos)") @RequestParam String query,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of users per page") @RequestParam(defaultValue = "5") int size) {
        userRetrievalCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        // Use Elasticsearch fuzzy search
        Page<User> userPage = userService.fuzzySearchUsersAsUsers(query, page, size);
        
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
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID (checks Redis cache first)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        userRetrievalCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Timed(value = "create_user_duration", description = "Time taken to create a user")
    @Counted(value = "create_user_count", description = "Number of users created")
    @Operation(summary = "Create a new user", description = "Create a new user and cache it in Redis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    })
    public User createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User object to create",
                required = true,
                content = @Content(schema = @Schema(implementation = User.class))
            )
            @RequestBody User user) {
        userCreationCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    @Timed(value = "update_user_duration", description = "Time taken to update a user")
    @Operation(summary = "Update user", description = "Update an existing user and refresh Redis cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated user object",
                required = true,
                content = @Content(schema = @Schema(implementation = User.class))
            )
            @RequestBody User userDetails) {
        userUpdateCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Timed(value = "delete_user_duration", description = "Time taken to delete a user")
    @Operation(summary = "Delete user", description = "Delete a user and remove from Redis cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        userDeletionCounter.increment();
        
        // Simulate some processing time
        simulateProcessingTime();
        
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is healthy")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is healthy!");
    }

    @GetMapping("/slow")
    @Timed(value = "slow_endpoint_duration", description = "Time taken for slow endpoint")
    @Operation(summary = "Slow endpoint", description = "Simulates a slow operation (2-5 seconds) for testing")
    @ApiResponse(responseCode = "200", description = "Slow operation completed")
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

    @GetMapping("/departments")
    @Operation(summary = "Get all departments", description = "Retrieve list of all departments")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved departments")
    public ResponseEntity<?> getAllDepartments() {
        return ResponseEntity.ok(userService.getAllDepartments());
    }
    
    @GetMapping("/fuzzy-search")
    @Timed(value = "fuzzy_search_users_duration", description = "Time taken for fuzzy search")
    @Operation(summary = "Fuzzy search users", description = "Search users with typo tolerance using Elasticsearch")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved fuzzy search results")
    })
    public ResponseEntity<?> fuzzySearchUsers(
            @Parameter(description = "Search query (handles typos)") @RequestParam String query,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of users per page") @RequestParam(defaultValue = "5") int size) {
        userRetrievalCounter.increment();
        simulateProcessingTime();
        
        return ResponseEntity.ok(userService.fuzzySearchUsers(query, page, size));
    }
    
    @PostMapping("/reindex")
    @Operation(summary = "Reindex all users", description = "Reindex all users in Elasticsearch for fuzzy search")
    @ApiResponse(responseCode = "200", description = "Reindexing completed")
    public ResponseEntity<String> reindexUsers() {
        long count = userService.reindexAllUsers();
        return ResponseEntity.ok("Reindexed " + count + " users in Elasticsearch");
    }
}
