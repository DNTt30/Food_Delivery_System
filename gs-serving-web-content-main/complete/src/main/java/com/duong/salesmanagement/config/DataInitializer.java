package com.duong.salesmanagement.config;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.ArrayList;

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
                                      ReviewRepository reviewRepository,
                                      FoodReviewRepository foodReviewRepository,
                                      CategoryRepository categoryRepository,
                                      PaymentRepository paymentRepository,
                                      ChatMessageRepository chatMessageRepository,
                                      OrderTrackingLocationRepository orderTrackingLocationRepository,
                                      NotificationRepository notificationRepository) {
        return args -> {
            try {
                // Seed Categories
                Category catTraChanh = categoryRepository.findAll().stream().filter(c -> c.getName().equalsIgnoreCase("Trà chanh")).findFirst().orElse(null);
                if (catTraChanh == null) {
                    catTraChanh = new Category();
                    catTraChanh.setName("Trà chanh");
                    catTraChanh.setDescription("Trà chanh tươi mát giải nhiệt");
                    catTraChanh = categoryRepository.save(catTraChanh);
                }

                Category catCom = categoryRepository.findAll().stream().filter(c -> c.getName().equalsIgnoreCase("Cơm")).findFirst().orElse(null);
                if (catCom == null) {
                    catCom = new Category();
                    catCom.setName("Cơm");
                    catCom.setDescription("Các món cơm bình dân & gia đình");
                    catCom = categoryRepository.save(catCom);
                }

                Category catTraSua = categoryRepository.findAll().stream().filter(c -> c.getName().equalsIgnoreCase("Trà sữa")).findFirst().orElse(null);
                if (catTraSua == null) {
                    catTraSua = new Category();
                    catTraSua.setName("Trà sữa");
                    catTraSua.setDescription("Trà sữa trân châu, thạch các loại");
                    catTraSua = categoryRepository.save(catTraSua);
                }

                Category catDoUong = categoryRepository.findAll().stream().filter(c -> c.getName().equalsIgnoreCase("Đồ uống")).findFirst().orElse(null);
                if (catDoUong == null) {
                    catDoUong = new Category();
                    catDoUong.setName("Đồ uống");
                    catDoUong.setDescription("Cà phê, trà trái cây giải nhiệt");
                    catDoUong = categoryRepository.save(catDoUong);
                }

                Category catAnVat = categoryRepository.findAll().stream().filter(c -> c.getName().equalsIgnoreCase("Ăn vặt")).findFirst().orElse(null);
                if (catAnVat == null) {
                    catAnVat = new Category();
                    catAnVat.setName("Ăn vặt");
                    catAnVat.setDescription("Bánh ngọt, bánh mì, đồ ăn nhẹ");
                    catAnVat = categoryRepository.save(catAnVat);
                }
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

                // 5. Ép nạp tọa độ cho đơn hàng #3
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

                // 7. Seed exactly 20 reviews in descending order for menu items
                if (foodReviewRepository.count() < 10) {
                    System.out.println("🌱 Seeding exactly 20 reviews in descending order...");
                    
                    // Clear existing reviews to ensure clean state of 20 reviews
                    reviewRepository.deleteAll();
                    foodReviewRepository.deleteAll();
                    
                    // Reset MenuItem ratings
                    for (MenuItem item : menuItemRepository.findAll()) {
                        item.setAverageRating(0.0);
                        item.setReviewCount(0);
                        menuItemRepository.save(item);
                    }

                    // Get all restaurants
                    List<RestaurantProfile> restaurants = restaurantProfileRepository.findAll();
                    if (!restaurants.isEmpty()) {
                        for (RestaurantProfile targetRest : restaurants) {
                            String[] itemNames = {
                                "Trà Chanh", "Cơm Rang Dưa Bò", "trà sữa", "Cà Phê Sữa Đá", 
                                "Trà Đào Cam Sả", "Bánh Mì Thịt Nướng", "Cà Phê Đen Đá", "Bánh Croissant Bơ Tỏi"
                            };
                            double[] prices = { 15000.0, 45000.0, 25000.0, 29000.0, 39000.0, 25000.0, 22000.0, 35000.0 };
                            
                            List<MenuItem> menuItemsList = new ArrayList<>();
                            List<MenuItem> existing = menuItemRepository.findByRestaurant(targetRest);
                            
                            for (int i = 0; i < itemNames.length; i++) {
                                final String name = itemNames[i];
                                MenuItem m = existing.stream().filter(item -> item.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                                if (m == null) {
                                    m = new MenuItem();
                                    m.setRestaurant(targetRest);
                                    m.setName(name);
                                    m.setDescription(name + " thơm ngon, chất lượng tuyệt vời.");
                                    m.setPrice(prices[i]);
                                    m.setAvailable(true);
                                    m.setSoldCount(50);
                                }
                                
                                if (name.equalsIgnoreCase("Trà Chanh")) {
                                    m.setCategory(catTraChanh);
                                } else if (name.equalsIgnoreCase("Cơm Rang Dưa Bò")) {
                                    m.setCategory(catCom);
                                } else if (name.equalsIgnoreCase("trà sữa")) {
                                    m.setCategory(catTraSua);
                                } else if (name.equalsIgnoreCase("Cà Phê Sữa Đá") || name.equalsIgnoreCase("Trà Đào Cam Sả") || name.equalsIgnoreCase("Cà Phê Đen Đá")) {
                                    m.setCategory(catDoUong);
                                } else if (name.equalsIgnoreCase("Bánh Mì Thịt Nướng") || name.equalsIgnoreCase("Bánh Croissant Bơ Tỏi")) {
                                    m.setCategory(catAnVat);
                                }
                                
                                m = menuItemRepository.save(m);
                                menuItemsList.add(m);
                            }

                            int[] targetItemIndices = {
                                0, 0, 0, 0, // Trà Chanh: 5, 5, 5, 5 -> Avg 5.0
                                1, 1, 1,    // Cơm Rang Dưa Bò: 5, 4, 5 -> Avg 4.7
                                2, 2, 2,    // trà sữa: 4, 5, 4 -> Avg 4.3
                                3, 3, 3,    // Cà Phê Sữa Đá: 4, 4, 4 -> Avg 4.0
                                4, 4,       // Trà Đào Cam Sả: 3, 4 -> Avg 3.5
                                5, 5,       // Bánh Mì Thịt Nướng: 3, 3 -> Avg 3.0
                                6, 6,       // Cà Phê Đen Đá: 2, 2 -> Avg 2.0
                                7           // Bánh Croissant Bơ Tỏi: 1 -> Avg 1.0
                            };
                            
                            int[] ratings = {
                                5, 5, 5, 5,
                                5, 4, 5,
                                4, 5, 4,
                                4, 4, 4,
                                3, 4,
                                3, 3,
                                2, 2,
                                1
                            };
                            
                            String[] comments = {
                                "Trà chanh rất mát, vị ngọt thanh cực ngon!",
                                "Ngon xuất sắc, trà chanh siêu đậm vị.",
                                "Giao hàng nhanh, nước uống ngon tuyệt.",
                                "Trà chanh uống phê lắm, vote 5 sao.",
                                "Cơm rang dưa bò giòn ngon, nhiều thịt bò.",
                                "Cơm rang hơi ít dưa một chút nhưng vẫn ngon.",
                                "Hương vị đậm đà, rất vừa miệng.",
                                "Trà sữa ngọt béo, trân châu dai giòn sần sật.",
                                "Trà sữa ngon tuyệt vời, giao hàng nhanh chóng.",
                                "Trà sữa đậm vị trà, rất ngon.",
                                "Cà phê sữa đá đậm đà, chuẩn vị Việt.",
                                "Ngon, tỉnh cả người, giao hàng thân thiện.",
                                "Uống cà phê sữa đá ở đây rất ổn.",
                                "Trà đào cam sả thơm phức, miếng đào to.",
                                "Ngon nhưng hơi ít đá, nói chung là tốt.",
                                "Bánh mì thịt nướng ăn tạm được, nhân đầy đặn.",
                                "Bánh mì hơi nguội một chút nhưng thịt thơm.",
                                "Cà phê đen hơi đắng quá so với mình.",
                                "Uống tạm được, chưa ấn tượng lắm.",
                                "Bánh croissant bơ tỏi hơi ngấy và bị cháy xém."
                            };

                            java.time.LocalDateTime seedTime = java.time.LocalDateTime.now().minusDays(10);
                            for (int k = 0; k < 20; k++) {
                                int itemIdx = targetItemIndices[k];
                                MenuItem selectedItem = menuItemsList.get(itemIdx);
                                int rating = ratings[k];
                                String comment = comments[k];

                                // Create FoodOrder
                                FoodOrder order = new FoodOrder();
                                order.setCustomer(customerProfile);
                                order.setRestaurant(targetRest);
                                order.setDriver(driverProfile);
                                order.setOrderTime(seedTime.plusHours(k * 4));
                                order.setStatus(OrderStatus.COMPLETED);
                                order.setTotalAmount(selectedItem.getPrice() + 15000.0);
                                order.setShippingFee(15000.0);
                                order.setRestaurantLat(10.772500);
                                order.setRestaurantLng(106.669500);
                                order.setDeliveryLat(10.762913); 
                                order.setDeliveryLng(106.682171);
                                order = foodOrderRepository.save(order);

                                // Create OrderItem
                                OrderItem orderItem = new OrderItem();
                                orderItem.setOrder(order);
                                orderItem.setMenuItem(selectedItem);
                                orderItem.setQuantity(1);
                                orderItem.setPriceAtTimeOfOrder(selectedItem.getPrice());
                                orderItemRepository.save(orderItem);

                                // Create restaurant Review
                                Review review = new Review();
                                review.setOrder(order);
                                review.setRating(rating);
                                review.setComment(comment);
                                review.setOriginalComment(comment);
                                review.setCreatedAt(order.getOrderTime().plusMinutes(30));
                                reviewRepository.save(review);
                            }
                        }
                    }
                    System.out.println("✨ Seed data injected successfully!");
                }

                // 8. Di cư (migrate) các đánh giá cũ sang bảng FoodReview & cập nhật averageRating/reviewCount cho MenuItem
                if (foodReviewRepository.count() == 0 && reviewRepository.count() > 0) {
                    System.out.println("🔄 Migrating existing restaurant reviews to food reviews...");
                    java.util.List<Review> allReviews = reviewRepository.findAll();
                    for (Review r : allReviews) {
                        FoodOrder order = r.getOrder();
                        if (order == null || order.getCustomer() == null || order.getCustomer().getUser() == null) continue;
                        java.util.List<OrderItem> orderItems = order.getOrderItems();
                        if (orderItems == null || orderItems.isEmpty()) continue;
                        
                        for (OrderItem oi : orderItems) {
                            MenuItem menuItem = oi.getMenuItem();
                            if (menuItem == null) continue;
                            
                            // Tạo FoodReview mới
                            FoodReview fr = new FoodReview();
                            fr.setMenuItem(menuItem);
                            fr.setCustomer(order.getCustomer().getUser());
                            fr.setRating(r.getRating());
                            fr.setComment(r.getComment());
                            fr.setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : java.time.LocalDateTime.now());
                            fr.setRatingLevel(FoodReview.getRatingLevelDescription(r.getRating()));
                            foodReviewRepository.save(fr);
                        }
                    }
                    System.out.println("✅ Food reviews migration completed!");
                }

                // 10. Tạo thêm 5 nhà hàng cùng món "Trà Chanh" nhưng khác số sao để đối chiếu (xóa đi tạo lại sạch sẽ mỗi lần chạy)
                System.out.println("🌱 Cleaning up old comparison restaurants...");
                for (int i = 1; i <= 5; i++) {
                    String username = "rest_tea_" + i;
                    userRepository.findByUsername(username).ifPresent(u -> {
                        restaurantProfileRepository.findByUser(u).ifPresent(rp -> {
                            // 1. Xóa Review liên quan đến nhà hàng trước (vì Review tham chiếu đến FoodOrder)
                            List<Review> reviews = reviewRepository.findByRestaurant(rp);
                            reviewRepository.deleteAll(reviews);

                            // 2. Xóa Order và OrderItem liên quan
                            List<FoodOrder> orders = foodOrderRepository.findByRestaurant(rp);
                            for (FoodOrder o : orders) {
                                List<OrderItem> orderItems = orderItemRepository.findByOrder(o);
                                if (orderItems != null && !orderItems.isEmpty()) {
                                    orderItemRepository.deleteAll(orderItems);
                                }
                                paymentRepository.findByOrder(o).ifPresent(paymentRepository::delete);
                                chatMessageRepository.deleteAll(chatMessageRepository.findByOrderIdOrderByCreatedAtAsc(o.getId()));
                                orderTrackingLocationRepository.deleteAll(orderTrackingLocationRepository.findByOrderIdOrderByTimestampAsc(o.getId()));
                                foodOrderRepository.delete(o);
                            }

                            // 3. Xóa MenuItem và FoodReview liên quan
                            List<MenuItem> items = menuItemRepository.findByRestaurant(rp);
                            for (MenuItem item : items) {
                                List<FoodReview> foodReviews = foodReviewRepository.findByMenuItemOrderByCreatedAtDesc(item);
                                foodReviewRepository.deleteAll(foodReviews);
                                menuItemRepository.delete(item);
                            }

                            restaurantProfileRepository.delete(rp);
                        });
                        notificationRepository.deleteByUser(u);
                        userRepository.delete(u);
                    });
                }

                // Flush deletes to DB to avoid unique constraints collision when inserts are executed
                restaurantProfileRepository.flush();
                userRepository.flush();

                System.out.println("🌱 Seeding comparison restaurants for 'Trà Chanh' search sorting demo...");
                String[] restNames = {
                    "High Tea House", 
                    "Green Garden Cafe", 
                    "Lemon Chill Zone", 
                    "Fresh & Fast Drink", 
                    "Lazy Coffee Shop"
                };
                
                int[][] ratingsData = {
                    { 5, 5 },       // Avg 5.0
                    { 5, 4 },       // Avg 4.5
                    { 4, 4 },       // Avg 4.0
                    { 3, 3 },       // Avg 3.0
                    { 2, 2 }        // Avg 2.0
                };

                for (int i = 0; i < restNames.length; i++) {
                    String username = "rest_tea_" + (i + 1);
                    User restUser = new User(username, "restaurant123", restNames[i] + " Owner", username + "@foodonl.com", Role.RESTAURANT);
                    restUser.setEnabled(true);

                    RestaurantProfile rp = new RestaurantProfile();
                    rp.setUser(restUser);
                    rp.setRestaurantName(restNames[i]);
                    rp.setAddress("Địa chỉ " + restNames[i]);
                    rp.setLatitude(10.772500 + (i * 0.001)); 
                    rp.setLongitude(106.669500 + (i * 0.001));
                    rp.setOpen(true);
                    rp.setAverageRating(0.0);
                    rp.setReviewCount(0);
                    restaurantProfileRepository.save(rp);

                    // Tạo món "Trà Chanh" cho nhà hàng này
                    MenuItem m = new MenuItem();
                    m.setRestaurant(rp);
                    m.setName("Trà Chanh");
                    m.setDescription("Trà chanh mát lạnh giải nhiệt mùa hè của " + restNames[i]);
                    m.setPrice(15000.0);
                    m.setCategory(catTraChanh);
                    m.setAvailable(true);
                    m.setSoldCount(10);
                    m.setAverageRating(0.0);
                    m.setReviewCount(0);
                    menuItemRepository.save(m);

                    // Tạo các đơn hàng COMPLETED và đánh giá cho món ăn/nhà hàng này để tạo số sao khác nhau
                    int[] ratings = ratingsData[i];
                    for (int j = 0; j < ratings.length; j++) {
                        int ratingVal = ratings[j];

                        FoodOrder order = new FoodOrder();
                        order.setCustomer(customerProfile);
                        order.setRestaurant(rp);
                        order.setDriver(driverProfile);
                        order.setOrderTime(java.time.LocalDateTime.now().minusDays(1));
                        order.setStatus(OrderStatus.COMPLETED);
                        order.setTotalAmount(15000.0 + 15000.0);
                        order.setShippingFee(15000.0);
                        order.setRestaurantLat(rp.getLatitude());
                        order.setRestaurantLng(rp.getLongitude());
                        order.setDeliveryLat(10.762913); 
                        order.setDeliveryLng(106.682171);
                        order = foodOrderRepository.save(order);

                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(order);
                        orderItem.setMenuItem(m);
                        orderItem.setQuantity(1);
                        orderItem.setPriceAtTimeOfOrder(15000.0);
                        orderItemRepository.save(orderItem);

                        Review review = new Review();
                        review.setOrder(order);
                        review.setRating(ratingVal);
                        review.setComment("Đánh giá " + ratingVal + " sao cho " + restNames[i]);
                        review.setOriginalComment("Đánh giá " + ratingVal + " sao cho " + restNames[i]);
                        review.setCreatedAt(order.getOrderTime().plusMinutes(30));
                        reviewRepository.save(review);

                        FoodReview fr = new FoodReview();
                        fr.setMenuItem(m);
                        fr.setCustomer(customerProfile.getUser());
                        fr.setRating(ratingVal);
                        fr.setComment("Món này " + ratingVal + " sao!");
                        fr.setCreatedAt(order.getOrderTime().plusMinutes(35));
                        fr.setRatingLevel(FoodReview.getRatingLevelDescription(ratingVal));
                        foodReviewRepository.save(fr);
                    }
                }
                System.out.println("✅ Comparison restaurants seeded successfully!");

                // 11. Cập nhật đánh giá của các nhà hàng hiện tại ("Test Restaurant Owner" và "Khoai Lang") để có số sao khác nhau
                System.out.println("🌱 Adjusting reviews for existing restaurants ('Test Restaurant Owner' & 'Khoai Lang')...");
                for (RestaurantProfile rp : restaurantProfileRepository.findAll()) {
                    String name = rp.getRestaurantName() != null ? rp.getRestaurantName().toLowerCase() : "";
                    String ownerName = (rp.getUser() != null && rp.getUser().getFullName() != null) ? rp.getUser().getFullName().toLowerCase() : "";
                    
                    if (name.contains("test restaurant owner") || ownerName.contains("test restaurant owner") || name.contains("coffee house")) {
                        System.out.println("⭐️ Setting 5-star ratings for: " + rp.getRestaurantName());
                        List<Review> reviews = reviewRepository.findByRestaurant(rp);
                        reviewRepository.deleteAll(reviews);
                        
                        List<MenuItem> items = menuItemRepository.findByRestaurant(rp);
                        for (MenuItem item : items) {
                            foodReviewRepository.deleteAll(foodReviewRepository.findByMenuItemOrderByCreatedAtDesc(item));
                        }

                        for (int j = 0; j < 20; j++) {
                            FoodOrder order = new FoodOrder();
                            order.setCustomer(customerProfile);
                            order.setRestaurant(rp);
                            order.setDriver(driverProfile);
                            order.setStatus(OrderStatus.COMPLETED);
                            order.setOrderTime(java.time.LocalDateTime.now().minusDays(2));
                            order.setTotalAmount(30000.0);
                            order = foodOrderRepository.save(order);

                            Review r = new Review();
                            r.setOrder(order);
                            r.setRating(5);
                            r.setComment("Đồ ăn rất ngon, 5 sao!");
                            r.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));
                            reviewRepository.save(r);

                            for (MenuItem item : items) {
                                FoodReview fr = new FoodReview();
                                fr.setMenuItem(item);
                                fr.setCustomer(customerProfile.getUser());
                                fr.setRating(5);
                                fr.setComment("Món này rất ngon!");
                                fr.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));
                                fr.setRatingLevel(FoodReview.getRatingLevelDescription(5));
                                foodReviewRepository.save(fr);
                            }
                        }
                    } else if (name.contains("khoai lang")) {
                        System.out.println("⭐️ Setting 3-star ratings for: " + rp.getRestaurantName());
                        List<Review> reviews = reviewRepository.findByRestaurant(rp);
                        reviewRepository.deleteAll(reviews);
                        
                        List<MenuItem> items = menuItemRepository.findByRestaurant(rp);
                        for (MenuItem item : items) {
                            foodReviewRepository.deleteAll(foodReviewRepository.findByMenuItemOrderByCreatedAtDesc(item));
                        }

                        for (int j = 0; j < 20; j++) {
                            FoodOrder order = new FoodOrder();
                            order.setCustomer(customerProfile);
                            order.setRestaurant(rp);
                            order.setDriver(driverProfile);
                            order.setStatus(OrderStatus.COMPLETED);
                            order.setOrderTime(java.time.LocalDateTime.now().minusDays(2));
                            order.setTotalAmount(30000.0);
                            order = foodOrderRepository.save(order);

                            Review r = new Review();
                            r.setOrder(order);
                            r.setRating(3);
                            r.setComment("Ăn bình thường, tạm được.");
                            r.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));
                            reviewRepository.save(r);

                            for (MenuItem item : items) {
                                FoodReview fr = new FoodReview();
                                fr.setMenuItem(item);
                                fr.setCustomer(customerProfile.getUser());
                                fr.setRating(3);
                                fr.setComment("Món này bình thường.");
                                fr.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));
                                fr.setRatingLevel(FoodReview.getRatingLevelDescription(3));
                                foodReviewRepository.save(fr);
                            }
                        }
                    }
                }

                // 9. Đồng bộ lại averageRating và reviewCount từ dữ liệu thực tế trong DB cho RestaurantProfile và MenuItem
                System.out.println("🔄 Synchronizing Restaurant and MenuItem ratings from database reviews...");
                for (RestaurantProfile rest : restaurantProfileRepository.findAll()) {
                    Double avgRating = foodReviewRepository.getAverageRatingByRestaurantId(rest.getId());
                    Long count = foodReviewRepository.countByRestaurantId(rest.getId());
                    rest.setReviewCount(count != null ? count.intValue() : 0);
                    rest.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
                    restaurantProfileRepository.save(rest);
                }

                for (MenuItem item : menuItemRepository.findAll()) {
                    Double avgRating = foodReviewRepository.getAverageRatingForMenuItem(item.getId());
                    Long count = foodReviewRepository.countByMenuItemId(item.getId());
                    item.setReviewCount(count != null ? count.intValue() : 0);
                    item.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
                    menuItemRepository.save(item);
                }
                System.out.println("✅ Ratings and review counts synchronized successfully!");

                System.out.println("Data initialization completed.");
            } catch (Exception e) {
                System.out.println("Data initialization error: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
