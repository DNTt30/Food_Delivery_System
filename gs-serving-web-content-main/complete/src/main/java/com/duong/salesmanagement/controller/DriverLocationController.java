package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.dto.LocationUpdateDTO;
import com.duong.salesmanagement.dto.TrackingResponseDTO;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.TrackingPhase;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.service.LocationTrackingService;
import com.duong.salesmanagement.service.OrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/driver")
public class DriverLocationController {

    private static final Logger logger = LoggerFactory.getLogger(DriverLocationController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final LocationTrackingService locationTrackingService;
    private final OrderService orderService;

    public DriverLocationController(SimpMessagingTemplate messagingTemplate,
                                    LocationTrackingService locationTrackingService,
                                    OrderService orderService) {
        this.messagingTemplate = messagingTemplate;
        this.locationTrackingService = locationTrackingService;
        this.orderService = orderService;
    }

    @PostMapping("/location")
    public ResponseEntity<?> updateLocation(@RequestBody LocationUpdateDTO request,
                                            @AuthenticationPrincipal User driverUser) {
        // 1. Authenticate check
        if (driverUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Yêu cầu đăng nhập");
        }

        if (request == null || request.getOrderId() == null) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ");
        }

        final Long orderId = request.getOrderId();
        Optional<FoodOrder> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng");
        }

        FoodOrder order = orderOpt.get();

        // 2. Validate Ownership
        if (order.getDriver() == null || !order.getDriver().getUser().getId().equals(driverUser.getId())) {
            logger.warn("SECURITY ALERT: Driver {} attempted to update GPS for Order {} which they do not own!", 
                         driverUser.getId(), order.getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền cập nhật tọa độ cho đơn hàng này");
        }

        // 3. Mapping OrderStatus to TrackingPhase
        TrackingPhase phase = determinePhase(order);

        // 4. Broadcast to Customer via STOMP
        TrackingResponseDTO response = new TrackingResponseDTO(
                order.getId(),
                request.getLatitude(),
                request.getLongitude(),
                phase,
                driverUser.getFullName(),
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/tracking/" + order.getId(), response);

        // 5. Async save to history
        locationTrackingService.saveTrackingHistory(order.getId(), request.getLatitude(), request.getLongitude(), phase);

        return ResponseEntity.ok().build();
    }

    private TrackingPhase determinePhase(FoodOrder order) {
        if (order.getStatus() == null) return TrackingPhase.DRIVER_ACCEPTED;
        
        switch (order.getStatus()) {
            case PENDING:
                return TrackingPhase.GOING_TO_RESTAURANT;
            case PREPARING:
                return TrackingPhase.WAITING_AT_RESTAURANT;
            case DELIVERING:
                return TrackingPhase.DELIVERING;
            case COMPLETED:
                return TrackingPhase.ARRIVED;
            default:
                return TrackingPhase.DRIVER_ACCEPTED;
        }
    }
}
