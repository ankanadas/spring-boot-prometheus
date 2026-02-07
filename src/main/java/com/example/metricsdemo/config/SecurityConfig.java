package com.example.metricsdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(HttpMethod.GET, "/api/users/search").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/", "/index.html", "/login.html").permitAll()
                
                // User endpoints - role-based access control
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()  // USER or ADMIN can view
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()  // Ownership check in controller
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")     // Only ADMIN can create
                .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("ADMIN")  // Only ADMIN can delete
                .requestMatchers(HttpMethod.PATCH, "/api/users/{id}/role").hasRole("ADMIN")  // Only ADMIN can change roles
                .requestMatchers(HttpMethod.GET, "/api/users").authenticated()  // List users - authenticated
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Enable HTTP Basic Authentication
            .httpBasic(basic -> {})
            
            // Stateless session management for REST API
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
