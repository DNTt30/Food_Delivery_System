package com.duong.salesmanagement.service;

import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.OrderTrackingLocation;
import com.duong.salesmanagement.model.TrackingPhase;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import com.duong.salesmanagement.repository.OrderTrackingLocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationTrackingService {

    private static final Logger log = LoggerFactory.getLogger(LocationTrackingService.class);

    private final OrderTrackingLocationRepository trackingRepository;
    private final FoodOrderRepository foodOrderRepository;

    public LocationTrackingService(OrderTrackingLocationRepository trackingRepository,
                                   FoodOrderRepository foodOrderRepository) {
        this.trackingRepository = trackingRepository;
        this.foodOrderRepository = foodOrderRepository;
    }

    /**
     * Hàm lưu tọa độ vào Database.
     * Chạy ở một luồng riêng biệt (Async) để không làm delay việc gửi tọa độ lên WebSocket của khách.
     */
    @Async
    @Transactional
    public void saveTrackingHistory(Long orderId, Double lat, Double lng, TrackingPhase phase) {
        if (orderId == null) return;
        try {
            // TỐI ƯU HÓA: Dùng getReferenceById thay vì findById
            // Hibernate Proxy giúp tránh một câu lệnh SELECT dư thừa.
            FoodOrder orderProxy = foodOrderRepository.getReferenceById(orderId);

            OrderTrackingLocation location = new OrderTrackingLocation();
            location.setOrder(orderProxy);
            location.setLatitude(lat);
            location.setLongitude(lng);
            location.setTrackingPhase(phase);

            trackingRepository.save(location);
            
            log.debug("Saved location history for Order {}: [{}, {}] - Phase: {}", orderId, lat, lng, phase);
        } catch (Exception e) {
            log.error("Lỗi khi lưu lịch sử tracking cho đơn hàng {}: {}", orderId, e.getMessage());
        }
    }
}
