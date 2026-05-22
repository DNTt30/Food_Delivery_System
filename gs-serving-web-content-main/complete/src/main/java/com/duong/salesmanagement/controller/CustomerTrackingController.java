package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.dto.TrackingResponseDTO;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.OrderTrackingLocation;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import com.duong.salesmanagement.repository.OrderTrackingLocationRepository;
import com.duong.salesmanagement.service.IOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/customer/orders")
public class CustomerTrackingController {

    private final FoodOrderRepository foodOrderRepository;
    private final OrderTrackingLocationRepository trackingLocationRepository;
    private final IOrderService orderService;

    public CustomerTrackingController(FoodOrderRepository foodOrderRepository,
                                      OrderTrackingLocationRepository trackingLocationRepository,
                                      IOrderService orderService) {
        this.foodOrderRepository = foodOrderRepository;
        this.trackingLocationRepository = trackingLocationRepository;
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}/live-location")
    public ResponseEntity<?> getLiveLocation(@PathVariable Long orderId, 
                                             @AuthenticationPrincipal User customer) {
        
        if (orderId == null) return ResponseEntity.badRequest().body("Order ID is required");

        // 1. Kiểm tra Access Control
        if (!orderService.hasPermissionToTrackOrder(orderId, customer)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền theo dõi đơn hàng này");
        }

        Optional<FoodOrder> orderOpt = foodOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) return ResponseEntity.notFound().build();
        FoodOrder order = orderOpt.get();

        // 2. Lấy tọa độ mới nhất từ DB
        OrderTrackingLocation latestLocation = trackingLocationRepository.findFirstByOrderIdOrderByTimestampDesc(orderId);

        if (latestLocation == null) {
            return ResponseEntity.noContent().build();
        }

        String driverName = order.getDriver() != null ? order.getDriver().getUser().getFullName() : "Tài xế";

        TrackingResponseDTO response = new TrackingResponseDTO(
                orderId,
                latestLocation.getLatitude(),
                latestLocation.getLongitude(),
                latestLocation.getTrackingPhase(),
                driverName,
                latestLocation.getTimestamp()
        );

        return ResponseEntity.ok(response);
    }
}
