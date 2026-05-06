package com.duong.salesmanagement.config;

import com.duong.salesmanagement.model.Role;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.repository.UserRepository;
import com.duong.salesmanagement.model.MenuItem;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.CustomerProfile;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.OrderItem;
import com.duong.salesmanagement.model.OrderStatus;
import com.duong.salesmanagement.repository.MenuItemRepository;
import com.duong.salesmanagement.repository.RestaurantProfileRepository;
import com.duong.salesmanagement.repository.CustomerProfileRepository;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;


/**
 * Khởi tạo dữ liệu mẫu khi DB trống.
 * Các tài khoản seed đã được kích hoạt sẵn (enabled = true)
 * vì không cần xác minh email.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final FoodOrderRepository foodOrderRepository;

    public DataInitializer(UserRepository userRepository, 
                           PasswordEncoder passwordEncoder,
                           RestaurantProfileRepository restaurantProfileRepository,
                           MenuItemRepository menuItemRepository,
                           CustomerProfileRepository customerProfileRepository,
                           FoodOrderRepository foodOrderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.menuItemRepository = menuItemRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.foodOrderRepository = foodOrderRepository;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            System.out.println("No users found. Initializing default accounts...");

            saveSeedUser("admin",      "admin123",      "System Administrator",   "admin@food.dev",      Role.ADMIN);
            saveSeedUser("customer",   "customer123",   "John Doe – Customer",    "customer@food.dev",   Role.CUSTOMER);
            saveSeedUser("restaurant", "restaurant123", "Tasty Food Restaurant",  "restaurant@food.dev", Role.RESTAURANT);
            saveSeedUser("driver",     "driver123",     "Mike – Driver",           "driver@food.dev",     Role.DRIVER);

            System.out.println("Default accounts created successfully!");
        } else {
            System.out.println("Database already has users. Forcefully updating default accounts to ensure they work...");
            forceUpdateUser("admin", "admin123");
            forceUpdateUser("customer", "customer123");
            forceUpdateUser("restaurant", "restaurant123");
            forceUpdateUser("driver", "driver123");
        }

        // Tạo dữ liệu cho nhà hàng mẫu
        userRepository.findByUsername("restaurant").ifPresent(user -> {
            RestaurantProfile restaurantProfile = restaurantProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        RestaurantProfile profile = new RestaurantProfile();
                        profile.setUser(user);
                        profile.setRestaurantName("Tasty Food Restaurant");
                        profile.setAddress("123 Food Street, Food City");
                        profile.setOpen(true);
                        profile.setAverageRating(4.5);
                        return restaurantProfileRepository.save(profile);
                    });

            if (menuItemRepository.findByRestaurant(restaurantProfile).isEmpty()) {
                System.out.println("No menu items found. Seeding menu items...");
                saveMenuItem(restaurantProfile, "Cơm Sườn Ninh Kiều", "Cơm tấm dẻo, sườn nướng mỡ hành thơm lừng.", 35000.0, "https://via.placeholder.com/300x200");
                saveMenuItem(restaurantProfile, "Trà Sữa Trân Châu", "Trà sữa vị ô long ngọt thanh, trân châu dai giòn.", 25000.0, "https://via.placeholder.com/300x200");
                saveMenuItem(restaurantProfile, "Bún Bò Huế", "Bún bò Huế chính gốc, nước dùng đậm đà.", 45000.0, "https://via.placeholder.com/300x200");
            }

            // Tạo hồ sơ khách hàng mẫu
            userRepository.findByUsername("customer").ifPresent(customerUser -> {
                CustomerProfile customerProfile = customerProfileRepository.findByUser(customerUser)
                        .orElseGet(() -> {
                            CustomerProfile profile = new CustomerProfile();
                            profile.setUser(customerUser);
                            profile.setPhoneNumber("0987654321");
                            profile.setDeliveryAddress("123 Customer Ave, Customer City");
                            return customerProfileRepository.save(profile);
                        });

                // Seed dummy orders
                if (foodOrderRepository.findByRestaurant(restaurantProfile).isEmpty()) {
                    System.out.println("Seeding dummy orders...");
                    
                    MenuItem item1 = menuItemRepository.findByRestaurant(restaurantProfile).get(0);
                    MenuItem item2 = menuItemRepository.findByRestaurant(restaurantProfile).get(1);

                    // Order 1: PENDING
                    FoodOrder order1 = new FoodOrder();
                    order1.setCustomer(customerProfile);
                    order1.setRestaurant(restaurantProfile);
                    order1.setStatus(OrderStatus.PENDING);
                    order1.setOrderTime(LocalDateTime.now().minusMinutes(10));
                    order1.setDeliveryAddress("123 Customer Ave, Customer City");
                    order1.setTotalAmount(item1.getPrice() * 2);
                    
                    OrderItem oi1 = new OrderItem();
                    oi1.setOrder(order1);
                    oi1.setMenuItem(item1);
                    oi1.setQuantity(2);
                    oi1.setPriceAtTimeOfOrder(item1.getPrice());
                    
                    ArrayList<OrderItem> items1 = new ArrayList<>();
                    items1.add(oi1);
                    order1.setOrderItems(items1);
                    foodOrderRepository.save(order1);

                    // Order 2: PREPARING
                    FoodOrder order2 = new FoodOrder();
                    order2.setCustomer(customerProfile);
                    order2.setRestaurant(restaurantProfile);
                    order2.setStatus(OrderStatus.PREPARING);
                    order2.setOrderTime(LocalDateTime.now().minusMinutes(30));
                    order2.setDeliveryAddress("456 Customer Ave, Customer City");
                    order2.setTotalAmount(item2.getPrice() * 1);
                    
                    OrderItem oi2 = new OrderItem();
                    oi2.setOrder(order2);
                    oi2.setMenuItem(item2);
                    oi2.setQuantity(1);
                    oi2.setPriceAtTimeOfOrder(item2.getPrice());
                    
                    ArrayList<OrderItem> items2 = new ArrayList<>();
                    items2.add(oi2);
                    order2.setOrderItems(items2);
                    foodOrderRepository.save(order2);

                    // Order 3: COMPLETED
                    FoodOrder order3 = new FoodOrder();
                    order3.setCustomer(customerProfile);
                    order3.setRestaurant(restaurantProfile);
                    order3.setStatus(OrderStatus.COMPLETED);
                    order3.setOrderTime(LocalDateTime.now().minusHours(2));
                    order3.setDeliveryAddress("789 Customer Ave, Customer City");
                    order3.setTotalAmount(item1.getPrice() * 1 + item2.getPrice() * 1);
                    
                    OrderItem oi3 = new OrderItem();
                    oi3.setOrder(order3);
                    oi3.setMenuItem(item1);
                    oi3.setQuantity(1);
                    oi3.setPriceAtTimeOfOrder(item1.getPrice());

                    OrderItem oi4 = new OrderItem();
                    oi4.setOrder(order3);
                    oi4.setMenuItem(item2);
                    oi4.setQuantity(1);
                    oi4.setPriceAtTimeOfOrder(item2.getPrice());
                    
                    ArrayList<OrderItem> items3 = new ArrayList<>();
                    items3.add(oi3);
                    items3.add(oi4);
                    order3.setOrderItems(items3);
                    foodOrderRepository.save(order3);
                }
            });
        });
    }

    private void saveMenuItem(RestaurantProfile restaurant, String name, String desc, Double price, String imageUrl) {
        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setName(name);
        item.setDescription(desc);
        item.setPrice(price);
        item.setImageUrl(imageUrl);
        item.setAvailable(true);
        menuItemRepository.save(item);
    }

    private void forceUpdateUser(String username, String rawPassword) {
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setEnabled(true); // Ensure it's enabled
            userRepository.save(u);
            System.out.println("Force-updated password for user: " + username);
        });
    }

    /** Tạo và lưu trực tiếp để tránh cảnh báo Null safety của IDE */
    private void saveSeedUser(String username, String rawPassword,
                              String fullName, String email, Role role) {
        User u = new User(username, passwordEncoder.encode(rawPassword), fullName, email, role);
        u.setEnabled(true);
        userRepository.save(u);
    }
}
