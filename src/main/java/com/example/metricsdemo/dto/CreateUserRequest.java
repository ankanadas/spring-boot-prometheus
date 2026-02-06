package com.example.metricsdemo.dto;

public class CreateUserRequest {
    private String name;
    private String email;
    private Long departmentId;
    
    public CreateUserRequest() {}
    
    public CreateUserRequest(String name, String email, Long departmentId) {
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
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
}
