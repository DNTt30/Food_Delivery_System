package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.MenuItem;
import com.duong.salesmanagement.model.RestaurantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurant(RestaurantProfile restaurant);
    List<MenuItem> findByRestaurantAndIsAvailableTrue(RestaurantProfile restaurant);
    List<MenuItem> findTop10ByIsAvailableTrueOrderBySoldCountDesc();

    // Tìm kiếm theo tên và sắp xếp theo đánh giá giảm dần
    List<MenuItem> findByNameContainingIgnoreCaseAndIsAvailableTrueOrderByAverageRatingDesc(String keyword);

    // Tìm kiếm theo danh mục (by id)
    List<MenuItem> findByCategory_IdAndIsAvailableTrue(Long categoryId);

    // Tìm theo tên danh mục (gán qua category.name)
    List<MenuItem> findByCategory_NameContainingIgnoreCaseAndIsAvailableTrue(String categoryName);

    // Fallback: tìm theo tên hoặc mô tả món ăn (khi category không có dữ liệu gán sẵn)
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
           " ORDER BY m.averageRating DESC")
    List<MenuItem> findByNameOrDescriptionContainingIgnoreCaseAndIsAvailableTrue(@Param("keyword") String keyword);
}
