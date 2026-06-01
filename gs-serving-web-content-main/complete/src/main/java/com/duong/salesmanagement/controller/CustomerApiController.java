package com.duong.salesmanagement.controller;


import com.duong.salesmanagement.service.IOrderService;
import com.duong.salesmanagement.service.IShippingCalculationService;
import com.duong.salesmanagement.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.duong.salesmanagement.model.CustomerProfile;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.MenuItem;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.Review;
import com.duong.salesmanagement.model.Role;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.model.Voucher;
import com.duong.salesmanagement.repository.CustomerProfileRepository;
import com.duong.salesmanagement.repository.MenuItemRepository;
import com.duong.salesmanagement.repository.RestaurantProfileRepository;
import com.duong.salesmanagement.repository.ReviewRepository;
import com.duong.salesmanagement.repository.UserRepository;
import com.duong.salesmanagement.repository.VoucherRepository;

@RestController
@RequestMapping("/api/customer")

public class CustomerApiController {

    private final RestaurantProfileRepository restaurantProfileRepository;
    private final MenuItemRepository menuItemRepository;
    private final IOrderService orderService;
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final VoucherRepository voucherRepository;
    private final ReviewRepository reviewRepository;
    private final IShippingCalculationService shippingCalculationService;
    private final com.duong.salesmanagement.service.GeocodingService geocodingService;

