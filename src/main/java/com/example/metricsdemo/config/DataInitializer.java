package com.example.metricsdemo.config;

import com.example.metricsdemo.model.User;
import com.example.metricsdemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only add sample data if the database is empty
        if (userRepository.count() == 0) {
            userRepository.save(new User("John Doe", "john.doe@example.com", "Engineering"));
            userRepository.save(new User("Jane Smith", "jane.smith@example.com", "Marketing"));
            userRepository.save(new User("Bob Johnson", "bob.johnson@example.com", "Sales"));
            userRepository.save(new User("Alice Brown", "alice.brown@example.com", "HR"));
            userRepository.save(new User("Charlie Wilson", "charlie.wilson@example.com", "Engineering"));
            
            System.out.println("✅ Sample data initialized: " + userRepository.count() + " users created");
        }
    }
}