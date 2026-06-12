package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.MenuItem;
import com.duong.salesmanagement.model.RestaurantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurant(RestaurantProfile restaurant);
    List<MenuItem> findByRestaurantAndIsAvailableTrue(RestaurantProfile restaurant);
    List<MenuItem> findTop10ByIsAvailableTrueOrderBySoldCountDesc();
    
    // Tìm kiếm theo tên và sắp xếp theo đánh giá giảm dần
    List<MenuItem> findByNameContainingIgnoreCaseAndIsAvailableTrueOrderByAverageRatingDesc(String keyword);
    
    // Tìm kiếm theo danh mục
    List<MenuItem> findByCategory_IdAndIsAvailableTrue(Long categoryId);
}
