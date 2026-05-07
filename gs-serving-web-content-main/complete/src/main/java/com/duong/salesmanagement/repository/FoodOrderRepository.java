package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByRestaurant(RestaurantProfile restaurant);
    List<FoodOrder> findByCustomerOrderByOrderTimeDesc(CustomerProfile customer);
    List<FoodOrder> findByDriverOrderByOrderTimeDesc(DriverProfile driver);
    List<FoodOrder> findByStatus(OrderStatus status);
    List<FoodOrder> findByDriverIsNullAndStatus(OrderStatus status);
    List<FoodOrder> findByDriverAndStatus(DriverProfile driver, OrderStatus status);
    List<FoodOrder> findAllByOrderByOrderTimeDesc();
}
