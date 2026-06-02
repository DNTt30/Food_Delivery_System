package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByRestaurant(RestaurantProfile restaurant);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.menuItem", "customer"})
    List<FoodOrder> findByRestaurantOrderByOrderTimeDesc(RestaurantProfile restaurant);

    Page<FoodOrder> findByRestaurant(RestaurantProfile restaurant, Pageable pageable);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.menuItem", "restaurant"})
    List<FoodOrder> findByCustomerOrderByOrderTimeDesc(CustomerProfile customer);

    List<FoodOrder> findByDriverOrderByOrderTimeDesc(DriverProfile driver);
    List<FoodOrder> findByStatus(OrderStatus status);
    List<FoodOrder> findByDriverIsNullAndStatus(OrderStatus status);
    List<FoodOrder> findByDriverAndStatus(DriverProfile driver, OrderStatus status);
    List<FoodOrder> findAllByOrderByOrderTimeDesc();
    List<FoodOrder> findByCustomer_User_Id(String userId);

    List<FoodOrder> findByRestaurantAndOrderTimeBetweenOrderByOrderTimeDesc(
            RestaurantProfile restaurant, LocalDateTime from, LocalDateTime to);

    @Query("SELECT o FROM FoodOrder o WHERE o.restaurant = :r AND o.status = :s ORDER BY o.orderTime DESC")
    List<FoodOrder> findByRestaurantAndStatus(@Param("r") RestaurantProfile r, @Param("s") OrderStatus s);

    long countByStatus(OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM FoodOrder o WHERE o.status = :status")
    Double sumTotalAmountByStatus(@Param("status") OrderStatus status);

    long countByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndOrderTimeBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM FoodOrder o WHERE o.status = :status AND o.orderTime >= :start AND o.orderTime <= :end")
    Double sumTotalAmountByStatusAndDateRange(@Param("status") OrderStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM FoodOrder o WHERE o.restaurant = :r AND o.status = :status")
    Double sumTotalAmountByRestaurantAndStatus(@Param("r") RestaurantProfile restaurant, @Param("status") OrderStatus status);

    long countByRestaurantAndStatusAndOrderTimeBetween(RestaurantProfile restaurant, OrderStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(COALESCE(o.totalAmount, 0) - COALESCE(o.shippingFee, 0)), 0) FROM FoodOrder o WHERE o.restaurant = :r AND o.status = :status AND o.orderTime >= :start AND o.orderTime <= :end")
    Double sumNetRevenueByRestaurantAndStatusAndDateRange(@Param("r") RestaurantProfile restaurant, @Param("status") OrderStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
