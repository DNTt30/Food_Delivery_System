package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurant")
@SuppressWarnings("null")
public class RestaurantApiController {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final MenuItemRepository menuItemRepository;
    private final FoodOrderRepository foodOrderRepository;

    public RestaurantApiController(UserRepository userRepository,
                                   RestaurantProfileRepository restaurantProfileRepository,
                                   MenuItemRepository menuItemRepository,
                                   FoodOrderRepository foodOrderRepository) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.menuItemRepository = menuItemRepository;
        this.foodOrderRepository = foodOrderRepository;
    }

    private RestaurantProfile getAuthenticatedRestaurant(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || user.getRole() != Role.RESTAURANT) return null;
        return restaurantProfileRepository.findByUser(user).orElse(null);
    }

    // 1. Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(authentication);
        if (restaurant == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<FoodOrder> orders = foodOrderRepository.findByRestaurant(restaurant);
        long newOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long completedOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        double revenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(FoodOrder::getTotalAmount)
                .sum();

        return ResponseEntity.ok(Map.of(
                "totalNewOrders", newOrders,
                "totalCompletedOrders", completedOrders,
                "todayRevenue", revenue
        ));
    }

    // 2. Menu Management
    @GetMapping("/menu")
    public ResponseEntity<?> getMenu(Authentication authentication) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(authentication);
        if (restaurant == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<MenuItem> menuItems = menuItemRepository.findByRestaurant(restaurant);
        List<MenuItemDTO> dtos = menuItems.stream().map(m -> new MenuItemDTO(
                m.getId(), m.getName(), m.getDescription(), m.getPrice(), m.getImageUrl(), m.isAvailable()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/menu")
    public ResponseEntity<?> createMenuItem(Authentication authentication, @RequestBody MenuItemDTO dto) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(authentication);
        if (restaurant == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setName(dto.name);
        item.setDescription(dto.description);
        item.setPrice(dto.price);
        item.setImageUrl(dto.imageUrl != null ? dto.imageUrl : "https://via.placeholder.com/300x200");
        item.setAvailable(dto.isAvailable);
        menuItemRepository.save(item);

        return ResponseEntity.ok(Map.of("message", "Item created successfully"));
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<?> updateMenuItem(Authentication authentication, @PathVariable Long id, @RequestBody MenuItemDTO dto) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(authentication);
        if (restaurant == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MenuItem item = menuItemRepository.findById(id).orElse(null);
        if (item == null || !item.getRestaurant().getId().equals(restaurant.getId())) {
            return ResponseEntity.notFound().build();
        }

        item.setName(dto.name);
        item.setDescription(dto.description);
        item.setPrice(dto.price);
        if (dto.imageUrl != null) item.setImageUrl(dto.imageUrl);
        item.setAvailable(dto.isAvailable);
        menuItemRepository.save(item);

        return ResponseEntity.ok(Map.of("message", "Item updated successfully"));
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(Authentication authentication, @PathVariable Long id) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(authentication);
        if (restaurant == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MenuItem item = menuItemRepository.findById(id).orElse(null);
        if (item == null || !item.getRestaurant().getId().equals(restaurant.getId())) {
            return ResponseEntity.notFound().build();
        }

        menuItemRepository.delete(item);
        return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
    }

    // 3. Orders Management
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(Authentication authentication) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(authentication);
        if (restaurant == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<FoodOrder> orders = foodOrderRepository.findByRestaurant(restaurant);
        // Lấy PENDING và PREPARING để hiển thị trong UI xử lý
        List<OrderDTO> dtos = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.PREPARING)
                .map(o -> {
                    List<OrderItemDTO> items = o.getOrderItems().stream().map(oi -> new OrderItemDTO(
                            oi.getMenuItem().getName(), oi.getQuantity()
                    )).collect(Collectors.toList());
                    return new OrderDTO(o.getId(), o.getCustomer().getUser().getFullName(), items, o.getStatus().name());
                }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(Authentication authentication, @PathVariable Long id, @RequestBody Map<String, String> body) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(authentication);
        if (restaurant == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        FoodOrder order = foodOrderRepository.findById(id).orElse(null);
        if (order == null || !order.getRestaurant().getId().equals(restaurant.getId())) {
            return ResponseEntity.notFound().build();
        }

        try {
            OrderStatus newStatus = OrderStatus.valueOf(body.get("status"));
            order.setStatus(newStatus);
            foodOrderRepository.save(order);
            return ResponseEntity.ok(Map.of("message", "Order status updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        }
    }

    // DTOs
    public static class MenuItemDTO {
        public Long id;
        public String name;
        public String description;
        public Double price;
        public String imageUrl;
        public boolean isAvailable;

        public MenuItemDTO() {}

        public MenuItemDTO(Long id, String name, String description, Double price, String imageUrl, boolean isAvailable) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.imageUrl = imageUrl;
            this.isAvailable = isAvailable;
        }
    }

    public static class OrderItemDTO {
        public String itemName;
        public int quantity;

        public OrderItemDTO(String itemName, int quantity) {
            this.itemName = itemName;
            this.quantity = quantity;
        }
    }

    public static class OrderDTO {
        public Long id;
        public String customerName;
        public List<OrderItemDTO> items;
        public String status;

        public OrderDTO(Long id, String customerName, List<OrderItemDTO> items, String status) {
            this.id = id;
            this.customerName = customerName;
            this.items = items;
            this.status = status;
        }
    }
}
