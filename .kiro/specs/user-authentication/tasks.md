# Implementation Plan: Role-Based Authentication and Authorization

## Overview

This implementation plan breaks down the role-based authentication and authorization feature into discrete, incremental coding tasks. Each task builds on previous work, with testing integrated throughout to validate functionality early. The implementation follows Spring Security best practices and maintains backward compatibility with existing features.

## Tasks

- [x] 1. Create Role enum and update User entity with authentication fields
  - Create `Role.java` enum with USER and ADMIN values
  - Add username, password, and role fields to User entity
  - Add database indexes for username and role
  - Update User constructors and getters/setters
  - _Requirements: 1.1, 1.2, 1.3, 1.5_

- [ ]* 1.1 Write property test for username uniqueness
  - **Property 18: Username Uniqueness**
  - **Validates: Requirements 1.1**

- [ ]* 1.2 Write property test for valid role values
  - **Property 19: Valid Role Values**
  - **Validates: Requirements 1.3**

- [x] 2. Add Spring Security dependencies and create SecurityConfig
  - Add spring-boot-starter-security to pom.xml
  - Create SecurityConfig class with @EnableWebSecurity
  - Configure BCryptPasswordEncoder bean
  - Configure basic HTTP security (disable CSRF for stateless API)
  - _Requirements: 8.1, 8.3, 8.5_

- [x] 3. Implement CustomUserDetailsService for authentication
  - Create CustomUserDetailsService implementing UserDetailsService
  - Implement loadUserByUsername method to query UserRepository
  - Create CustomUserDetails class implementing UserDetails
  - Map User entity to UserDetails with authorities based on role
  - Handle UsernameNotFoundException for invalid usernames
  - _Requirements: 2.6, 8.4_

- [ ]* 3.1 Write property test for valid credentials authentication
  - **Property 2: Valid Credentials Authenticate Successfully**
  - **Validates: Requirements 2.1, 2.2**

- [ ]* 3.2 Write property test for invalid credentials rejection
  - **Property 3: Invalid Credentials Rejected**
  - **Validates: Requirements 2.3**

- [ ]* 3.3 Write property test for protected endpoints requiring authentication
  - **Property 4: Protected Endpoints Require Authentication**
  - **Validates: Requirements 2.4**

- [x] 4. Configure endpoint authorization rules in SecurityConfig
  - Configure permitAll for GET /api/users/search (public access)
  - Configure authenticated() for GET /api/users/{id}
  - Configure hasRole("ADMIN") for POST /api/users
  - Configure hasRole("ADMIN") for DELETE /api/users/{id}
  - Configure hasRole("ADMIN") for PATCH /api/users/{id}/role
  - Configure authenticated() for PUT /api/users/{id} (ownership check in controller)
  - Configure HTTP Basic Authentication
  - _Requirements: 3.1, 4.1, 4.6, 4.7, 4.8, 5.1, 5.3, 5.4_

- [ ]* 4.1 Write property test for public search access
  - **Property 5: Public Search Access**
  - **Validates: Requirements 3.1, 3.2**

- [ ]* 4.2 Write unit test for existing search functionality
  - Verify Elasticsearch fuzzy search still works with public access
  - _Requirements: 3.3_

- [x] 5. Add repository methods for username queries
  - Add findByUsername(String username) to UserRepository
  - Add existsByUsername(String username) to UserRepository
  - _Requirements: 1.1_

- [x] 6. Update UserService with password hashing and authentication methods
  - Inject PasswordEncoder into UserService
  - Update createUser to hash passwords before saving
  - Update updateUser to hash passwords if changed
  - Add getUserByUsername method
  - Add updateUserRole method for role changes
  - Ensure cached and indexed users exclude password field
  - _Requirements: 1.4, 11.2_

- [ ]* 6.1 Write property test for password BCrypt hashing
  - **Property 1: Password BCrypt Hashing**
  - **Validates: Requirements 1.2, 1.4, 11.2**

- [ ]* 6.2 Write property test for cache excluding passwords
  - **Property 27: Cache Excludes Passwords**
  - **Validates: Requirements 12.1, 12.2**

- [ ]* 6.3 Write property test for search index excluding sensitive fields
  - **Property 28: Search Index Excludes Sensitive Fields**
  - **Validates: Requirements 12.3**

