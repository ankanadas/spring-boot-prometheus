package com.example.metricsdemo.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class RoleUpdateRequest {
    
    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;  // Role names like "ROLE_USER", "ROLE_ADMIN"
    
    // Constructors
    public RoleUpdateRequest() {}
    
    public RoleUpdateRequest(Set<String> roles) {
        this.roles = roles;
    }
    
    // Getters and Setters
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
