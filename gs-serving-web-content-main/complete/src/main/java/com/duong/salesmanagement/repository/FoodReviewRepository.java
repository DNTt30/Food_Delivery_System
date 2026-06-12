package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.FoodReview;
import com.duong.salesmanagement.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodReviewRepository extends JpaRepository<FoodReview, Long> {
    List<FoodReview> findByMenuItemOrderByCreatedAtDesc(MenuItem menuItem);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(f.rating) FROM FoodReview f WHERE f.menuItem.id = :menuItemId")
    Double getAverageRatingForMenuItem(@org.springframework.data.repository.query.Param("menuItemId") Long menuItemId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(f) FROM FoodReview f WHERE f.menuItem.id = :menuItemId")
    Long countByMenuItemId(@org.springframework.data.repository.query.Param("menuItemId") Long menuItemId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(f.rating) FROM FoodReview f WHERE f.menuItem.restaurant.id = :restaurantId")
    Double getAverageRatingByRestaurantId(@org.springframework.data.repository.query.Param("restaurantId") Long restaurantId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(f) FROM FoodReview f WHERE f.menuItem.restaurant.id = :restaurantId")
    Long countByRestaurantId(@org.springframework.data.repository.query.Param("restaurantId") Long restaurantId);
}