- [x] 7. Create DTOs for authentication-aware requests and responses
  - Create CreateUserRequest DTO with username, password, name, email, departmentId, role
  - Create UpdateUserRequest DTO with name, email, departmentId, password (optional), role
  - Create UserDTO for responses with id, username, name, email, departmentName, role (NO password)
  - Create RoleUpdateRequest DTO with role field
  - Add validation annotations to DTOs
  - _Requirements: 9.1, 9.2, 9.3_

- [ ]* 7.1 Write property test for password never exposed in responses
  - **Property 20: Password Never Exposed in Responses**
  - **Validates: Requirements 9.3, 11.3**

- [ ]* 7.2 Write unit test for DTO structure validation
  - Verify CreateUserRequest has all required fields
  - Verify UserDTO has all required fields except password
  - _Requirements: 9.1, 9.2_

- [ ] 8. Update UserController with ownership validation and new endpoints
  - Update PUT /api/users/{id} to check ownership for USER role
  - Add logic to prevent USER from changing their own role
  - Add PATCH /api/users/{id}/role endpoint for ADMIN role changes
  - Update POST /api/users to use CreateUserRequest DTO
  - Update all responses to use UserDTO (excluding passwords)
  - Inject Authentication parameter where needed for ownership checks
  - _Requirements: 4.2, 4.3, 4.4, 4.5, 5.2, 5.4, 9.4, 9.5_

- [ ]* 8.1 Write property test for USER can view any profile
  - **Property 6: USER Can View Any Profile**
  - **Validates: Requirements 4.1**

- [ ]* 8.2 Write property test for USER can update own profile
  - **Property 7: USER Can Update Own Profile**
  - **Validates: Requirements 4.3**

- [ ]* 8.3 Write property test for USER cannot update other profiles
  - **Property 8: USER Cannot Update Other Profiles**
  - **Validates: Requirements 4.4**

- [ ]* 8.4 Write property test for USER cannot change own role
  - **Property 9: USER Cannot Change Own Role**
  - **Validates: Requirements 4.5**

- [ ]* 8.5 Write property test for USER cannot create users
  - **Property 10: USER Cannot Create Users**
  - **Validates: Requirements 4.6**

- [ ]* 8.6 Write property test for USER cannot delete users
  - **Property 11: USER Cannot Delete Users**
  - **Validates: Requirements 4.7**

- [ ]* 8.7 Write property test for USER cannot change roles
  - **Property 12: USER Cannot Change Roles**
  - **Validates: Requirements 4.8**

- [ ]* 8.8 Write property test for ADMIN can create users
  - **Property 13: ADMIN Can Create Users**
  - **Validates: Requirements 5.1**

- [ ]* 8.9 Write property test for ADMIN can update any user
  - **Property 14: ADMIN Can Update Any User**
  - **Validates: Requirements 5.2**

- [ ]* 8.10 Write property test for ADMIN can delete any user
  - **Property 15: ADMIN Can Delete Any User**
  - **Validates: Requirements 5.3**

- [ ]* 8.11 Write property test for ADMIN can change roles
  - **Property 16: ADMIN Can Change Roles**
  - **Validates: Requirements 5.4**

- [ ]* 8.12 Write property test for ADMIN can view any profile
  - **Property 17: ADMIN Can View Any Profile**
  - **Validates: Requirements 5.5**

- [ ] 9. Create custom exception classes and update global exception handler
  - Create AccessDeniedException for authorization failures
  - Update GlobalExceptionHandler with @ExceptionHandler for AccessDeniedException (403)
  - Update GlobalExceptionHandler with @ExceptionHandler for AuthenticationException (401)
  - Add specific error messages for each failure type
  - Add security logging for authentication and authorization failures
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_

- [ ]* 9.1 Write unit tests for error messages
  - Test 401 error message for invalid credentials
  - Test 401 error message for missing credentials
  - Test 403 error message for insufficient permissions
  - Test 403 error message for ownership validation failure
  - Test 403 error message for role change attempt
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ]* 9.2 Write property test for authentication failures logged
  - **Property 30: Authentication Failures Logged**
  - **Validates: Requirements 10.6**

- [ ]* 9.3 Write property test for authorization failures logged
  - **Property 31: Authorization Failures Logged**
  - **Validates: Requirements 10.7**

