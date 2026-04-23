package com.duong.salesmanagement.config;

import com.duong.salesmanagement.model.Role;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("No actors found. Initializing default accounts...");

            // Admin account
            userRepository.save(new User("admin", "admin123", "System Administrator", Role.ADMIN));
            
            // Customer account
            userRepository.save(new User("customer", "customer123", "John Doe - Customer", Role.CUSTOMER));
            
            // Restaurant owner account
            userRepository.save(new User("restaurant", "restaurant123", "Tasty Food Restaurant", Role.RESTAURANT));
            
            // Driver account
            userRepository.save(new User("driver", "driver123", "Mike - Driver", Role.DRIVER));

            System.out.println("Default accounts for all actors created successfully!");
        } else {
            System.out.println("Database already contains actor accounts.");
        }
    }
}
