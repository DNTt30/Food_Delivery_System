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
@RequestMapping("/api/driver")
public class DriverApiController {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final OrderService orderService;

    public DriverApiController(UserRepository userRepository,
                               DriverProfileRepository driverProfileRepository,
                               OrderService orderService) {
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.orderService = orderService;
    }

    private DriverProfile getAuthenticatedDriver(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || user.getRole() != Role.DRIVER) return null;
        return driverProfileRepository.findByUser(user).orElseGet(() -> {
            DriverProfile p = new DriverProfile();
            p.setUser(user);
            return driverProfileRepository.save(p);
        });
    }

    // UC-16: Xem thống kê tài xế
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        DriverProfile driver = getAuthenticatedDriver(authentication);
        if (driver == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<FoodOrder> all = orderService.getDriverHistory(driver);
        long delivering = all.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERING).count();
        long completed = all.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long available = orderService.getAvailableOrdersForDriver().size();
        double totalEarnings = all.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(o -> o.getTotalAmount() * 0.1) // 10% commission
                .sum();

        return ResponseEntity.ok(Map.of(
                "activeDeliveries", delivering,
                "completedDeliveries", completed,
                "availableOrders", available,
                "totalEarnings", totalEarnings,
                "driverName", driver.getUser().getFullName(),
                "isAvailable", driver.isAvailable()
        ));
    }

    // UC-16: Danh sách đơn có thể nhận (PREPARING, chưa có tài xế)
    @GetMapping("/available-orders")
    public ResponseEntity<?> getAvailableOrders(Authentication authentication) {
        DriverProfile driver = getAuthenticatedDriver(authentication);
        if (driver == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<FoodOrder> orders = orderService.getAvailableOrdersForDriver();
        List<DriverOrderDTO> dtos = orders.stream().map(o -> mapToDriverOrderDTO(o)).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // UC-16: Nhận đơn
    @PostMapping("/orders/{id}/accept")
    public ResponseEntity<?> acceptOrder(Authentication authentication, @PathVariable Long id) {
        DriverProfile driver = getAuthenticatedDriver(authentication);
        if (driver == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            FoodOrder order = orderService.acceptOrderByDriver(id, driver);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã nhận đơn hàng #" + order.getId(),
                    "orderId", order.getId()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // UC-16: Hoàn thành giao hàng
    @PutMapping("/orders/{id}/complete")
    public ResponseEntity<?> completeDelivery(Authentication authentication, @PathVariable Long id) {
        DriverProfile driver = getAuthenticatedDriver(authentication);
        if (driver == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            orderService.completeDelivery(id, driver);
            return ResponseEntity.ok(Map.of("message", "Giao hàng thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Đơn đang giao của tài xế
    @GetMapping("/my-deliveries")
    public ResponseEntity<?> getMyDeliveries(Authentication authentication) {
        DriverProfile driver = getAuthenticatedDriver(authentication);
        if (driver == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<FoodOrder> orders = orderService.getDriverActiveDeliveries(driver);
        List<DriverOrderDTO> dtos = orders.stream().map(this::mapToDriverOrderDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Lịch sử giao hàng
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        DriverProfile driver = getAuthenticatedDriver(authentication);
        if (driver == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<FoodOrder> orders = orderService.getDriverHistory(driver);
        List<DriverOrderDTO> dtos = orders.stream().map(this::mapToDriverOrderDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Cập nhật trạng thái sẵn sàng của tài xế
    @PutMapping("/availability")
    public ResponseEntity<?> toggleAvailability(Authentication authentication) {
        DriverProfile driver = getAuthenticatedDriver(authentication);
        if (driver == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        driver.setAvailable(!driver.isAvailable());
        driverProfileRepository.save(driver);
        return ResponseEntity.ok(Map.of(
                "isAvailable", driver.isAvailable(),
                "message", driver.isAvailable() ? "Bạn đang trực tuyến" : "Bạn đã ngoại tuyến"
        ));
    }

    private DriverOrderDTO mapToDriverOrderDTO(FoodOrder o) {
        List<String> itemNames = o.getOrderItems() == null ? List.of() :
                o.getOrderItems().stream()
                        .map(oi -> oi.getQuantity() + "x " + oi.getMenuItem().getName())
                        .collect(Collectors.toList());
        return new DriverOrderDTO(
                o.getId(),
                o.getRestaurant().getRestaurantName(),
                o.getRestaurant().getAddress(),
                o.getDeliveryAddress(),
                o.getStatus().name(),
                o.getTotalAmount(),
                o.getOrderTime() != null ? o.getOrderTime().toString() : "",
                o.getCustomer().getUser().getFullName(),
                itemNames
        );
    }

    public static class DriverOrderDTO {
        public Long id;
        public String restaurantName;
        public String restaurantAddress;
        public String deliveryAddress;
        public String status;
        public Double totalAmount;
        public String orderTime;
        public String customerName;
        public List<String> items;

        public DriverOrderDTO(Long id, String restaurantName, String restaurantAddress,
                              String deliveryAddress, String status, Double totalAmount,
                              String orderTime, String customerName, List<String> items) {
            this.id = id; this.restaurantName = restaurantName; this.restaurantAddress = restaurantAddress;
            this.deliveryAddress = deliveryAddress; this.status = status; this.totalAmount = totalAmount;
            this.orderTime = orderTime; this.customerName = customerName; this.items = items;
        }
    }
}
