package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.OrderTrackingLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderTrackingLocationRepository extends JpaRepository<OrderTrackingLocation, Long> {
    List<OrderTrackingLocation> findByOrderIdOrderByTimestampAsc(Long orderId);
    
    // Lấy tọa độ mới nhất của đơn hàng
    OrderTrackingLocation findFirstByOrderIdOrderByTimestampDesc(Long orderId);
}
