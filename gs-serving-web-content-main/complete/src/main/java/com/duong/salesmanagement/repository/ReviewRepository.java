package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrder(FoodOrder order);
    boolean existsByOrder(FoodOrder order);

    @Query("SELECT r FROM Review r WHERE r.order.restaurant = :restaurant ORDER BY r.createdAt DESC")
    List<Review> findByRestaurant(@Param("restaurant") RestaurantProfile restaurant);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.order.restaurant = :restaurant")
    Double avgRatingByRestaurant(@Param("restaurant") RestaurantProfile restaurant);
}
