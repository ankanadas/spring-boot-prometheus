package com.example.metricsdemo.security;

import com.example.metricsdemo.model.User;
import com.example.metricsdemo.model.UserCredentials;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    
    private final UserCredentials credentials;
    
    public CustomUserDetails(UserCredentials credentials) {
        this.credentials = credentials;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Get all roles from the user's UserRole relationships
        return credentials.getUser().getUserRoles().stream()
            .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getName()))
            .collect(Collectors.toList());
    }
    
    @Override
    public String getPassword() {
        return credentials.getPassword();
    }
    
    @Override
    public String getUsername() {
        return credentials.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // Expose the underlying User entity for controller access
    public User getUser() {
        return credentials.getUser();
    }
}
