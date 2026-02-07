package com.example.metricsdemo.dto;

import java.util.Set;

public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private Long departmentId;
    private String departmentName;
    private Set<String> roles;  // Role names like "ROLE_USER", "ROLE_ADMIN"
    // NO password field - never expose passwords in responses
    
    public UserDTO() {}
    
    public UserDTO(Long id, String username, String name, String email, Long departmentId, String departmentName, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.roles = roles;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
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
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
