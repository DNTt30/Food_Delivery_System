package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import com.duong.salesmanagement.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@SuppressWarnings("null")
public class CustomerApiController {

    private final RestaurantProfileRepository restaurantProfileRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;

    public CustomerApiController(RestaurantProfileRepository restaurantProfileRepository,
                                 MenuItemRepository menuItemRepository,
                                 OrderService orderService,
                                 UserRepository userRepository,
                                 CustomerProfileRepository customerProfileRepository) {
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.customerProfileRepository = customerProfileRepository;
    }

    private CustomerProfile getAuthenticatedCustomer(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || user.getRole() != Role.CUSTOMER) return null;
        return customerProfileRepository.findByUser(user).orElseGet(() -> {
            CustomerProfile p = new CustomerProfile();
            p.setUser(user);
            return customerProfileRepository.save(p);
        });
    }

    // UC-05: Tìm kiếm nhà hàng/món ăn
    @GetMapping("/restaurants")
    public ResponseEntity<?> getRestaurants(@RequestParam(required = false) String search) {
        List<RestaurantProfile> restaurants;
        if (search != null && !search.trim().isEmpty()) {
            restaurants = restaurantProfileRepository.searchByKeyword(search.trim());
        } else {
            restaurants = restaurantProfileRepository.findAll();
        }

        List<RestaurantDTO> dtos = restaurants.stream().map(r -> new RestaurantDTO(
                r.getId(),
                r.getRestaurantName(),
                r.getAddress(),
                r.getAverageRating(),
                r.isOpen(),
                r.getBannerUrl()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // UC-06: Xem thực đơn nhà hàng
    @GetMapping("/restaurants/{id}")
    public ResponseEntity<?> getRestaurantDetail(@PathVariable Long id) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(id).orElse(null);
        if (restaurant == null) return ResponseEntity.notFound().build();

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantAndIsAvailableTrue(restaurant);
        List<MenuItemDTO> menuItemDTOs = menuItems.stream().map(m -> new MenuItemDTO(
                m.getId(), m.getName(), m.getDescription(), m.getPrice(), m.getImageUrl()
        )).collect(Collectors.toList());

        RestaurantDetailDTO detailDTO = new RestaurantDetailDTO(
                restaurant.getId(),
                restaurant.getRestaurantName(),
                restaurant.getAddress(),
                restaurant.getAverageRating(),
                restaurant.isOpen(),
                restaurant.getBannerUrl(),
                menuItemDTOs
        );
        return ResponseEntity.ok(detailDTO);
    }

    // UC-08: Đặt đơn hàng
    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(Authentication authentication, @RequestBody PlaceOrderRequest request) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        RestaurantProfile restaurant = restaurantProfileRepository.findById(request.restaurantId).orElse(null);
        if (restaurant == null) return ResponseEntity.notFound().build();
        if (!restaurant.isOpen())
            return ResponseEntity.badRequest().body(Map.of("error", "Nhà hàng hiện đang đóng cửa"));

        try {
            FoodOrder order = orderService.createOrder(customer, restaurant, request.items, request.deliveryAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Đặt hàng thành công!",
                    "orderId", order.getId(),
                    "totalAmount", order.getTotalAmount()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // UC-10: Lịch sử & theo dõi đơn hàng
    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<FoodOrder> orders = orderService.getCustomerOrders(customer);
        List<OrderSummaryDTO> dtos = orders.stream().map(o -> {
            List<OrderItemDTO> items = o.getOrderItems() == null ? List.of() :
                    o.getOrderItems().stream().map(oi -> new OrderItemDTO(
                            oi.getMenuItem().getName(), oi.getQuantity(), oi.getPriceAtTimeOfOrder()
                    )).collect(Collectors.toList());
            return new OrderSummaryDTO(
                    o.getId(),
                    o.getRestaurant().getRestaurantName(),
                    o.getRestaurant().getBannerUrl(),
                    o.getStatus().name(),
                    o.getTotalAmount(),
                    o.getOrderTime() != null ? o.getOrderTime().toString() : "",
                    o.getDeliveryAddress(),
                    items
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetail(Authentication authentication, @PathVariable Long id) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        FoodOrder order = orderService.getOrderById(id).orElse(null);
        if (order == null || !order.getCustomer().getId().equals(customer.getId()))
            return ResponseEntity.notFound().build();

        List<OrderItemDTO> items = order.getOrderItems() == null ? List.of() :
                order.getOrderItems().stream().map(oi -> new OrderItemDTO(
                        oi.getMenuItem().getName(), oi.getQuantity(), oi.getPriceAtTimeOfOrder()
                )).collect(Collectors.toList());

        String driverName = (order.getDriver() != null) ? order.getDriver().getUser().getFullName() : null;
        String driverPhone = (order.getDriver() != null) ? order.getDriver().getPhoneNumber() : null;

        OrderDetailDTO dto = new OrderDetailDTO(
                order.getId(),
                order.getRestaurant().getRestaurantName(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getOrderTime() != null ? order.getOrderTime().toString() : "",
                order.getDeliveryAddress(),
                driverName,
                driverPhone,
                items
        );
        return ResponseEntity.ok(dto);
    }

    // UC-11: Hủy đơn hàng
    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(Authentication authentication, @PathVariable Long id) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            orderService.cancelOrder(id, customer);
            return ResponseEntity.ok(Map.of("message", "Đã hủy đơn hàng thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // UC-12: Đánh giá đơn hàng
    @PostMapping("/orders/{id}/review")
    public ResponseEntity<?> reviewOrder(Authentication authentication, @PathVariable Long id,
                                          @RequestBody ReviewRequest request) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (request.rating < 1 || request.rating > 5)
            return ResponseEntity.badRequest().body(Map.of("error", "Đánh giá phải từ 1 đến 5 sao"));

        try {
            orderService.reviewOrder(id, customer, request.rating, request.comment);
            return ResponseEntity.ok(Map.of("message", "Cảm ơn bạn đã đánh giá!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- DTOs ----
    public static class RestaurantDTO {
        public Long id;
        public String name;
        public String address;
        public Double rating;
        public boolean isOpen;
        public String imageUrl;

        public RestaurantDTO(Long id, String name, String address, Double rating, boolean isOpen, String imageUrl) {
            this.id = id; this.name = name; this.address = address;
            this.rating = rating; this.isOpen = isOpen; this.imageUrl = imageUrl;
        }
    }

    public static class MenuItemDTO {
        public Long id;
        public String name;
        public String description;
        public Double price;
        public String imageUrl;

        public MenuItemDTO(Long id, String name, String description, Double price, String imageUrl) {
            this.id = id; this.name = name; this.description = description;
            this.price = price; this.imageUrl = imageUrl;
        }
    }

    public static class RestaurantDetailDTO {
        public Long id;
        public String name;
        public String address;
        public Double rating;
        public boolean isOpen;
        public String bannerUrl;
        public List<MenuItemDTO> menuItems;

        public RestaurantDetailDTO(Long id, String name, String address, Double rating,
                                   boolean isOpen, String bannerUrl, List<MenuItemDTO> menuItems) {
            this.id = id; this.name = name; this.address = address; this.rating = rating;
            this.isOpen = isOpen; this.bannerUrl = bannerUrl; this.menuItems = menuItems;
        }
    }

    public static class OrderItemDTO {
        public String itemName;
        public int quantity;
        public Double price;

        public OrderItemDTO(String itemName, int quantity, Double price) {
            this.itemName = itemName; this.quantity = quantity; this.price = price;
        }
    }

    public static class OrderSummaryDTO {
        public Long id;
        public String restaurantName;
        public String restaurantImage;
        public String status;
        public Double totalAmount;
        public String orderTime;
        public String deliveryAddress;
        public List<OrderItemDTO> items;

        public OrderSummaryDTO(Long id, String restaurantName, String restaurantImage, String status,
                               Double totalAmount, String orderTime, String deliveryAddress, List<OrderItemDTO> items) {
            this.id = id; this.restaurantName = restaurantName; this.restaurantImage = restaurantImage;
            this.status = status; this.totalAmount = totalAmount; this.orderTime = orderTime;
            this.deliveryAddress = deliveryAddress; this.items = items;
        }
    }

    public static class OrderDetailDTO extends OrderSummaryDTO {
        public String driverName;
        public String driverPhone;

        public OrderDetailDTO(Long id, String restaurantName, String status, Double totalAmount,
                              String orderTime, String deliveryAddress, String driverName,
                              String driverPhone, List<OrderItemDTO> items) {
            super(id, restaurantName, null, status, totalAmount, orderTime, deliveryAddress, items);
            this.driverName = driverName;
            this.driverPhone = driverPhone;
        }
    }

    public static class PlaceOrderRequest {
        public Long restaurantId;
        public List<OrderService.OrderItemRequest> items;
        public String deliveryAddress;
        public String paymentMethod;
    }

    public static class ReviewRequest {
        public int rating;
        public String comment;
    }
}
