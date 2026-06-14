package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByRestaurant(RestaurantProfile restaurant);

    @Query("SELECT DISTINCT o FROM FoodOrder o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.menuItem LEFT JOIN FETCH o.customer WHERE o.restaurant = :r ORDER BY o.orderTime DESC")
    List<FoodOrder> findByRestaurantOrderByOrderTimeDesc(@Param("r") RestaurantProfile restaurant);

    Page<FoodOrder> findByRestaurant(RestaurantProfile restaurant, Pageable pageable);

    @Query("SELECT DISTINCT o FROM FoodOrder o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.menuItem LEFT JOIN FETCH o.restaurant WHERE o.customer = :c ORDER BY o.orderTime DESC")
    List<FoodOrder> findByCustomerOrderByOrderTimeDesc(@Param("c") CustomerProfile customer);

    List<FoodOrder> findByDriverOrderByOrderTimeDesc(DriverProfile driver);
    List<FoodOrder> findByStatus(OrderStatus status);
    List<FoodOrder> findByDriverIsNullAndStatus(OrderStatus status);
    List<FoodOrder> findByDriverAndStatus(DriverProfile driver, OrderStatus status);
    List<FoodOrder> findAllByOrderByOrderTimeDesc();
    List<FoodOrder> findByCustomer_User_Id(String userId);
    List<FoodOrder> findTop5ByStatusNotOrderByOrderTimeDesc(OrderStatus status);
    List<FoodOrder> findByStatusOrderByOrderTimeDesc(OrderStatus status);
    List<FoodOrder> findByOrderTimeAfterOrderByOrderTimeDesc(LocalDateTime since);
    List<FoodOrder> findByStatusAndOrderTimeAfterOrderByOrderTimeDesc(OrderStatus status, LocalDateTime since);

    // Pageable versions for admin orders page
    org.springframework.data.domain.Page<FoodOrder> findByStatusNot(OrderStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<FoodOrder> findByStatus(OrderStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<FoodOrder> findByOrderTimeAfterAndStatusNot(LocalDateTime since, OrderStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<FoodOrder> findByStatusAndOrderTimeAfter(OrderStatus status, LocalDateTime since, org.springframework.data.domain.Pageable pageable);

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

    @Query("SELECT COUNT(o) FROM FoodOrder o WHERE o.customer.id = :customerId AND o.status <> com.duong.salesmanagement.model.OrderStatus.CANCELLED AND (o.foodVoucherCode = :code OR o.shippingVoucherCode = :code)")
    long countVoucherUsageByCustomer(@Param("customerId") Long customerId, @Param("code") String code);
}