    public CustomerApiController(RestaurantProfileRepository restaurantProfileRepository,
                                 MenuItemRepository menuItemRepository,
                                 IOrderService orderService,
                                 UserRepository userRepository,
                                 CustomerProfileRepository customerProfileRepository,
                                 VoucherRepository voucherRepository,
                                 ReviewRepository reviewRepository,
                                 IShippingCalculationService shippingCalculationService,
                                 com.duong.salesmanagement.service.GeocodingService geocodingService) {
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.voucherRepository = voucherRepository;
        this.reviewRepository = reviewRepository;
        this.shippingCalculationService = shippingCalculationService;
        this.geocodingService = geocodingService;
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

        List<RestaurantDTO> dtos = restaurants.stream()
                .filter(r -> r.getUser() != null && r.getUser().isEnabled())
                .map(r -> new RestaurantDTO(
                r.getId(),
                r.getRestaurantName(),
                r.getAddress(),
                r.getAverageRating(),
                r.isOpen(),
                r.getBannerUrl(),
                r.getReviewCount()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // UC: Món Bán Chạy
    @GetMapping("/top-items")
    public ResponseEntity<?> getTopItems() {
        List<MenuItem> items = menuItemRepository.findTop10ByIsAvailableTrueOrderBySoldCountDesc();
        List<Map<String, Object>> result = items.stream()
                .filter(m -> m.getRestaurant() != null && m.getRestaurant().getUser() != null && m.getRestaurant().getUser().isEnabled())
                .map(m -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("price", m.getPrice());
            map.put("imageUrl", m.getImageUrl());
            map.put("restaurantId", m.getRestaurant().getId());
            map.put("restaurantName", m.getRestaurant().getRestaurantName());
            map.put("soldCount", m.getSoldCount() != null ? m.getSoldCount() : 0);
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // UC-06: Xem thực đơn nhà hàng
    @GetMapping("/restaurants/{id}")
    public ResponseEntity<?> getRestaurantDetail(@PathVariable Long id) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(id).orElse(null);
        if (restaurant == null) return ResponseEntity.notFound().build();

        List<MenuItem> menuItems = menuItemRepository.findByRestaurant(restaurant);
        List<MenuItemDTO> menuItemDTOs = menuItems.stream().map(m -> new MenuItemDTO(
                m.getId(), m.getName(), m.getDescription(), m.getPrice(), m.getImageUrl(), m.getVideoUrl(), m.isAvailable()
        )).collect(Collectors.toList());

        RestaurantDetailDTO detailDTO = new RestaurantDetailDTO(
                restaurant.getId(),
                restaurant.getRestaurantName(),
                restaurant.getAddress(),
                restaurant.getAverageRating(),
                restaurant.isOpen(),
                restaurant.getBannerUrl(),
                restaurant.getReviewCount(),
                menuItemDTOs
        );
        return ResponseEntity.ok(detailDTO);
    }

    // UC-06.1: Xem đánh giá của nhà hàng
    @GetMapping("/restaurants/{id}/reviews")
    public ResponseEntity<?> getRestaurantReviews(@PathVariable Long id) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(id).orElse(null);
        if (restaurant == null) return ResponseEntity.notFound().build();

        List<Review> reviews = reviewRepository.findByRestaurant(restaurant);
        List<ReviewDTO> reviewDTOs = reviews.stream().map(r -> {
            List<String> items = r.getOrder().getOrderItems() != null ? r.getOrder().getOrderItems().stream()
                    .map(oi -> oi.getMenuItem().getName() + " (x" + oi.getQuantity() + ")")
                    .collect(Collectors.toList()) : List.of();
            return new ReviewDTO(
                    r.getOrder().getCustomer().getUser().getFullName(),
                    r.getRating(),
                    r.getComment(),
                    r.getCreatedAt() != null ? r.getCreatedAt().toString() : "",
                    items,
                    r.getImageUrl(),
                    r.getRestaurantReply(),
                    r.getRepliedAt() != null ? r.getRepliedAt().toString() : ""
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(reviewDTOs);
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
            FoodOrder order = orderService.createOrder(customer, restaurant, request.items, request.deliveryAddress, request.voucherCode, request.paymentMethod);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Đặt hàng thành công!",
                    "orderId", order.getId(),
                    "totalAmount", order.getTotalAmount()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Kiểm tra Voucher
    @GetMapping("/vouchers/check")
    public ResponseEntity<?> checkVoucher(@RequestParam String code, 
                                          @RequestParam(required = false) Long restaurantId) {
        Voucher voucher = voucherRepository.findByCode(code).orElse(null);
        if (voucher == null || !voucher.isActive() || 
           (voucher.getExpirationDate() != null && voucher.getExpirationDate().isBefore(java.time.LocalDate.now()))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã giảm giá không hợp lệ hoặc đã hết hạn"));
        }
        
        // Validate ownership: must be global or belong to the restaurant
        if (voucher.getRestaurant() != null) {
            if (restaurantId == null || !voucher.getRestaurant().getId().equals(restaurantId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã giảm giá này không áp dụng cho nhà hàng này"));
            }
        }

        return ResponseEntity.ok(Map.of(
            "code", voucher.getCode(),
            "discountType", voucher.getDiscountType().name(),
            "discountValue", voucher.getDiscountValue()
        ));
    }

    @PostMapping("/orders/estimate-shipping")
    public ResponseEntity<?> estimateShipping(@RequestBody Map<String, Object> body) {
        Long restaurantId;
        try {
            restaurantId = Long.valueOf(body.get("restaurantId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid restaurantId"));
        }
        String address = (String) body.get("deliveryAddress");
        
        RestaurantProfile rest = restaurantProfileRepository.findById(restaurantId).orElse(null);
        if (rest == null) return ResponseEntity.badRequest().build();
        
        double fee = 15000.0; // default base fee
        if (rest.getLatitude() != null && rest.getLongitude() != null && address != null && !address.isBlank()) {
            Map<String, Double> coords = geocodingService.getCoordinates(address);
            if (coords != null) {
                double dist = shippingCalculationService.calculateDistance(
                    rest.getLatitude(), rest.getLongitude(), coords.get("lat"), coords.get("lng")
                );
                fee = shippingCalculationService.calculateShippingFee(dist);
            }
        }
        return ResponseEntity.ok(Map.of("shippingFee", fee));
    }

    // UC-10: Lịch sử & theo dõi đơn hàng
    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<FoodOrder> orders = orderService.getCustomerOrders(customer).stream()
                .filter(o -> o.getStatus() != OrderStatus.PENDING_PAYMENT)
                .collect(Collectors.toList());
        List<OrderSummaryDTO> dtos = orders.stream().map(o -> {
            List<OrderItemDTO> items = o.getOrderItems() == null ? List.of() :
                    o.getOrderItems().stream().map(oi -> new OrderItemDTO(
                            oi.getMenuItem().getName(), oi.getQuantity(), oi.getPriceAtTimeOfOrder()
                    )).collect(Collectors.toList());
            
            // Tìm đánh giá cho đơn hàng này
            Optional<Review> reviewOpt = reviewRepository.findByOrder(o);
            boolean isReviewed = reviewOpt.isPresent();
            Integer reviewRating = isReviewed ? reviewOpt.get().getRating() : null;
            String reviewComment = isReviewed ? reviewOpt.get().getComment() : null;

            return new OrderSummaryDTO(
                    o.getId(),
                    o.getRestaurant().getRestaurantName(),
                    o.getRestaurant().getBannerUrl(),
                    o.getStatus().name(),
                    o.getTotalAmount(),
                    o.getOrderTime() != null ? o.getOrderTime().toString() : "",
                    o.getDeliveryAddress(),
                    items,
                    isReviewed,
                    reviewRating,
                    reviewComment,
                    o.getPaymentMethod()
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

        // Tìm đánh giá cho đơn hàng này
        Optional<Review> reviewOpt = reviewRepository.findByOrder(order);
        boolean isReviewed = reviewOpt.isPresent();
        Integer reviewRating = isReviewed ? reviewOpt.get().getRating() : null;
        String reviewComment = isReviewed ? reviewOpt.get().getComment() : null;

        OrderDetailDTO dto = new OrderDetailDTO(
                order.getId(),
                order.getRestaurant().getRestaurantName(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getOrderTime() != null ? order.getOrderTime().toString() : "",
                order.getDeliveryAddress(),
                driverName,
                driverPhone,
                items,
                isReviewed,
                reviewRating,
                reviewComment,
                order.getPaymentMethod()
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
            orderService.reviewOrder(id, customer, request.rating, request.comment, request.imageUrl);
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
        public Integer reviewCount;

        public RestaurantDTO(Long id, String name, String address, Double rating, boolean isOpen, String imageUrl, Integer reviewCount) {
            this.id = id; this.name = name; this.address = address;
            this.rating = rating; this.isOpen = isOpen; this.imageUrl = imageUrl;
            this.reviewCount = reviewCount != null ? reviewCount : 0;
        }
    }

    public static class MenuItemDTO {
        public Long id;
        public String name;
        public String description;
        public Double price;
        public String imageUrl;
        public String videoUrl;
        public boolean isAvailable;

        public MenuItemDTO(Long id, String name, String description, Double price, String imageUrl, String videoUrl, boolean isAvailable) {
            this.id = id; this.name = name; this.description = description;
            this.price = price; this.imageUrl = imageUrl; this.videoUrl = videoUrl; this.isAvailable = isAvailable;
        }
    }

    public static class RestaurantDetailDTO {
        public Long id;
        public String name;
        public String address;
        public Double rating;
        public boolean isOpen;
        public String bannerUrl;
        public Integer reviewCount;
        public List<MenuItemDTO> menuItems;

        public RestaurantDetailDTO(Long id, String name, String address, Double rating,
                                   boolean isOpen, String bannerUrl, Integer reviewCount, List<MenuItemDTO> menuItems) {
            this.id = id; this.name = name; this.address = address; this.rating = rating;
            this.isOpen = isOpen; this.bannerUrl = bannerUrl; this.reviewCount = reviewCount != null ? reviewCount : 0; this.menuItems = menuItems;
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
        public boolean isReviewed;
        public Integer reviewRating;
        public String reviewComment;
        public String paymentMethod;

        public OrderSummaryDTO(Long id, String restaurantName, String restaurantImage, String status,
                               Double totalAmount, String orderTime, String deliveryAddress, List<OrderItemDTO> items,
                               boolean isReviewed, Integer reviewRating, String reviewComment, String paymentMethod) {
            this.id = id; this.restaurantName = restaurantName; this.restaurantImage = restaurantImage;
            this.status = status; this.totalAmount = totalAmount; this.orderTime = orderTime;
            this.deliveryAddress = deliveryAddress; this.items = items;
            this.isReviewed = isReviewed;
            this.reviewRating = reviewRating;
            this.reviewComment = reviewComment;
            this.paymentMethod = paymentMethod;
        }
    }

    public static class OrderDetailDTO extends OrderSummaryDTO {
        public String driverName;
        public String driverPhone;

        public OrderDetailDTO(Long id, String restaurantName, String status, Double totalAmount,
                              String orderTime, String deliveryAddress, String driverName,
                              String driverPhone, List<OrderItemDTO> items, boolean isReviewed, 
                              Integer reviewRating, String reviewComment, String paymentMethod) {
            super(id, restaurantName, null, status, totalAmount, orderTime, deliveryAddress, items, isReviewed, reviewRating, reviewComment, paymentMethod);
            this.driverName = driverName;
            this.driverPhone = driverPhone;
        }
    }

    public static class PlaceOrderRequest {
        public Long restaurantId;
        public List<OrderService.OrderItemRequest> items;
        public String deliveryAddress;
        public String paymentMethod;
        public String voucherCode;
    }

    public static class ReviewRequest {
        public int rating;
        public String comment;
        public String imageUrl;
    }

    public static class ReviewDTO {
        public String customerName;
        public int rating;
        public String comment;
        public String createdAt;
        public List<String> orderItems;
        public String imageUrl;
        public String restaurantReply;
        public String repliedAt;

        public ReviewDTO(String customerName, int rating, String comment, String createdAt, List<String> orderItems,
                         String imageUrl, String restaurantReply, String repliedAt) {
            this.customerName = customerName;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = createdAt;
            this.orderItems = orderItems;
            this.imageUrl = imageUrl;
            this.restaurantReply = restaurantReply;
            this.repliedAt = repliedAt;
        }
    }
}
