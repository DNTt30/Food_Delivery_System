package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho thống kê Dashboard & Analytics
 * Cung cấp queries chuyên biệt để lấy dữ liệu Best Sellers, Slow Moving Items
 */
@Repository
public interface DashboardAnalyticsRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Lấy Top N món ăn bán chạy nhất của nhà hàng trong khoảng thời gian
     * Dùng để hiển thị Best Sellers chart
     */
    @Query(value = """
        SELECT m.id, m.name, m.price, m.image_url, 
               SUM(oi.quantity) as totalSold,
               SUM(oi.quantity * oi.price_at_time_of_order) as totalRevenue
        FROM menu_items m
        LEFT JOIN order_items oi ON m.id = oi.menu_item_id
        LEFT JOIN food_orders o ON oi.order_id = o.id
        WHERE m.restaurant_id = :restaurantId
          AND (o.order_time IS NULL OR o.order_time BETWEEN :startDate AND :endDate)
          AND (o.status = 'COMPLETED' OR o.status IS NULL)
        GROUP BY m.id, m.name, m.price, m.image_url
        ORDER BY totalSold DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopBestSellersByRestaurant(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit
    );

    /**
     * Lấy Top N món ăn ế (Slow Moving Items) - ít bán, nhiều bị hủy
     */
    @Query(value = """
        SELECT m.id, m.name, m.price, m.image_url,
               SUM(oi.quantity) as totalSold,
               COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) as cancellationCount,
               CAST(COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) AS FLOAT) / 
               NULLIF(COUNT(DISTINCT o.id), 0) * 100 as cancellationRate
        FROM menu_items m
        LEFT JOIN order_items oi ON m.id = oi.menu_item_id
        LEFT JOIN food_orders o ON oi.order_id = o.id
        WHERE m.restaurant_id = :restaurantId
          AND (o.order_time IS NULL OR o.order_time BETWEEN :startDate AND :endDate)
        GROUP BY m.id, m.name, m.price, m.image_url
        HAVING SUM(oi.quantity) < 10 OR 
               COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) > 5
        ORDER BY totalSold ASC, cancellationCount DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSlowMovingItemsByRestaurant(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit
    );

    /**
     * Lấy doanh thu theo ngày cho chart
     */
    @Query(value = """
        SELECT DATE(o.order_time) as orderDate,
               SUM(COALESCE(o.total_amount, 0) - COALESCE(o.shipping_fee, 0)) as revenue,
               COUNT(o.id) as orderCount
        FROM food_orders o
        WHERE o.restaurant_id = :restaurantId
          AND o.order_time BETWEEN :startDate AND :endDate
          AND o.status = 'COMPLETED'
        GROUP BY DATE(o.order_time)
        ORDER BY orderDate ASC
        """, nativeQuery = true)
    List<Object[]> findDailyRevenueByRestaurant(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Tính tổng doanh thu của nhà hàng trong khoảng thời gian
     */
    @Query("""
        SELECT SUM(o.totalAmount - COALESCE(o.shippingFee, 0))
        FROM FoodOrder o
        WHERE o.restaurant.id = :restaurantId
          AND o.orderTime BETWEEN :startDate AND :endDate
          AND o.status = 'COMPLETED'
        """)
    Double findTotalRevenueByRestaurant(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Đếm số đơn hoàn thành trong khoảng thời gian
     */
    @Query("""
        SELECT COUNT(o.id)
        FROM FoodOrder o
        WHERE o.restaurant.id = :restaurantId
          AND o.orderTime BETWEEN :startDate AND :endDate
          AND o.status = 'COMPLETED'
        """)
    Long countCompletedOrders(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Đếm số đơn hủy trong khoảng thời gian
     */
    @Query("""
        SELECT COUNT(o.id)
        FROM FoodOrder o
        WHERE o.restaurant.id = :restaurantId
          AND o.orderTime BETWEEN :startDate AND :endDate
          AND o.status = 'CANCELLED'
        """)
    Long countCancelledOrders(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
