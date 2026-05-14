package com.duong.salesmanagement.config;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, 
                                      CustomerProfileRepository customerProfileRepository,
                                      RestaurantProfileRepository restaurantProfileRepository,
                                      DriverProfileRepository driverProfileRepository) {
        return args -> {
            try {
                // Seed Admin
                if (!userRepository.existsByUsername("admin")) {
                    User admin = new User("admin", "admin123", "System Admin", "admin@foodonl.com", Role.ADMIN);
                    admin.setEnabled(true);
                    userRepository.save(admin);
                }

                // Seed Customer
                if (!userRepository.existsByUsername("customer")) {
                    User customerUser = new User("customer", "customer123", "Default Customer", "customer@foodonl.com", Role.CUSTOMER);
                    customerUser.setEnabled(true);
                    userRepository.save(customerUser);

                    CustomerProfile profile = new CustomerProfile();
                    profile.setUser(customerUser);
                    profile.setPhoneNumber("0123456789");
                    profile.setDeliveryAddress("123 Street, City");
                    customerProfileRepository.save(profile);
                }

                // Seed Restaurant
                if (!userRepository.existsByUsername("restaurant")) {
                    User restaurantUser = new User("restaurant", "restaurant123", "Test Restaurant Owner", "restaurant@foodonl.com", Role.RESTAURANT);
                    restaurantUser.setEnabled(true);
                    userRepository.save(restaurantUser);

                    RestaurantProfile profile = new RestaurantProfile();
                    profile.setUser(restaurantUser);
                    profile.setRestaurantName("Test Restaurant");
                    profile.setAddress("456 Avenue, Food Court");
                    profile.setOpen(true);
                    restaurantProfileRepository.save(profile);
                }

                // Seed Driver
                if (!userRepository.existsByUsername("driver")) {
                    User driverUser = new User("driver", "driver123", "Default Driver", "driver@foodonl.com", Role.DRIVER);
                    driverUser.setEnabled(true);
                    userRepository.save(driverUser);

                    DriverProfile profile = new DriverProfile();
                    profile.setUser(driverUser);
                    profile.setPhoneNumber("0987654321");
                    profile.setLicensePlate("ABC-123");
                    profile.setAvailable(true);
                    driverProfileRepository.save(profile);
                }

                System.out.println("Data initialization completed.");
            } catch (Exception e) {
                System.out.println("Data already initialized or error occurred: " + e.getMessage());
            }
        };
    }
}
