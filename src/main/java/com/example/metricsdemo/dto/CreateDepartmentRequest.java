package com.example.metricsdemo.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateDepartmentRequest {
    
    @NotBlank(message = "Department name is required")
    private String name;
    
    private String description;
    
    // Constructors
    public CreateDepartmentRequest() {}
    
    public CreateDepartmentRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
