package com.example.metricsdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;

import java.util.Map;
import java.util.Set;

public class UpdateUserRequest {
    
    private String name;
    
    @Email
    private String email;
    
    private Long departmentId;
    
    private String password;  // Optional, only if changing
    
    private Set<String> roles;  // Role names like "ROLE_USER", "ROLE_ADMIN" - only ADMIN can change
    
    // Constructors
    public UpdateUserRequest() {}
    
    public UpdateUserRequest(String name, String email, Long departmentId, String password, Set<String> roles) {
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.password = password;
        this.roles = roles;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    // Handle both "departmentId": 2 and "department": {"id": 2} formats
    @JsonProperty("department")
    public void setDepartment(Map<String, Object> department) {
        if (department != null && department.containsKey("id")) {
            Object id = department.get("id");
            if (id instanceof Number) {
                this.departmentId = ((Number) id).longValue();
            } else if (id instanceof String) {
                this.departmentId = Long.parseLong((String) id);
            }
        }
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