- [ ]* 9.4 Write property test for password never logged
  - **Property 21: Password Never Logged**
  - **Validates: Requirements 11.4**

- [ ] 10. Checkpoint - Ensure all tests pass
  - Run all unit tests and property tests
  - Verify authentication and authorization work correctly
  - Test with Postman or curl for manual verification
  - Ask the user if questions arise

- [ ] 11. Update DataInitializer with bootstrap admin creation
  - Add method to create bootstrap admin if not exists
  - Check if username "admin" already exists before creating
  - Create admin user with username="admin", password="admin123", role=ADMIN
  - Set appropriate name, email, and department for admin
  - Hash password with BCryptPasswordEncoder
  - Index admin in Elasticsearch
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ]* 11.1 Write unit test for bootstrap admin creation
  - Verify admin has correct username, role, and can authenticate
  - _Requirements: 6.2_

- [ ]* 11.2 Write property test for bootstrap admin idempotency
  - **Property 22: Bootstrap Admin Idempotency**
  - **Validates: Requirements 6.3**

- [ ]* 11.3 Write unit test for bootstrap admin searchable
  - Verify admin is indexed in Elasticsearch after bootstrap
  - _Requirements: 6.5_

- [ ] 12. Implement data migration for existing users
  - Add method to detect users without username/password/role
  - Generate username from email (local part before @)
  - Implement username conflict resolution with numeric suffixes
  - Assign default password "password123" (BCrypt hashed)
  - Assign default role USER
  - Update all existing users in database
  - Log migration progress
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ]* 12.1 Write property test for username generation from email
  - **Property 23: Username Generation from Email**
  - **Validates: Requirements 7.2**

- [ ]* 12.2 Write property test for migrated users have default password
  - **Property 24: Migrated Users Have Default Password**
  - **Validates: Requirements 7.3**

- [ ]* 12.3 Write property test for migrated users have USER role
  - **Property 25: Migrated Users Have USER Role**
  - **Validates: Requirements 7.4**

- [ ]* 12.4 Write property test for username conflict resolution
  - **Property 26: Username Conflict Resolution**
  - **Validates: Requirements 7.5**

- [ ] 13. Update UserCacheService to exclude passwords from cache
  - Modify cache serialization to exclude password field
  - Verify cached users do not contain passwords
  - Update cache eviction logic for user updates and deletions
  - _Requirements: 12.1, 12.2_

- [ ] 14. Update UserSearchService to exclude sensitive fields from index
  - Verify UserDocument does not include password or role fields
  - Ensure indexing logic excludes sensitive data
  - Test search functionality still works correctly
  - _Requirements: 12.3_

- [ ]* 14.1 Write property test for deletion removes from cache and index
  - **Property 29: Deletion Removes from Cache and Index**
  - **Validates: Requirements 12.4**

- [ ]* 14.2 Write unit test for Prometheus metrics maintained
  - Verify existing metrics still work after adding security
  - _Requirements: 12.5_

- [ ] 15. Create database migration script for schema changes
  - Create Flyway or Liquibase migration script
  - Add username column (VARCHAR 255, NOT NULL, UNIQUE)
  - Add password column (VARCHAR 255, NOT NULL)
  - Add role column (VARCHAR 20, NOT NULL)
  - Create index on username
  - Create index on role
  - Handle existing data with default values
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 16. Final checkpoint - Integration testing and verification
  - Run full test suite (unit + property tests)
  - Test authentication flow end-to-end
  - Test authorization for all roles and endpoints
  - Test bootstrap admin creation on fresh database
  - Test data migration with existing users
  - Verify cache and search integration
  - Verify error handling and logging
  - Test with Postman collection for all endpoints
  - Ask the user if questions arise

- [ ]* 16.1 Write integration tests for end-to-end flows
  - Test complete authentication flow (Basic Auth → Database → Success)
  - Test complete authorization flow (Role check → Ownership → Service)
  - Test cache integration (Create → Cache → Retrieve)
  - Test search integration (Create → Index → Search)

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate universal correctness properties with 100+ iterations
- Unit tests validate specific examples, edge cases, and error messages
- Bootstrap admin and data migration run automatically on application startup
- All passwords are BCrypt hashed before storage
- Passwords are never exposed in API responses, logs, or cache
- Integration with existing Redis caching and Elasticsearch indexing is maintained
