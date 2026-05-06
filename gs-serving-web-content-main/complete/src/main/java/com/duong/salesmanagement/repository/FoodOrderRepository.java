package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.RestaurantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByRestaurant(RestaurantProfile restaurant);
}
