package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/admin")

public class AdminApiController {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final VoucherRepository voucherRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final ReviewRepository reviewRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private OrderItemRepository orderItemRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private NotificationRepository notificationRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private BroadcastLogRepository broadcastLogRepository;

    public AdminApiController(UserRepository userRepository,
                              RestaurantProfileRepository restaurantProfileRepository,
                              VoucherRepository voucherRepository,
                              FoodOrderRepository foodOrderRepository,
                              ReviewRepository reviewRepository,
                              CustomerProfileRepository customerProfileRepository,
                              DriverProfileRepository driverProfileRepository) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.voucherRepository = voucherRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.reviewRepository = reviewRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.driverProfileRepository = driverProfileRepository;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        return user != null && user.getRole() == Role.ADMIN;
    }

    // UC-19: Thống kê hệ thống
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication authentication, @RequestParam(defaultValue = "all") String range) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        long totalUsers = userRepository.count();
        long totalRestaurants = restaurantProfileRepository.count();
        
        long totalOrders, pendingOrders, preparingOrders, deliveringOrders, completedOrders;
        Double revenueOpt;
        List<Object[]> topItems;
        
        if ("all".equalsIgnoreCase(range)) {
            totalOrders = foodOrderRepository.count();
            pendingOrders = foodOrderRepository.countByStatus(OrderStatus.PENDING);
            preparingOrders = foodOrderRepository.countByStatus(OrderStatus.PREPARING);
            deliveringOrders = foodOrderRepository.countByStatus(OrderStatus.DELIVERING);
            completedOrders = foodOrderRepository.countByStatus(OrderStatus.COMPLETED);
            revenueOpt = foodOrderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED);
            topItems = orderItemRepository.findTopSellingProducts(OrderStatus.COMPLETED, org.springframework.data.domain.PageRequest.of(0, 5));
        } else {
            java.time.LocalDateTime start;
            java.time.LocalDateTime end = java.time.LocalDateTime.now();
            java.time.LocalDate today = java.time.LocalDate.now();
            if ("today".equalsIgnoreCase(range)) {
                start = today.atStartOfDay();
            } else if ("week".equalsIgnoreCase(range)) {
                start = today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay();
            } else if ("month".equalsIgnoreCase(range)) {
                start = today.withDayOfMonth(1).atStartOfDay();
            } else if ("year".equalsIgnoreCase(range)) {
                start = today.withDayOfYear(1).atStartOfDay();
            } else {
                start = today.atStartOfDay(); // fallback
            }

            totalOrders = foodOrderRepository.countByOrderTimeBetween(start, end);
            pendingOrders = foodOrderRepository.countByStatusAndOrderTimeBetween(OrderStatus.PENDING, start, end);
            preparingOrders = foodOrderRepository.countByStatusAndOrderTimeBetween(OrderStatus.PREPARING, start, end);
            deliveringOrders = foodOrderRepository.countByStatusAndOrderTimeBetween(OrderStatus.DELIVERING, start, end);
            completedOrders = foodOrderRepository.countByStatusAndOrderTimeBetween(OrderStatus.COMPLETED, start, end);
            revenueOpt = foodOrderRepository.sumTotalAmountByStatusAndDateRange(OrderStatus.COMPLETED, start, end);
            topItems = orderItemRepository.findTopSellingProductsByDateRange(OrderStatus.COMPLETED, start, end, org.springframework.data.domain.PageRequest.of(0, 5));
        }
        
        double totalRevenue = revenueOpt != null ? revenueOpt : 0.0;

        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalRestaurants", totalRestaurants);
        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("preparingOrders", preparingOrders);
        stats.put("deliveringOrders", deliveringOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("totalRevenue", totalRevenue);

        List<java.util.Map<String, Object>> topProducts = topItems.stream().map(obj -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", obj[0]);
            map.put("name", obj[1]);
            map.put("sold", obj[2]);
            map.put("restaurant", obj[3]);
            return map;
        }).collect(Collectors.toList());
        stats.put("topProducts", topProducts);

        // Revenue by day (last 7 days) for line chart
        List<java.util.Map<String, Object>> revenueByDay = new java.util.ArrayList<>();
        java.time.LocalDate today2 = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate day = today2.minusDays(i);
            java.time.LocalDateTime dayStart = day.atStartOfDay();
            java.time.LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();
            Double rev = foodOrderRepository.sumTotalAmountByStatusAndDateRange(OrderStatus.COMPLETED, dayStart, dayEnd);
            java.util.Map<String, Object> dayData = new java.util.LinkedHashMap<>();
            dayData.put("date", day.toString());
            dayData.put("label", day.getDayOfMonth() + "/" + day.getMonthValue());
            dayData.put("revenue", rev != null ? rev : 0.0);
            revenueByDay.add(dayData);
        }
        stats.put("revenueByDay", revenueByDay);

        // Recent 5 orders
        List<FoodOrder> recentOrders = foodOrderRepository.findTop5ByStatusNotOrderByOrderTimeDesc(OrderStatus.PENDING_PAYMENT);
        List<java.util.Map<String, Object>> recentOrderDTOs = recentOrders.stream().map(o -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("customerName", o.getCustomer().getUser().getFullName());
            m.put("restaurantName", o.getRestaurant().getRestaurantName());
            m.put("totalAmount", o.getTotalAmount());
            m.put("status", o.getStatus().name());
            m.put("paymentMethod", o.getPaymentMethod());
            m.put("orderTime", o.getOrderTime() != null ? o.getOrderTime().toString() : "");
            return m;
        }).collect(Collectors.toList());
        stats.put("recentOrders", recentOrderDTOs);

        return ResponseEntity.ok(stats);
    }

    // UC-17: Quản lý tài khoản người dùng
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<UserDTO> dtos = userRepository.findAll().stream()
                .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getFullName(),
                        u.getEmail(), u.getRole().name(), u.isEnabled()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(Authentication authentication, @PathVariable String id) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (user.getRole() == Role.ADMIN)
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể khóa tài khoản Admin"));

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "message", user.isEnabled() ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản",
                "enabled", user.isEnabled()
        ));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(Authentication authentication, @PathVariable String id) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (user.getRole() == Role.ADMIN)
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể xóa tài khoản Admin"));

        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "Đã xóa tài khoản"));
    }

    @GetMapping("/users/{id}/details")
    public ResponseEntity<?> getUserDetails(Authentication authentication, @PathVariable String id) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole().name());
        result.put("enabled", user.isEnabled());

        if (user.getRole() == Role.CUSTOMER) {
            CustomerProfile customerProfile = customerProfileRepository.findByUser(user).orElse(null);
            if (customerProfile != null) {
                result.put("phone", customerProfile.getPhoneNumber());
                result.put("address", customerProfile.getDeliveryAddress());
                result.put("latitude", customerProfile.getLatitude());
                result.put("longitude", customerProfile.getLongitude());

                List<FoodOrder> orders = foodOrderRepository.findByCustomerOrderByOrderTimeDesc(customerProfile);
                result.put("orderCount", orders.size());
            }
        } else if (user.getRole() == Role.DRIVER) {
            DriverProfile driverProfile = driverProfileRepository.findByUser(user).orElse(null);
            if (driverProfile != null) {
                result.put("phone", driverProfile.getPhoneNumber());
                result.put("licensePlate", driverProfile.getLicensePlate());
                result.put("isAvailable", driverProfile.isAvailable());

                List<FoodOrder> orders = foodOrderRepository.findByDriverOrderByOrderTimeDesc(driverProfile);
                result.put("orderCount", orders.size());
            }
        } else if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfile restaurantProfile = restaurantProfileRepository.findByUser(user).orElse(null);
            if (restaurantProfile != null) {
                result.put("restaurantName", restaurantProfile.getRestaurantName());
                result.put("address", restaurantProfile.getAddress());
                result.put("bannerUrl", restaurantProfile.getBannerUrl());
                result.put("isOpen", restaurantProfile.isOpen());
                result.put("averageRating", restaurantProfile.getAverageRating());

                List<FoodOrder> orders = foodOrderRepository.findByRestaurant(restaurantProfile);
                result.put("orderCount", orders.size());
            }
        }

        return ResponseEntity.ok(result);
    }


    // UC-18: Quản lý nhà hàng (duyệt/khóa)
    @GetMapping("/restaurants")
    public ResponseEntity<?> getAllRestaurants(Authentication authentication) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<RestaurantAdminDTO> dtos = restaurantProfileRepository.findAll().stream()
                .map(r -> new RestaurantAdminDTO(
                        r.getId(),
                        r.getRestaurantName(),
                        r.getAddress(),
                        r.getUser().getUsername(),
                        r.getUser().getEmail(),
                        r.getBannerUrl(),
                        r.isOpen(),
                        r.getUser().isEnabled(),
                        r.getAverageRating()
                )).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/restaurants/{id}/toggle-status")
    public ResponseEntity<?> toggleRestaurantStatus(Authentication authentication, @PathVariable Long id) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        RestaurantProfile restaurant = restaurantProfileRepository.findById(id).orElse(null);
        if (restaurant == null) return ResponseEntity.notFound().build();

        User owner = restaurant.getUser();
        owner.setEnabled(!owner.isEnabled());
        userRepository.save(owner);
        
        // Nếu bị khóa, tự động đóng cửa hàng luôn
        if (!owner.isEnabled()) {
            restaurant.setOpen(false);
            restaurantProfileRepository.save(restaurant);
        }

        return ResponseEntity.ok(Map.of(
                "message", owner.isEnabled() ? "Đã mở khóa nhà hàng" : "Đã khóa nhà hàng",
                "ownerEnabled", owner.isEnabled()
        ));
    }

    @PutMapping("/restaurants/{id}/approve")
    public ResponseEntity<?> approveRestaurant(Authentication authentication, @PathVariable Long id) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        RestaurantProfile restaurant = restaurantProfileRepository.findById(id).orElse(null);
        if (restaurant == null) return ResponseEntity.notFound().build();

        User owner = restaurant.getUser();
        owner.setEnabled(true);
        restaurant.setOpen(true);
        userRepository.save(owner);
        restaurantProfileRepository.save(restaurant);
        return ResponseEntity.ok(Map.of("message", "Nhà hàng đã được duyệt và kích hoạt"));
    }

    @GetMapping("/restaurants/{id}/details")
    public ResponseEntity<?> getRestaurantDetails(Authentication authentication, @PathVariable Long id) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        RestaurantProfile restaurant = restaurantProfileRepository.findById(id).orElse(null);
        if (restaurant == null) return ResponseEntity.notFound().build();

        List<Review> reviews = reviewRepository.findByRestaurant(restaurant);
        List<Map<String, Object>> reviewDTOs = reviews.stream().map(r -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("rating", r.getRating());
            map.put("comment", r.getComment());
            map.put("createdAt", r.getCreatedAt().toString());
            map.put("customerName", r.getOrder().getCustomer().getUser().getFullName());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("restaurant", new RestaurantAdminDTO(
                restaurant.getId(), restaurant.getRestaurantName(), restaurant.getAddress(),
                restaurant.getUser().getUsername(), restaurant.getUser().getEmail(),
                restaurant.getBannerUrl(), restaurant.isOpen(), restaurant.getUser().isEnabled(),
                restaurant.getAverageRating()
        ));
        result.put("reviews", reviewDTOs);
        result.put("phone", null);
        result.put("latitude", restaurant.getLatitude());
        result.put("longitude", restaurant.getLongitude());

        Double revenue = foodOrderRepository.sumTotalAmountByRestaurantAndStatus(restaurant, OrderStatus.COMPLETED);
        result.put("totalRevenue", revenue != null ? revenue : 0.0);
        result.put("totalOrders", foodOrderRepository.findByRestaurant(restaurant).size());

        return ResponseEntity.ok(result);
    }

    // Quản lý đơn hàng toàn hệ thống (có phân trang)
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        java.time.LocalDateTime since = days > 0 && days <= 365
                ? java.time.LocalDateTime.now().minusDays(days) : null;

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("orderTime").descending());

        org.springframework.data.domain.Page<FoodOrder> pageResult;

        if ("all".equalsIgnoreCase(status)) {
            if (since != null)
                pageResult = foodOrderRepository.findByOrderTimeAfterAndStatusNot(
                        since, OrderStatus.PENDING_PAYMENT, pageable);
            else
                pageResult = foodOrderRepository.findByStatusNot(OrderStatus.PENDING_PAYMENT, pageable);
        } else {
            try {
                OrderStatus os = OrderStatus.valueOf(status.toUpperCase());
                if (since != null)
                    pageResult = foodOrderRepository.findByStatusAndOrderTimeAfter(os, since, pageable);
                else
                    pageResult = foodOrderRepository.findByStatus(os, pageable);
            } catch (IllegalArgumentException e) {
                pageResult = foodOrderRepository.findByStatusNot(OrderStatus.PENDING_PAYMENT, pageable);
            }
        }

        List<java.util.Map<String, Object>> content = pageResult.getContent().stream()
            .map(o -> {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", o.getId());
                m.put("customerName", o.getCustomer().getUser().getFullName());
                m.put("customerUsername", o.getCustomer().getUser().getUsername());
                m.put("restaurantName", o.getRestaurant().getRestaurantName());
                m.put("driverName", o.getDriver() != null ? o.getDriver().getUser().getFullName() : null);
                m.put("totalAmount", o.getTotalAmount());
                m.put("status", o.getStatus().name());
                m.put("paymentMethod", o.getPaymentMethod());
                m.put("paymentStatus", o.getPaymentStatus());
                m.put("deliveryAddress", o.getDeliveryAddress());
                m.put("orderTime", o.getOrderTime() != null ? o.getOrderTime().toString() : "");
                return m;
            }).collect(Collectors.toList());

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", content);
        response.put("totalElements", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("currentPage", pageResult.getNumber());
        response.put("pageSize", pageResult.getSize());
        response.put("hasNext", pageResult.hasNext());
        response.put("hasPrevious", pageResult.hasPrevious());

        return ResponseEntity.ok(response);
    }

    // UC-20: Quản lý mã khuyến mãi
    @GetMapping("/vouchers")
    public ResponseEntity<?> getAllVouchers(Authentication authentication) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(voucherRepository.findAll().stream()
                .map(this::mapVoucher).collect(Collectors.toList()));
    }

    @PostMapping("/vouchers")
    public ResponseEntity<?> createVoucher(Authentication authentication, @RequestBody VoucherRequest req) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (voucherRepository.findByCode(req.code).isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "Mã voucher đã tồn tại"));

        Voucher v = new Voucher();
        v.setCode(req.code.toUpperCase());
        v.setDiscountValue(req.discountValue);
        v.setDiscountType(req.discountType != null ? DiscountType.valueOf(req.discountType) : DiscountType.PERCENTAGE);
        if (req.expiryDate != null && !req.expiryDate.isBlank())
            v.setExpirationDate(LocalDate.parse(req.expiryDate));
        v.setActive(req.active);
        voucherRepository.save(v);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Tạo voucher thành công"));
    }

    @PutMapping("/vouchers/{id}")
    public ResponseEntity<?> updateVoucher(Authentication authentication, @PathVariable Long id,
                                            @RequestBody VoucherRequest req) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v == null) return ResponseEntity.notFound().build();

        if (req.code != null && !req.code.isBlank()) v.setCode(req.code.toUpperCase());
        v.setDiscountValue(req.discountValue);
        if (req.discountType != null) v.setDiscountType(DiscountType.valueOf(req.discountType));
        if (req.expiryDate != null && !req.expiryDate.isBlank())
            v.setExpirationDate(LocalDate.parse(req.expiryDate));
        v.setActive(req.active);
        voucherRepository.save(v);
        return ResponseEntity.ok(Map.of("message", "Cập nhật voucher thành công"));
    }

    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<?> deleteVoucher(Authentication authentication, @PathVariable Long id) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (!voucherRepository.existsById(id)) return ResponseEntity.notFound().build();
        voucherRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa voucher"));
    }

    private Map<String, Object> mapVoucher(Voucher v) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("code", v.getCode());
        m.put("discountValue", v.getDiscountValue());
        m.put("discountType", v.getDiscountType() != null ? v.getDiscountType().name() : "PERCENT");
        m.put("expiryDate", v.getExpirationDate() != null ? v.getExpirationDate().toString() : null);
        m.put("active", v.isActive());
        m.put("restaurantName", v.getRestaurant() != null ? v.getRestaurant().getRestaurantName() : null);
        m.put("restaurantId", v.getRestaurant() != null ? v.getRestaurant().getId() : null);
        return m;
    }

    // UC: Quản lý thông báo hệ thống (Broadcast)
    @PostMapping("/notifications/broadcast")
    public ResponseEntity<?> broadcastNotification(Authentication authentication, @RequestBody BroadcastRequest req) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (req.title == null || req.title.isBlank() || req.message == null || req.message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tiêu đề và nội dung không được để trống"));
        }

        List<User> targets;
        String audience = req.targetAudience != null ? req.targetAudience.toUpperCase() : "ALL";
        switch (audience) {
            case "CUSTOMER":
                targets = userRepository.findByRole(Role.CUSTOMER);
                break;
            case "RESTAURANT":
                targets = userRepository.findByRole(Role.RESTAURANT);
                break;
            case "DRIVER":
                targets = userRepository.findByRole(Role.DRIVER);
                break;
            case "ALL":
            default:
                // Tất cả trừ admin
                targets = userRepository.findByRoleNot(Role.ADMIN);
                break;
        }

        // Tạo log
        BroadcastLog log = new BroadcastLog(req.title, req.message, audience);
        broadcastLogRepository.save(log);

        List<Notification> notifications = targets.stream()
                .map(u -> {
                    Notification n = new Notification(u, req.title, req.message, NotificationType.SYSTEM_ALERT, null);
                    n.setBroadcastLogId(log.getId());
                    return n;
                })
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);

        return ResponseEntity.ok(Map.of(
                "message", "Đã gửi thông báo thành công tới " + notifications.size() + " người dùng.",
                "count", notifications.size()
        ));
    }

    @GetMapping("/notifications/broadcasts")
    public ResponseEntity<?> getBroadcastHistory(Authentication authentication) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(broadcastLogRepository.findAllByOrderByCreatedAtDesc());
    }

    @Transactional
    @PutMapping("/notifications/broadcasts/{id}")
    public ResponseEntity<?> updateBroadcast(Authentication authentication, @PathVariable Long id, @RequestBody BroadcastRequest req) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        
        BroadcastLog log = broadcastLogRepository.findById(id).orElse(null);
        if (log == null) return ResponseEntity.notFound().build();

        if (req.title == null || req.title.isBlank() || req.message == null || req.message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tiêu đề và nội dung không được để trống"));
        }

        log.setTitle(req.title);
        log.setMessage(req.message);
        broadcastLogRepository.save(log);

        notificationRepository.updateByBroadcastLogId(id, req.title, req.message);

        return ResponseEntity.ok(Map.of("message", "Đã cập nhật thông báo thành công"));
    }

    @Transactional
    @DeleteMapping("/notifications/broadcasts/{id}")
    public ResponseEntity<?> deleteBroadcast(Authentication authentication, @PathVariable Long id) {
        if (!isAdmin(authentication)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        
        if (!broadcastLogRepository.existsById(id)) return ResponseEntity.notFound().build();

        notificationRepository.deleteByBroadcastLogId(id);
        broadcastLogRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("message", "Đã thu hồi thông báo thành công"));
    }

    // ---- DTOs ----
    public static class UserDTO {
        public String id;
        public String username;
        public String fullName;
        public String email;
        public String role;
        public boolean enabled;

        public UserDTO(String id, String username, String fullName, String email, String role, boolean enabled) {
            this.id = id; this.username = username; this.fullName = fullName;
            this.email = email; this.role = role; this.enabled = enabled;
        }
    }

    public static class RestaurantAdminDTO {
        public Long id;
        public String restaurantName;
        public String address;
        public String ownerUsername;
        public String ownerEmail;
        public String bannerUrl;
        public boolean open;
        public boolean ownerEnabled;
        public Double averageRating;

        public RestaurantAdminDTO(Long id, String restaurantName, String address, String ownerUsername,
                                  String ownerEmail, String bannerUrl, boolean open, boolean ownerEnabled,
                                  Double averageRating) {
            this.id = id; this.restaurantName = restaurantName; this.address = address;
            this.ownerUsername = ownerUsername; this.ownerEmail = ownerEmail; this.bannerUrl = bannerUrl;
            this.open = open; this.ownerEnabled = ownerEnabled; this.averageRating = averageRating;
        }
    }

    public static class VoucherRequest {
        public String code;
        public Double discountValue;
        public String discountType;
        public String expiryDate;
        public String startDate;
        public Double minOrderAmount;
        public Double maxDiscount;
        public String description;
        public boolean active;
    }

    public static class BroadcastRequest {
        public String title;
        public String message;
        public String targetAudience;
    }
}
