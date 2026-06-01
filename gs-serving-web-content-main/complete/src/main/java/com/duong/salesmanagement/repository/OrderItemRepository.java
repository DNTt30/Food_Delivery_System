package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.OrderItem;
import com.duong.salesmanagement.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.duong.salesmanagement.model.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(FoodOrder order);

    @Query("SELECT o.menuItem.id, o.menuItem.name, SUM(o.quantity) as totalSold, o.menuItem.restaurant.restaurantName " +
           "FROM OrderItem o " +
           "WHERE o.order.status = :status " +
           "GROUP BY o.menuItem.id, o.menuItem.name, o.menuItem.restaurant.restaurantName " +
           "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts(@Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT o.menuItem.id, o.menuItem.name, SUM(o.quantity) as totalSold, o.menuItem.restaurant.restaurantName " +
           "FROM OrderItem o " +
           "WHERE o.order.status = :status AND o.order.orderTime >= :start AND o.order.orderTime <= :end " +
           "GROUP BY o.menuItem.id, o.menuItem.name, o.menuItem.restaurant.restaurantName " +
           "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProductsByDateRange(@Param("status") OrderStatus status, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end, Pageable pageable);
}
