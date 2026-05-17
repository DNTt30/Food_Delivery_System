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
                                      DriverProfileRepository driverProfileRepository,
                                      FoodOrderRepository foodOrderRepository) {
        return args -> {
            try {
                // 1. Seed Admin
                if (!userRepository.existsByUsername("admin")) {
                    User admin = new User("admin", "admin123", "System Admin", "admin@foodonl.com", Role.ADMIN);
                    admin.setEnabled(true);
                    userRepository.save(admin);
                }

                // 2. Seed Customer & Order
                User customerUser = userRepository.findByUsername("customer").orElse(null);
                if (customerUser == null) {
                    customerUser = new User("customer", "customer123", "Default Customer", "customer@foodonl.com", Role.CUSTOMER);
                    customerUser.setEnabled(true);
                    userRepository.save(customerUser);

                    CustomerProfile profile = new CustomerProfile();
                    profile.setUser(customerUser);
                    profile.setPhoneNumber("0123456789");
                    profile.setDeliveryAddress("227 Nguyễn Văn Cừ, Quận 5, TP.HCM");
                    customerProfileRepository.save(profile);
                }
                CustomerProfile customerProfile = customerProfileRepository.findByUser(customerUser).get();

                // 3. Seed Restaurant
                User restaurantUser = userRepository.findByUsername("restaurant").orElse(null);
                if (restaurantUser == null) {
                    restaurantUser = new User("restaurant", "restaurant123", "Test Restaurant Owner", "restaurant@foodonl.com", Role.RESTAURANT);
                    restaurantUser.setEnabled(true);
                    userRepository.save(restaurantUser);

                    RestaurantProfile profile = new RestaurantProfile();
                    profile.setUser(restaurantUser);
                    profile.setRestaurantName("The Coffee House");
                    profile.setAddress("797 Sư Vạn Hạnh, Quận 10, TP.HCM");
                    profile.setLatitude(10.772500); // Tọa độ mẫu
                    profile.setLongitude(106.669500);
                    profile.setOpen(true);
                    restaurantProfileRepository.save(profile);
                }
                RestaurantProfile restProfile = restaurantProfileRepository.findByUser(restaurantUser).get();

                // 4. Seed Driver
                User driverUser = userRepository.findByUsername("driver").orElse(null);
                if (driverUser == null) {
                    driverUser = new User("driver", "driver123", "Default Driver", "driver@foodonl.com", Role.DRIVER);
                    driverUser.setEnabled(true);
                    userRepository.save(driverUser);

                    DriverProfile profile = new DriverProfile();
                    profile.setUser(driverUser);
                    profile.setPhoneNumber("0987654321");
                    profile.setLicensePlate("59-G1 123.45");
                    profile.setAvailable(true);
                    driverProfileRepository.save(profile);
                }
                DriverProfile driverProfile = driverProfileRepository.findByUser(driverUser).get();

                // 5. [SPECIAL FIX] Ép nạp tọa độ cho đơn hàng #3 của bạn
                foodOrderRepository.findById(3L).ifPresent(order -> {
                    order.setRestaurantLat(10.772500);
                    order.setRestaurantLng(106.669500);
                    order.setDeliveryLat(10.762913); 
                    order.setDeliveryLng(106.682171);
                    order.setRestaurantAddressSnapshot("797 Sư Vạn Hạnh, Quận 10");
                    order.setDeliveryAddressSnapshot("227 Nguyễn Văn Cừ, Quận 5");
                    order.setStatus(OrderStatus.DELIVERING);
                    if (order.getDriver() == null) order.setDriver(driverProfile);
                    foodOrderRepository.save(order);
                    System.out.println("✨ MAGIC: Order #3 has been force-updated with coordinates!");
                });

                // 6. Đảm bảo luôn có ít nhất 1 đơn hàng xịn khác
                FoodOrder testOrder = foodOrderRepository.findAll().stream()
                        .filter(o -> o.getRestaurantLat() != null && o.getDeliveryLat() != null)
                        .findFirst()
                        .orElse(null);

                if (testOrder == null) {
                    FoodOrder order = new FoodOrder();
                    order.setCustomer(customerProfile);
                    order.setRestaurant(restProfile);
                    order.setDriver(driverProfile);
                    order.setStatus(OrderStatus.DELIVERING);
                    order.setTotalAmount(45000.0);
                    order.setRestaurantLat(10.772500);
                    order.setRestaurantLng(106.669500);
                    order.setDeliveryLat(10.762913); 
                    order.setDeliveryLng(106.682171);
                    foodOrderRepository.save(order);
                }

                System.out.println("Data initialization completed.");
            } catch (Exception e) {
                System.out.println("Data initialization error: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
