package com.example.metricsdemo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles", schema = "userschema")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;  // e.g., "ROLE_ADMIN", "ROLE_USER"
    
    @Column
    private String description;
    
    // Constructors
    public Role() {}
    
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    // Helper method to get authority string for Spring Security
    public String getAuthority() {
        return name;
    }
}
