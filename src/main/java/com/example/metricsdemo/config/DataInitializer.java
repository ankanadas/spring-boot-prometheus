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
            userRepository.save(new User("Tony Stark", "tony.stark@example.com", "Engineering"));
            userRepository.save(new User("Bruce Wayne", "bruce.wayne@example.com", "Security"));
            userRepository.save(new User("Peter Parker", "peter.parker@example.com", "Photography"));
            userRepository.save(new User("Diana Prince", "diana.prince@example.com", "Legal"));
            userRepository.save(new User("Clark Kent", "clark.kent@example.com", "Journalism"));
            userRepository.save(new User("Natasha Romanoff", "natasha.romanoff@example.com", "Security"));
            userRepository.save(new User("Steve Rogers", "steve.rogers@example.com", "Leadership"));
            userRepository.save(new User("Wanda Maximoff", "wanda.maximoff@example.com", "Research"));
            userRepository.save(new User("Scott Lang", "scott.lang@example.com", "Engineering"));
            userRepository.save(new User("Carol Danvers", "carol.danvers@example.com", "Operations"));

            System.out.println("✅ Sample data initialized: " + userRepository.count() + " users created");
        }
    }
}