package com.example.metricsdemo.config;

import com.example.metricsdemo.model.Department;
import com.example.metricsdemo.model.User;
import com.example.metricsdemo.repository.DepartmentRepository;
import com.example.metricsdemo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) throws Exception {
        long existingUserCount = userRepository.count();
        long existingDeptCount = departmentRepository.count();
        
        if (existingUserCount > 0 || existingDeptCount > 0) {
            logger.info("Database already contains {} users and {} departments. Skipping data initialization.", 
                existingUserCount, existingDeptCount);
            return;
        }
        
        logger.info("Database is empty. Initializing sample data...");
        
        // Create departments first
        Department engineering = departmentRepository.save(new Department("Engineering", "Software development and technical teams"));
        Department marketing = departmentRepository.save(new Department("Marketing", "Marketing and brand management"));
        Department sales = departmentRepository.save(new Department("Sales", "Sales and business development"));
        Department hr = departmentRepository.save(new Department("HR", "Human resources and recruitment"));
        Department security = departmentRepository.save(new Department("Security", "Security and risk management"));
        Department photography = departmentRepository.save(new Department("Photography", "Photography and media"));
        Department legal = departmentRepository.save(new Department("Legal", "Legal and compliance"));
        Department journalism = departmentRepository.save(new Department("Journalism", "News and reporting"));
        Department leadership = departmentRepository.save(new Department("Leadership", "Executive leadership"));
        Department research = departmentRepository.save(new Department("Research", "Research and development"));
        Department operations = departmentRepository.save(new Department("Operations", "Operations and logistics"));
        
        logger.info("Created {} departments", departmentRepository.count());
        
        // Create users with department references
        userRepository.save(new User("John Doe", "john.doe@example.com", engineering));
        userRepository.save(new User("Jane Smith", "jane.smith@example.com", marketing));
        userRepository.save(new User("Bob Johnson", "bob.johnson@example.com", sales));
        userRepository.save(new User("Alice Brown", "alice.brown@example.com", hr));
        userRepository.save(new User("Charlie Wilson", "charlie.wilson@example.com", engineering));            
        userRepository.save(new User("Tony Stark", "tony.stark@example.com", engineering));
        userRepository.save(new User("Bruce Wayne", "bruce.wayne@example.com", security));
        userRepository.save(new User("Peter Parker", "peter.parker@example.com", photography));
        userRepository.save(new User("Diana Prince", "diana.prince@example.com", legal));
        userRepository.save(new User("Clark Kent", "clark.kent@example.com", journalism));
        userRepository.save(new User("Natasha Romanoff", "natasha.romanoff@example.com", security));
        userRepository.save(new User("Steve Rogers", "steve.rogers@example.com", leadership));
        userRepository.save(new User("Wanda Maximoff", "wanda.maximoff@example.com", research));
        userRepository.save(new User("Scott Lang", "scott.lang@example.com", engineering));
        userRepository.save(new User("Carol Danvers", "carol.danvers@example.com", operations));

        long finalUserCount = userRepository.count();
        long finalDeptCount = departmentRepository.count();
        logger.info("✅ Sample data initialized successfully: {} users and {} departments created", 
            finalUserCount, finalDeptCount);
    }
}
