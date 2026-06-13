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
import java.util.HashMap;

import com.duong.salesmanagement.model.CustomerProfile;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.MenuItem;
import com.duong.salesmanagement.model.OrderStatus;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.Review;
import com.duong.salesmanagement.model.Role;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.model.Voucher;
import com.duong.salesmanagement.model.FoodReview;
import com.duong.salesmanagement.model.Category;
import com.duong.salesmanagement.dto.FoodReviewDTO;
import com.duong.salesmanagement.repository.CustomerProfileRepository;
import com.duong.salesmanagement.repository.MenuItemRepository;
import com.duong.salesmanagement.repository.PaymentRepository;
import com.duong.salesmanagement.repository.RestaurantProfileRepository;
import com.duong.salesmanagement.repository.ReviewRepository;
import com.duong.salesmanagement.repository.UserRepository;
import com.duong.salesmanagement.repository.VoucherRepository;
import com.duong.salesmanagement.repository.FoodReviewRepository;
import com.duong.salesmanagement.repository.CategoryRepository;

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
    private final PaymentRepository paymentRepository;
    private final FoodReviewRepository foodReviewRepository;
    private final CategoryRepository categoryRepository;
    private final com.duong.salesmanagement.service.NotificationService notificationService;

    public CustomerApiController(RestaurantProfileRepository restaurantProfileRepository,
                                 MenuItemRepository menuItemRepository,
                                 IOrderService orderService,
                                 UserRepository userRepository,
                                 CustomerProfileRepository customerProfileRepository,
                                 VoucherRepository voucherRepository,
                                 ReviewRepository reviewRepository,
                                 IShippingCalculationService shippingCalculationService,
                                 com.duong.salesmanagement.service.GeocodingService geocodingService,
                                 PaymentRepository paymentRepository,
                                 FoodReviewRepository foodReviewRepository,
                                 CategoryRepository categoryRepository,
                                 com.duong.salesmanagement.service.NotificationService notificationService) {
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.voucherRepository = voucherRepository;
        this.reviewRepository = reviewRepository;
        this.shippingCalculationService = shippingCalculationService;
        this.geocodingService = geocodingService;
        this.paymentRepository = paymentRepository;
        this.foodReviewRepository = foodReviewRepository;
        this.categoryRepository = categoryRepository;
        this.notificationService = notificationService;
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
                m.getId(), m.getName(), m.getDescription(), m.getPrice(), m.getImageUrl(), m.getVideoUrl(), m.isAvailable(),
                m.getAverageRating(), m.getReviewCount(), m.getRestaurant() != null ? m.getRestaurant().getId() : null, m.getRestaurant() != null ? m.getRestaurant().getRestaurantName() : null,
                m.getCategory() != null ? m.getCategory().getId() : null, m.getCategory() != null ? m.getCategory().getName() : null
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

    // UC: Tìm kiếm món ăn và sắp xếp theo đánh giá
    @GetMapping("/menu-items/search")
    public ResponseEntity<?> searchMenuItems(@RequestParam String keyword) {
        List<MenuItem> menuItems = menuItemRepository.findByNameContainingIgnoreCaseAndIsAvailableTrueOrderByAverageRatingDesc(keyword);
        List<MenuItemDTO> menuItemDTOs = menuItems.stream().map(m -> new MenuItemDTO(
                m.getId(), m.getName(), m.getDescription(), m.getPrice(), m.getImageUrl(), m.getVideoUrl(), m.isAvailable(),
                m.getAverageRating(), m.getReviewCount(), m.getRestaurant() != null ? m.getRestaurant().getId() : null, m.getRestaurant() != null ? m.getRestaurant().getRestaurantName() : null,
                m.getCategory() != null ? m.getCategory().getId() : null, m.getCategory() != null ? m.getCategory().getName() : null
        )).collect(Collectors.toList());
        return ResponseEntity.ok(menuItemDTOs);
    }

    // UC: Lấy tất cả danh mục món ăn
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    // UC: Lấy món ăn theo danh mục — có fallback thông minh
    @GetMapping("/menu-items/category/{categoryId}")
    public ResponseEntity<?> getMenuItemsByCategory(@PathVariable Long categoryId) {
        com.duong.salesmanagement.model.Category category =
            categoryRepository.findById(categoryId).orElse(null);
        if (category == null) return ResponseEntity.notFound().build();

        // Buớc 1: Lấy món được gán trực tiếp vào category này
        List<MenuItem> menuItems = menuItemRepository.findByCategory_IdAndIsAvailableTrue(categoryId);
        boolean isFallback = menuItems.isEmpty();

        // Buớc 2: Nếu không có kết quả, fallback tìm theo tên category trong tên/mô tả món
        if (isFallback) {
            menuItems = menuItemRepository.findByNameOrDescriptionContainingIgnoreCaseAndIsAvailableTrue(
                category.getName()
            );
        }

        // Buớc 3: Map sang DTO, sắp xếp theo rating giảm dần
        List<MenuItemDTO> menuItemDTOs = menuItems.stream()
                .filter(m -> m.getRestaurant() != null && m.getRestaurant().getUser() != null
                        && m.getRestaurant().getUser().isEnabled())
                .sorted((a, b) -> Double.compare(
                    b.getAverageRating() != null ? b.getAverageRating() : 0,
                    a.getAverageRating() != null ? a.getAverageRating() : 0))
                .map(m -> new MenuItemDTO(
                    m.getId(), m.getName(), m.getDescription(), m.getPrice(),
                    m.getImageUrl(), m.getVideoUrl(), m.isAvailable(),
                    m.getAverageRating(), m.getReviewCount(),
                    m.getRestaurant() != null ? m.getRestaurant().getId() : null,
                    m.getRestaurant() != null ? m.getRestaurant().getRestaurantName() : null,
                    m.getCategory() != null ? m.getCategory().getId() : null,
                    m.getCategory() != null ? m.getCategory().getName() : null
                )).collect(Collectors.toList());

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("items", menuItemDTOs);
        result.put("categoryName", category.getName());
        result.put("isFallback", isFallback);
        result.put("total", menuItemDTOs.size());
        return ResponseEntity.ok(result);
    }

    // UC: Xem danh sách đánh giá của món ăn
    @GetMapping("/menu-items/{id}/reviews")
    public ResponseEntity<?> getMenuItemReviews(@PathVariable Long id) {
        MenuItem menuItem = menuItemRepository.findById(id).orElse(null);
        if (menuItem == null) return ResponseEntity.notFound().build();

        List<FoodReview> reviews = foodReviewRepository.findByMenuItemOrderByCreatedAtDesc(menuItem);
        List<FoodReviewDTO> reviewDTOs = reviews.stream().map(r -> new FoodReviewDTO(
                r.getId(),
                r.getMenuItem().getId(),
                r.getCustomer().getFullName() != null ? r.getCustomer().getFullName() : r.getCustomer().getUsername(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt().toString(),
                r.getRatingLevel()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(reviewDTOs);
    }

    public static class FoodReviewRequest {
        public Integer rating;
        public String comment;
    }

    // UC: Đánh giá món ăn
    @PostMapping("/menu-items/{id}/reviews")
    public ResponseEntity<?> reviewMenuItem(Authentication authentication, @PathVariable Long id, @RequestBody FoodReviewRequest request) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        MenuItem menuItem = menuItemRepository.findById(id).orElse(null);
        if (menuItem == null) return ResponseEntity.notFound().build();

        if (request.rating == null || request.rating < 1 || request.rating > 5) {
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
        }

        // Tạo review mới
        FoodReview review = new FoodReview();
        review.setMenuItem(menuItem);
        review.setCustomer(customer.getUser());
        review.setRating(request.rating);
        review.setComment(request.comment);
        review.setRatingLevel(FoodReview.getRatingLevelDescription(request.rating));
        foodReviewRepository.save(review);

        // Tính toán lại averageRating cho MenuItem từ dữ liệu thực tế trong DB
        Double avgRating = foodReviewRepository.getAverageRatingForMenuItem(menuItem.getId());
        Long count = foodReviewRepository.countByMenuItemId(menuItem.getId());
        
        menuItem.setReviewCount(count != null ? count.intValue() : 0);
        menuItem.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        menuItemRepository.save(menuItem);

        // Đồng bộ lại averageRating cho RestaurantProfile từ toàn bộ FoodReview
        if (menuItem.getRestaurant() != null) {
            RestaurantProfile restaurant = menuItem.getRestaurant();
            Double restAvgRating = foodReviewRepository.getAverageRatingByRestaurantId(restaurant.getId());
            Long restCount = foodReviewRepository.countByRestaurantId(restaurant.getId());
            restaurant.setAverageRating(restAvgRating != null ? Math.round(restAvgRating * 10.0) / 10.0 : 0.0);
            restaurant.setReviewCount(restCount != null ? restCount.intValue() : 0);
            restaurantProfileRepository.save(restaurant);
            // ③ Gửi thông báo đến nhà hàng khi có đánh giá món ăn mới
            try {
                if (restaurant.getUser() != null) {
                    notificationService.notifyNewReview(restaurant.getUser(), null, request.rating);
                }
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok(Map.of("message", "Đánh giá món ăn thành công!"));
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
            FoodOrder order = orderService.createOrder(customer, restaurant, request.items, request.deliveryAddress, request.deliveryLat, request.deliveryLng, request.voucherCode, request.paymentMethod);
            boolean requiresPayment = order.getStatus() == com.duong.salesmanagement.model.OrderStatus.AWAITING_PAYMENT;
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", requiresPayment
                            ? "Vui lòng hoàn tất thanh toán để xác nhận đơn hàng"
                            : "Đặt hàng thành công!",
                    "orderId", order.getId(),
                    "totalAmount", order.getTotalAmount(),
                    "requiresPayment", requiresPayment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Lấy danh sách Voucher có thể áp dụng
    @GetMapping("/vouchers/available")
    public ResponseEntity<?> getAvailableVouchers(@RequestParam(required = false) Long restaurantId) {
        List<Voucher> vouchers;
        java.time.LocalDate today = java.time.LocalDate.now();
        if (restaurantId != null) {
            vouchers = voucherRepository.findAvailableVouchers(restaurantId, today);
        } else {
            vouchers = voucherRepository.findGlobalAvailableVouchers(today);
        }
        
        List<Map<String, Object>> result = vouchers.stream().map(v -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("code", v.getCode());
            map.put("discountType", v.getDiscountType() != null ? v.getDiscountType().name() : "PERCENTAGE");
            map.put("discountValue", v.getDiscountValue());
            map.put("minOrderAmount", v.getMinOrderAmount());
            map.put("maxDiscount", v.getMaxDiscount());
            map.put("description", v.getDescription());
            map.put("expirationDate", v.getExpirationDate() != null ? v.getExpirationDate().toString() : "");
            map.put("isGlobal", v.getRestaurant() == null);
            map.put("restaurantId", v.getRestaurant() != null ? v.getRestaurant().getId() : null);
            map.put("restaurantName", v.getRestaurant() != null ? v.getRestaurant().getRestaurantName() : "Hệ thống");
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    // Kiểm tra Voucher
    @GetMapping("/vouchers/check")
    public ResponseEntity<?> checkVoucher(@RequestParam String code, 
                                          @RequestParam(required = false) Long restaurantId) {
        Voucher voucher = null;
        java.time.LocalDate today = java.time.LocalDate.now();
        
        if (restaurantId != null) {
            java.util.List<Voucher> restVouchers = voucherRepository.findByCodeAndRestaurantId(code, restaurantId);
            voucher = restVouchers.stream()
                .filter(v -> v.isActive() && 
                             (v.getStartDate() == null || !v.getStartDate().isAfter(today)) &&
                             (v.getExpirationDate() == null || !v.getExpirationDate().isBefore(today)))
                .findFirst().orElse(null);
        }
        if (voucher == null) {
            java.util.List<Voucher> globalVouchers = voucherRepository.findByCodeAndRestaurantIsNull(code);
            voucher = globalVouchers.stream()
                .filter(v -> v.isActive() && 
                             (v.getStartDate() == null || !v.getStartDate().isAfter(today)) &&
                             (v.getExpirationDate() == null || !v.getExpirationDate().isBefore(today)))
                .findFirst().orElse(null);
        }
        
        if (voucher == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã giảm giá không hợp lệ, chưa đến ngày hoặc đã hết hạn"));
        }
        
        // Validate ownership: must be global or belong to the restaurant
        if (voucher.getRestaurant() != null) {
            if (restaurantId == null || !voucher.getRestaurant().getId().equals(restaurantId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã giảm giá này không áp dụng cho nhà hàng này"));
            }
        }

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("code", voucher.getCode());
        response.put("discountType", voucher.getDiscountType() != null ? voucher.getDiscountType().name() : "PERCENTAGE");
        response.put("discountValue", voucher.getDiscountValue());
        response.put("minOrderAmount", voucher.getMinOrderAmount());
        response.put("maxDiscount", voucher.getMaxDiscount());
        response.put("description", voucher.getDescription());
        return ResponseEntity.ok(response);
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
        Double lat = null, lng = null;
        try {
            if (body.get("lat") != null) lat = Double.valueOf(body.get("lat").toString());
            if (body.get("lng") != null) lng = Double.valueOf(body.get("lng").toString());
        } catch (Exception e) {}

        if (rest.getLatitude() != null && rest.getLongitude() != null) {
            if (lat != null && lng != null) {
                double dist = shippingCalculationService.calculateDistance(
                    rest.getLatitude(), rest.getLongitude(), lat, lng
                );
                fee = shippingCalculationService.calculateShippingFee(dist);
            } else if (address != null && !address.isBlank()) {
                Map<String, Double> coords = geocodingService.getCoordinates(address);
                if (coords != null) {
                    double dist = shippingCalculationService.calculateDistance(
                        rest.getLatitude(), rest.getLongitude(), coords.get("lat"), coords.get("lng")
                    );
                    fee = shippingCalculationService.calculateShippingFee(dist);
                }
            }
        }
        return ResponseEntity.ok(Map.of("shippingFee", fee));
    }

    /** Hủy đơn online khi chưa thanh toán (VD: không tạo được URL VNPAY) */
    @PostMapping("/orders/{id}/cancel-unpaid")
    public ResponseEntity<?> cancelUnpaidOrder(Authentication authentication, @PathVariable Long id) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            orderService.cancelUnpaidOnlineOrder(id, customer);
            return ResponseEntity.ok(Map.of("message", "Đã hủy đơn chờ thanh toán"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // UC-10: Lịch sử & theo dõi đơn hàng
    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<FoodOrder> orders = orderService.getCustomerOrders(customer).stream()
                .filter(o -> o.getStatus() != OrderStatus.AWAITING_PAYMENT)
                .collect(Collectors.toList());

        // Optimize: Fetch all reviews for these orders in one query
        Map<FoodOrder, Review> reviewMap = new HashMap<>();
        if (!orders.isEmpty()) {
            List<Review> reviews = reviewRepository.findByOrderIn(orders);
            for (Review r : reviews) {
                reviewMap.put(r.getOrder(), r);
            }
        }

        List<OrderSummaryDTO> dtos = orders.stream().map(o -> {
            List<OrderItemDTO> items = o.getOrderItems() == null ? List.of() :
                    o.getOrderItems().stream().map(oi -> new OrderItemDTO(
                            oi.getMenuItem().getId(), oi.getMenuItem().getName(), oi.getQuantity(), oi.getPriceAtTimeOfOrder()
                    )).collect(Collectors.toList());
            
            // Tìm đánh giá cho đơn hàng này từ Map (O(1))
            Review review = reviewMap.get(o);
            boolean isReviewed = review != null;
            Integer reviewRating = isReviewed ? review.getRating() : null;
            String reviewComment = isReviewed ? review.getComment() : null;

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
        if (order == null || !order.getCustomer().getId().equals(customer.getId())
                || !orderService.isVisibleInCustomerOrderHistory(order))
            return ResponseEntity.notFound().build();

        List<OrderItemDTO> items = order.getOrderItems() == null ? List.of() :
                order.getOrderItems().stream().map(oi -> new OrderItemDTO(
                        oi.getMenuItem().getId(), oi.getMenuItem().getName(), oi.getQuantity(), oi.getPriceAtTimeOfOrder()
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
            // ③ Gửi thông báo đến nhà hàng khi có review mới
            try {
                com.duong.salesmanagement.model.FoodOrder order = orderService.getOrderById(id).orElse(null);
                if (order != null && order.getRestaurant() != null && order.getRestaurant().getUser() != null) {
                    notificationService.notifyNewReview(order.getRestaurant().getUser(), id, request.rating);
                }
            } catch (Exception ignored) {}
            return ResponseEntity.ok(Map.of("message", "Cảm ơn bạn đã đánh giá!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ① Vote "Hữu ích" cho review
    @PostMapping("/reviews/{id}/helpful")
    public ResponseEntity<?> voteHelpful(Authentication authentication, @PathVariable Long id) {
        if (authentication == null || !authentication.isAuthenticated())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) return ResponseEntity.notFound().build();
        review.setHelpfulCount((review.getHelpfulCount() == null ? 0 : review.getHelpfulCount()) + 1);
        reviewRepository.save(review);
        return ResponseEntity.ok(Map.of("helpfulCount", review.getHelpfulCount()));
    }

    // ④ Lịch sử đánh giá của khách
    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Review> reviews = reviewRepository.findByOrder_CustomerOrderByCreatedAtDesc(customer);
        List<Map<String, Object>> result = reviews.stream().map(r -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("orderId", r.getOrder().getId());
            map.put("restaurantName", r.getOrder().getRestaurant().getRestaurantName());
            map.put("restaurantImage", r.getOrder().getRestaurant().getBannerUrl());
            map.put("rating", r.getRating());
            map.put("comment", r.getComment());
            map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
            map.put("restaurantReply", r.getRestaurantReply());
            map.put("helpfulCount", r.getHelpfulCount() != null ? r.getHelpfulCount() : 0);
            map.put("imageUrl", r.getImageUrl());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Lịch sử giao dịch
    @GetMapping("/payments")
    public ResponseEntity<?> getPaymentHistory(Authentication authentication) {
        CustomerProfile customer = getAuthenticatedCustomer(authentication);
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<com.duong.salesmanagement.model.Payment> payments = paymentRepository.findByOrder_Customer(customer);
        List<PaymentHistoryDTO> dtos = payments.stream().map(p -> new PaymentHistoryDTO(
                p.getId(),
                p.getOrder().getId(),
                p.getOrder().getRestaurant().getRestaurantName(),
                p.getOrder().getRestaurant().getBannerUrl(),
                p.getPaymentMethod().name(),
                p.getPaymentStatus().name(),
                p.getAmount(),
                p.getTransactionDate() != null ? p.getTransactionDate().toString() : "",
                p.getOrder().getOrderTime() != null ? p.getOrder().getOrderTime().toString() : ""
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
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
        public Double averageRating;
        public Integer reviewCount;
        public Long restaurantId;
        public String restaurantName;
        public Long categoryId;
        public String categoryName;

        public MenuItemDTO(Long id, String name, String description, Double price, String imageUrl, String videoUrl, boolean isAvailable, Double averageRating, Integer reviewCount, Long restaurantId, String restaurantName, Long categoryId, String categoryName) {
            this.id = id; this.name = name; this.description = description;
            this.price = price; this.imageUrl = imageUrl; this.videoUrl = videoUrl; this.isAvailable = isAvailable;
            this.averageRating = averageRating != null ? averageRating : 0.0;
            this.reviewCount = reviewCount != null ? reviewCount : 0;
            this.restaurantId = restaurantId;
            this.restaurantName = restaurantName;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
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
        public Long menuItemId;
        public String itemName;
        public int quantity;
        public Double price;

        public OrderItemDTO(Long menuItemId, String itemName, int quantity, Double price) {
            this.menuItemId = menuItemId; this.itemName = itemName; this.quantity = quantity; this.price = price;
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
        public Double deliveryLat;
        public Double deliveryLng;
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

    public static class PaymentHistoryDTO {
        public Long id;
        public Long orderId;
        public String restaurantName;
        public String restaurantImage;
        public String paymentMethod;
        public String paymentStatus;
        public Double amount;
        public String transactionDate;
        public String orderTime;

        public PaymentHistoryDTO(Long id, Long orderId, String restaurantName, String restaurantImage,
                                  String paymentMethod, String paymentStatus, Double amount,
                                  String transactionDate, String orderTime) {
            this.id = id;
            this.orderId = orderId;
            this.restaurantName = restaurantName;
            this.restaurantImage = restaurantImage;
            this.paymentMethod = paymentMethod;
            this.paymentStatus = paymentStatus;
            this.amount = amount;
            this.transactionDate = transactionDate;
            this.orderTime = orderTime;
        }
    }
}
