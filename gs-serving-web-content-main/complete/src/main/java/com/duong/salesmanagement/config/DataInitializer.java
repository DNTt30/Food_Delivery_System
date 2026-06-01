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
                                      FoodOrderRepository foodOrderRepository,
                                      MenuItemRepository menuItemRepository,
                                      OrderItemRepository orderItemRepository,
                                      ReviewRepository reviewRepository) {
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

                // 7. Seed rich realistic data for "restaurant" (The Coffee House) if it doesn't have enough reviews
                if (reviewRepository.count() < 10) {
                    System.out.println("🌱 Seeding menu items, orders, and reviews for The Coffee House...");
                    
                    // Create Menu Items or retrieve existing ones
                    java.util.List<MenuItem> existingItems = menuItemRepository.findByRestaurant(restProfile);
                    
                    MenuItem m1 = existingItems.stream().filter(m -> m.getName().equals("Cà Phê Sữa Đá")).findFirst().orElse(null);
                    if (m1 == null) {
                        m1 = new MenuItem();
                        m1.setRestaurant(restProfile);
                        m1.setName("Cà Phê Sữa Đá");
                        m1.setDescription("Cà phê hạt pha phin đậm đà kết hợp sữa đặc.");
                        m1.setPrice(29000.0);
                        m1.setAvailable(true);
                        m1.setSoldCount(125);
                        m1 = menuItemRepository.save(m1);
                    }
                    
                    MenuItem m2 = existingItems.stream().filter(m -> m.getName().equals("Trà Đào Cam Sả")).findFirst().orElse(null);
                    if (m2 == null) {
                        m2 = new MenuItem();
                        m2.setRestaurant(restProfile);
                        m2.setName("Trà Đào Cam Sả");
                        m2.setDescription("Trà đào thơm ngon, mát lạnh kết hợp lát đào và cam tươi.");
                        m2.setPrice(39000.0);
                        m2.setAvailable(true);
                        m2.setSoldCount(98);
                        m2 = menuItemRepository.save(m2);
                    }
                    
                    MenuItem m3 = existingItems.stream().filter(m -> m.getName().equals("Bánh Mì Thịt Nướng")).findFirst().orElse(null);
                    if (m3 == null) {
                        m3 = new MenuItem();
                        m3.setRestaurant(restProfile);
                        m3.setName("Bánh Mì Thịt Nướng");
                        m3.setDescription("Bánh mì giòn kẹp thịt nướng xiên thơm ngon.");
                        m3.setPrice(25000.0);
                        m3.setAvailable(true);
                        m3.setSoldCount(84);
                        m3 = menuItemRepository.save(m3);
                    }
                    
                    MenuItem m4 = existingItems.stream().filter(m -> m.getName().equals("Cà Phê Đen Đá")).findFirst().orElse(null);
                    if (m4 == null) {
                        m4 = new MenuItem();
                        m4.setRestaurant(restProfile);
                        m4.setName("Cà Phê Đen Đá");
                        m4.setDescription("Cà phê đen nguyên chất pha phin.");
                        m4.setPrice(22000.0);
                        m4.setAvailable(true);
                        m4.setSoldCount(60);
                        m4 = menuItemRepository.save(m4);
                    }
                    
                    MenuItem m5 = existingItems.stream().filter(m -> m.getName().equals("Bánh Croissant Bơ Tỏi")).findFirst().orElse(null);
                    if (m5 == null) {
                        m5 = new MenuItem();
                        m5.setRestaurant(restProfile);
                        m5.setName("Bánh Croissant Bơ Tỏi");
                        m5.setDescription("Bánh sừng bò ngập vị bơ tỏi thơm lừng.");
                        m5.setPrice(35000.0);
                        m5.setAvailable(true);
                        m5.setSoldCount(12);
                        m5 = menuItemRepository.save(m5);
                    }

                    // Create Orders and OrderItems over the past 14 days
                    java.time.LocalDateTime nowTime = java.time.LocalDateTime.now();
                    java.util.Random rand = new java.util.Random(42);
                    MenuItem[] itemsList = {m1, m2, m3, m4, m5};
                    
                    for (int dayOffset = 13; dayOffset >= 0; dayOffset--) {
                        int ordersToday = 2 + rand.nextInt(2);
                        for (int o = 0; o < ordersToday; o++) {
                            FoodOrder order = new FoodOrder();
                            order.setCustomer(customerProfile);
                            order.setRestaurant(restProfile);
                            order.setDriver(driverProfile);
                            order.setOrderTime(nowTime.minusDays(dayOffset).minusHours(rand.nextInt(12)));
                            
                            boolean isCompleted = rand.nextDouble() < 0.9;
                            order.setStatus(isCompleted ? OrderStatus.COMPLETED : OrderStatus.CANCELLED);
                            
                            MenuItem selectedItem = itemsList[rand.nextInt(itemsList.length)];
                            int quantity = 1 + rand.nextInt(3);
                            
                            double itemTotal = selectedItem.getPrice() * quantity;
                            order.setTotalAmount(itemTotal + 15000.0);
                            order.setShippingFee(15000.0);
                            order.setRestaurantLat(10.772500);
                            order.setRestaurantLng(106.669500);
                            order.setDeliveryLat(10.762913); 
                            order.setDeliveryLng(106.682171);
                            
                            order = foodOrderRepository.save(order);
                            
                            OrderItem orderItem = new OrderItem();
                            orderItem.setOrder(order);
                            orderItem.setMenuItem(selectedItem);
                            orderItem.setQuantity(quantity);
                            orderItem.setPriceAtTimeOfOrder(selectedItem.getPrice());
                            orderItemRepository.save(orderItem);
                            
                            if (isCompleted && rand.nextDouble() < 0.6) {
                                Review review = new Review();
                                review.setOrder(order);
                                int rating = 3 + rand.nextInt(3); 
                                review.setRating(rating);
                                
                                String[] positiveComments = {
                                    "Đồ uống rất ngon, giao hàng nhanh chóng!",
                                    "Cà phê sữa đá đậm đà, bánh mì giòn.",
                                    "Trà đào cam sả thanh mát, sẽ ủng hộ tiếp.",
                                    "Chất lượng tuyệt vời, đóng gói rất cẩn thận.",
                                    "Món ăn ngon hợp vệ sinh."
                                };
                                String[] neutralComments = {
                                    "Cà phê hơi ngọt so với khẩu vị của mình.",
                                    "Giao hàng hơi lâu chút nhưng đồ uống vẫn ngon.",
                                    "Tạm được, không có gì quá nổi bật."
                                };
                                
                                String comment = rating >= 4 ? 
                                    positiveComments[rand.nextInt(positiveComments.length)] : 
                                    neutralComments[rand.nextInt(neutralComments.length)];
                                    
                                review.setComment(comment);
                                review.setOriginalComment(comment);
                                review.setCreatedAt(order.getOrderTime().plusMinutes(30 + rand.nextInt(60)));
                                
                                if (rand.nextDouble() < 0.3) {
                                    review.setRestaurantReply("Cảm ơn quý khách đã tin tưởng và ủng hộ The Coffee House ạ!");
                                    review.setRepliedAt(review.getCreatedAt().plusHours(1 + rand.nextInt(5)));
                                }
                                
                                if (rand.nextDouble() < 0.25) {
                                    review.setImageUrl("https://images.unsplash.com/photo-1541167760496-1628856ab772?w=500&q=80");
                                }
                                
                                reviewRepository.save(review);
                            }
                        }
                    }
                    System.out.println("✨ Seed data injected successfully!");
                }

                System.out.println("Data initialization completed.");
            } catch (Exception e) {
                System.out.println("Data initialization error: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
