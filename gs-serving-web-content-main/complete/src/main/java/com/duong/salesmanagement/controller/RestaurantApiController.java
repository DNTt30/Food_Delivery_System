package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import com.duong.salesmanagement.service.IOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API cho Restaurant Management module.
 * Base path: /api/restaurant
 * Security: JWT Bearer token, role RESTAURANT only (enforced by Spring Security).
 */
@RestController
@RequestMapping("/api/restaurant")

public class RestaurantApiController {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final MenuItemRepository menuItemRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final VoucherRepository voucherRepository;
    private final ReviewRepository reviewRepository;
    private final IOrderService orderService;
    private final com.duong.salesmanagement.repository.OrderTrackingLocationRepository trackingLocationRepository;

    public RestaurantApiController(UserRepository userRepository,
                                   RestaurantProfileRepository restaurantProfileRepository,
                                   MenuItemRepository menuItemRepository,
                                   FoodOrderRepository foodOrderRepository,
                                   VoucherRepository voucherRepository,
                                   ReviewRepository reviewRepository,
                                   IOrderService orderService,
                                   com.duong.salesmanagement.repository.OrderTrackingLocationRepository trackingLocationRepository) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.menuItemRepository = menuItemRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.voucherRepository = voucherRepository;
        this.reviewRepository = reviewRepository;
        this.orderService = orderService;
        this.trackingLocationRepository = trackingLocationRepository;
    }

    // ================================================================
    // AUTH HELPER
    // ================================================================

    private RestaurantProfile getAuthenticatedRestaurant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != Role.RESTAURANT) return null;
        return restaurantProfileRepository.findByUser(user).orElseGet(() -> {
            RestaurantProfile p = new RestaurantProfile();
            p.setUser(user);
            return restaurantProfileRepository.save(p);
        });
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Chưa đăng nhập hoặc không có quyền"));
    }

    // ================================================================
    // 1. DASHBOARD
    // ================================================================

    /**
     * GET /api/restaurant/dashboard
     * Trả về thống kê hôm nay: đơn mới, doanh thu hôm nay, số món, rating trung bình.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd   = LocalDate.now().atTime(LocalTime.MAX);

        List<FoodOrder> todayOrders = foodOrderRepository
                .findByRestaurantAndOrderTimeBetweenOrderByOrderTimeDesc(restaurant, todayStart, todayEnd);

        long newOrders       = todayOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long preparingOrders = todayOrders.stream().filter(o -> o.getStatus() == OrderStatus.PREPARING).count();
        long completedToday  = todayOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        double revenueToday  = todayOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(o -> {
                    double total = o.getTotalAmount() != null ? o.getTotalAmount() : 0;
                    double shipping = o.getShippingFee() != null ? o.getShippingFee() : 0;
                    return Math.max(0, total - shipping);
                })
                .sum();

        long menuCount = menuItemRepository.findByRestaurant(restaurant).stream()
                .filter(MenuItem::isAvailable).count();

        Double avgRating = reviewRepository.avgRatingByRestaurant(restaurant);

        // 7-day revenue for chart
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime from = day.atStartOfDay();
            LocalDateTime to   = day.atTime(LocalTime.MAX);
            List<FoodOrder> dayOrders = foodOrderRepository
                    .findByRestaurantAndOrderTimeBetweenOrderByOrderTimeDesc(restaurant, from, to);
            double dayRevenue = dayOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .mapToDouble(o -> {
                        double total = o.getTotalAmount() != null ? o.getTotalAmount() : 0;
                        double shipping = o.getShippingFee() != null ? o.getShippingFee() : 0;
                        return Math.max(0, total - shipping);
                    })
                    .sum();
            long dayCount = dayOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", day.toString());
            point.put("revenue", dayRevenue);
            point.put("orders", dayCount);
            chartData.add(point);
        }

        return ResponseEntity.ok(Map.of(
                "todayNewOrders",      newOrders,
                "todayPreparingOrders",preparingOrders,
                "todayCompleted",      completedToday,
                "todayRevenue",        revenueToday,
                "menuCount",           menuCount,
                "avgRating",           avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
                "isOpen",              restaurant.isOpen(),
                "chartData",           chartData
        ));
    }

    /** PUT /api/restaurant/toggle-status — Mở/đóng cửa nhà hàng */
    @PutMapping("/toggle-status")
    public ResponseEntity<?> toggleStatus(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        restaurant.setOpen(!restaurant.isOpen());
        restaurantProfileRepository.save(restaurant);
        return ResponseEntity.ok(Map.of(
                "isOpen",   restaurant.isOpen(),
                "message",  restaurant.isOpen() ? "Nhà hàng đang mở cửa, sẵn sàng nhận đơn!" : "Nhà hàng đã đóng cửa."
        ));
    }

    // ================================================================
    // 2. MENU MANAGEMENT
    // ================================================================

    /** GET /api/restaurant/menu — Lấy toàn bộ menu */
    @GetMapping("/menu")
    public ResponseEntity<?> getMenu(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        List<MenuItemDTO> dtos = menuItemRepository.findByRestaurant(restaurant).stream()
                .map(m -> new MenuItemDTO(m.getId(), m.getName(), m.getDescription(),
                        m.getPrice(), m.getImageUrl(), m.isAvailable(),
                        m.getCategory() != null ? m.getCategory().getName() : null,
                        m.getCategory() != null ? m.getCategory().getId() : null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /** POST /api/restaurant/menu — Thêm món mới */
    @PostMapping("/menu")
    public ResponseEntity<?> createMenuItem(Authentication auth, @RequestBody MenuItemDTO dto) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        applyDto(item, dto);
        menuItemRepository.save(item);
        return ResponseEntity.ok(Map.of("message", "Đã thêm món ăn thành công"));
    }

    /** PUT /api/restaurant/menu/{id} — Sửa món */
    @PutMapping("/menu/{id}")
    public ResponseEntity<?> updateMenuItem(Authentication auth, @PathVariable Long id,
                                            @RequestBody MenuItemDTO dto) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        MenuItem item = menuItemRepository.findById(id).orElse(null);
        if (item == null || !item.getRestaurant().getId().equals(restaurant.getId()))
            return ResponseEntity.notFound().build();

        applyDto(item, dto);
        menuItemRepository.save(item);
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật món ăn"));
    }

    /** PATCH /api/restaurant/menu/{id}/toggle — Ẩn/hiện món */
    @PatchMapping("/menu/{id}/toggle")
    public ResponseEntity<?> toggleMenuItem(Authentication auth, @PathVariable Long id) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        MenuItem item = menuItemRepository.findById(id).orElse(null);
        if (item == null || !item.getRestaurant().getId().equals(restaurant.getId()))
            return ResponseEntity.notFound().build();

        item.setAvailable(!item.isAvailable());
        menuItemRepository.save(item);
        return ResponseEntity.ok(Map.of(
                "available", item.isAvailable(),
                "message", item.isAvailable() ? "Đã hiện món" : "Đã ẩn món"
        ));
    }

    /** DELETE /api/restaurant/menu/{id} — Xóa món (hard delete; dùng toggle để soft hide) */
    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(Authentication auth, @PathVariable Long id) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        MenuItem item = menuItemRepository.findById(id).orElse(null);
        if (item == null || !item.getRestaurant().getId().equals(restaurant.getId()))
            return ResponseEntity.notFound().build();

        menuItemRepository.delete(item);
        return ResponseEntity.ok(Map.of("message", "Đã xóa món ăn"));
    }

    private void applyDto(MenuItem item, MenuItemDTO dto) {
        if (dto.name != null)        item.setName(dto.name);
        if (dto.description != null) item.setDescription(dto.description);
        if (dto.price != null)       item.setPrice(dto.price);
        if (dto.imageUrl != null)    item.setImageUrl(dto.imageUrl);
        item.setAvailable(dto.isAvailable);
    }

    // ================================================================
    // 3. ORDER MANAGEMENT
    // ================================================================

    /**
     * GET /api/restaurant/orders?status=ALL|PENDING|PREPARING|DELIVERING|COMPLETED|CANCELLED
     * Trả về đơn hàng đã lọc theo trạng thái, kèm thông tin chi tiết.
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(Authentication auth,
                                       @RequestParam(defaultValue = "ACTIVE") String status) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        List<FoodOrder> orders;
        if ("ACTIVE".equalsIgnoreCase(status)) {
            // Chỉ PENDING + PREPARING
            orders = foodOrderRepository.findByRestaurantOrderByOrderTimeDesc(restaurant).stream()
                    .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.PREPARING)
                    .collect(Collectors.toList());
        } else if ("ALL".equalsIgnoreCase(status)) {
            orders = foodOrderRepository.findByRestaurantOrderByOrderTimeDesc(restaurant);
        } else {
            try {
                OrderStatus s = OrderStatus.valueOf(status.toUpperCase());
                orders = foodOrderRepository.findByRestaurantAndStatus(restaurant, s);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái không hợp lệ: " + status));
            }
        }

        List<OrderDTO> dtos = orders.stream().map(this::toOrderDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** GET /api/restaurant/orders/{id} — Chi tiết đơn hàng */
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetail(Authentication auth, @PathVariable Long id) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        FoodOrder order = foodOrderRepository.findById(id).orElse(null);
        if (order == null || !order.getRestaurant().getId().equals(restaurant.getId()))
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(toOrderDTO(order));
    }

    /**
     * GET /api/restaurant/orders/history?page=0&size=10
     * Trả về lịch sử đơn hàng có phân trang.
     */
    @GetMapping("/orders/history")
    public ResponseEntity<?> getOrderHistory(Authentication auth,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderTime").descending());
        Page<FoodOrder> orderPage = foodOrderRepository.findByRestaurant(restaurant, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderPage.getContent().stream().map(this::toOrderDTO).collect(Collectors.toList()));
        response.put("currentPage", orderPage.getNumber());
        response.put("totalItems", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /** PUT /api/restaurant/orders/{id}/status — Cập nhật trạng thái đơn */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(Authentication auth, @PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        try {
            OrderStatus newStatus = OrderStatus.valueOf(body.get("status"));
            orderService.updateOrderStatus(id, newStatus, restaurant);
            return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái đơn hàng"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái không hợp lệ"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private OrderDTO toOrderDTO(FoodOrder o) {
        List<OrderItemDTO> items = Optional.ofNullable(o.getOrderItems())
                .orElse(Collections.emptyList()).stream()
                .map(oi -> new OrderItemDTO(
                        oi.getMenuItem().getName(),
                        oi.getQuantity() != null ? oi.getQuantity() : 0,
                        oi.getPriceAtTimeOfOrder(),
                        oi.getMenuItem().getImageUrl()))
                .collect(Collectors.toList());

        String customerPhone = null;
        String customerName  = null;
        if (o.getCustomer() != null) {
            customerName  = o.getCustomer().getUser().getFullName();
            customerPhone = o.getCustomer().getPhoneNumber();
        }

        String driverName  = null;
        String driverPhone = null;
        if (o.getDriver() != null) {
            driverName  = o.getDriver().getUser().getFullName();
            driverPhone = o.getDriver().getPhoneNumber();
        }

        // Lấy trackingPhase mới nhất để hiển thị cho Restaurant
        String trackingPhase = null;
        if (o.getStatus() == com.duong.salesmanagement.model.OrderStatus.DELIVERING
                || o.getStatus() == com.duong.salesmanagement.model.OrderStatus.COMPLETED) {
            var latest = trackingLocationRepository.findFirstByOrderIdOrderByTimestampDesc(o.getId());
            if (latest != null && latest.getTrackingPhase() != null) {
                trackingPhase = latest.getTrackingPhase().name();
            }
        }

        return new OrderDTO(
                o.getId(),
                customerName,
                customerPhone,
                items,
                o.getStatus().name(),
                o.getTotalAmount(),
                o.getShippingFee() != null ? o.getShippingFee() : 0.0,
                o.getDeliveryAddress(),
                o.getOrderTime() != null ? o.getOrderTime().toString() : null,
                driverName,
                driverPhone,
                trackingPhase
        );
    }

    // ================================================================
    // 4. VOUCHER MANAGEMENT
    // ================================================================

    /** GET /api/restaurant/vouchers — Danh sách voucher */
    @GetMapping("/vouchers")
    public ResponseEntity<?> getVouchers(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        List<Voucher> vouchers = voucherRepository.findByRestaurant(restaurant);
        List<VoucherDTO> dtos = vouchers.stream().map(v -> new VoucherDTO(
                v.getId(), v.getCode(), v.getDiscountValue(),
                v.getDiscountType() != null ? v.getDiscountType().name() : null,
                v.getExpirationDate() != null ? v.getExpirationDate().toString() : null,
                v.isActive()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** POST /api/restaurant/vouchers — Tạo voucher mới */
    @PostMapping("/vouchers")
    public ResponseEntity<?> createVoucher(Authentication auth, @RequestBody VoucherDTO dto) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        if (dto.code == null || dto.code.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Mã voucher không được để trống"));

        Voucher v = new Voucher();
        v.setCode(dto.code.trim().toUpperCase());
        v.setDiscountValue(dto.discountValue);
        v.setRestaurant(restaurant); // Assign to current restaurant
        if (dto.discountType != null) {
            try { v.setDiscountType(DiscountType.valueOf(dto.discountType)); }
            catch (IllegalArgumentException ignored) {}
        }
        if (dto.expirationDate != null && !dto.expirationDate.isBlank()) {
            v.setExpirationDate(LocalDate.parse(dto.expirationDate));
        }
        v.setActive(dto.isActive);
        voucherRepository.save(v);
        return ResponseEntity.ok(Map.of("message", "Đã tạo voucher " + v.getCode()));
    }

    /** PUT /api/restaurant/vouchers/{id} — Cập nhật voucher */
    @PutMapping("/vouchers/{id}")
    public ResponseEntity<?> updateVoucher(Authentication auth, @PathVariable Long id,
                                           @RequestBody VoucherDTO dto) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v == null || v.getRestaurant() == null || !v.getRestaurant().getId().equals(restaurant.getId())) 
            return ResponseEntity.notFound().build();

        if (dto.code != null)          v.setCode(dto.code.trim().toUpperCase());
        if (dto.discountValue != null)  v.setDiscountValue(dto.discountValue);
        if (dto.discountType != null) {
            try { v.setDiscountType(DiscountType.valueOf(dto.discountType)); }
            catch (IllegalArgumentException ignored) {}
        }
        if (dto.expirationDate != null && !dto.expirationDate.isBlank()) {
            v.setExpirationDate(LocalDate.parse(dto.expirationDate));
        }
        v.setActive(dto.isActive);
        voucherRepository.save(v);
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật voucher"));
    }

    /** PATCH /api/restaurant/vouchers/{id}/toggle — Bật/tắt voucher */
    @PatchMapping("/vouchers/{id}/toggle")
    public ResponseEntity<?> toggleVoucher(Authentication auth, @PathVariable Long id) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v == null || v.getRestaurant() == null || !v.getRestaurant().getId().equals(restaurant.getId())) 
            return ResponseEntity.notFound().build();
        v.setActive(!v.isActive());
        voucherRepository.save(v);
        return ResponseEntity.ok(Map.of("isActive", v.isActive(),
                "message", v.isActive() ? "Đã bật voucher" : "Đã tắt voucher"));
    }

    /** DELETE /api/restaurant/vouchers/{id} — Xóa voucher */
    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<?> deleteVoucher(Authentication auth, @PathVariable Long id) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v == null || v.getRestaurant() == null || !v.getRestaurant().getId().equals(restaurant.getId())) 
            return ResponseEntity.notFound().build();
        voucherRepository.delete(v);
        return ResponseEntity.ok(Map.of("message", "Đã xóa voucher"));
    }

    // ================================================================
    // 5. REVIEW MANAGEMENT
    // ================================================================

    /** GET /api/restaurant/reviews — Danh sách đánh giá */
    @GetMapping("/reviews")
    public ResponseEntity<?> getReviews(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        List<Review> reviews = reviewRepository.findByRestaurant(restaurant);
        Double avg = reviewRepository.avgRatingByRestaurant(restaurant);

        List<ReviewDTO> dtos = reviews.stream().map(r -> new ReviewDTO(
                r.getId(),
                r.getOrder().getId(),
                r.getOrder().getCustomer().getUser().getFullName(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt() != null ? r.getCreatedAt().toString() : null
        )).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "reviews", dtos,
                "avgRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0,
                "totalReviews", dtos.size()
        ));
    }

    // ================================================================
    // DTOs
    // ================================================================

    public static class MenuItemDTO {
        public Long id;
        public String name;
        public String description;
        public Double price;
        public String imageUrl;
        public boolean isAvailable;
        public String categoryName;
        public Long categoryId;

        public MenuItemDTO() {}

        public MenuItemDTO(Long id, String name, String description, Double price,
                           String imageUrl, boolean isAvailable, String categoryName, Long categoryId) {
            this.id = id; this.name = name; this.description = description;
            this.price = price; this.imageUrl = imageUrl; this.isAvailable = isAvailable;
            this.categoryName = categoryName; this.categoryId = categoryId;
        }
    }

    public static class OrderItemDTO {
        public String itemName;
        public int quantity;
        public Double price;
        public String imageUrl;

        public OrderItemDTO(String itemName, int quantity, Double price, String imageUrl) {
            this.itemName = itemName; this.quantity = quantity;
            this.price = price; this.imageUrl = imageUrl;
        }
    }

    public static class OrderDTO {
        public Long id;
        public String customerName;
        public String customerPhone;
        public List<OrderItemDTO> items;
        public String status;
        public Double totalAmount;
        public Double shippingFee;
        public String deliveryAddress;
        public String orderTime;
        public String driverName;
        public String driverPhone;
        public String trackingPhase; // Phase mới nhất từ GPS log

        public OrderDTO(Long id, String customerName, String customerPhone,
                        List<OrderItemDTO> items, String status, Double totalAmount, Double shippingFee,
                        String deliveryAddress, String orderTime,
                        String driverName, String driverPhone, String trackingPhase) {
            this.id = id; this.customerName = customerName; this.customerPhone = customerPhone;
            this.items = items; this.status = status; this.totalAmount = totalAmount; this.shippingFee = shippingFee;
            this.deliveryAddress = deliveryAddress; this.orderTime = orderTime;
            this.driverName = driverName; this.driverPhone = driverPhone;
            this.trackingPhase = trackingPhase;
        }
    }

    public static class VoucherDTO {
        public Long id;
        public String code;
        public Double discountValue;
        public String discountType;
        public String expirationDate;
        public boolean isActive;

        public VoucherDTO() {}

        public VoucherDTO(Long id, String code, Double discountValue, String discountType,
                          String expirationDate, boolean isActive) {
            this.id = id; this.code = code; this.discountValue = discountValue;
            this.discountType = discountType; this.expirationDate = expirationDate;
            this.isActive = isActive;
        }
    }

    public static class ReviewDTO {
        public Long id;
        public Long orderId;
        public String customerName;
        public Integer rating;
        public String comment;
        public String createdAt;

        public ReviewDTO(Long id, Long orderId, String customerName,
                         Integer rating, String comment, String createdAt) {
            this.id = id; this.orderId = orderId; this.customerName = customerName;
            this.rating = rating; this.comment = comment; this.createdAt = createdAt;
        }
    }
}
