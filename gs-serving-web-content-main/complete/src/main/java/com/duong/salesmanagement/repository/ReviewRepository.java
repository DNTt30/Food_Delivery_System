package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.Review;
import com.duong.salesmanagement.model.RestaurantProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Review với hỗ trợ phân trang
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r WHERE r.order.restaurant = :restaurant ORDER BY r.createdAt DESC")
    List<Review> findByRestaurant(@Param("restaurant") RestaurantProfile restaurant);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.order.restaurant = :restaurant")
    Double avgRatingByRestaurant(@Param("restaurant") RestaurantProfile restaurant);
    
    /**
     * Lấy danh sách review của nhà hàng với phân trang
     */
    @Query("SELECT r FROM Review r WHERE r.order.restaurant = :restaurant")
    Page<Review> findByRestaurant(@Param("restaurant") RestaurantProfile restaurant, Pageable pageable);
    
    /**
     * Lấy danh sách review của nhà hàng, sắp xếp theo rating (cao nhất trước)
     */
    @Query("SELECT r FROM Review r WHERE r.order.restaurant = :restaurant ORDER BY r.rating DESC")
    Page<Review> findByRestaurantOrderByRatingDesc(@Param("restaurant") RestaurantProfile restaurant, Pageable pageable);
    
    /**
     * Lấy danh sách review của nhà hàng, sắp xếp theo ngày tạo (mới nhất trước)
     */
    @Query("SELECT r FROM Review r WHERE r.order.restaurant = :restaurant ORDER BY r.createdAt DESC")
    Page<Review> findByRestaurantOrderByCreatedAtDesc(@Param("restaurant") RestaurantProfile restaurant, Pageable pageable);
    
    /**
     * Đếm số review chưa được phản hồi
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.order.restaurant.id = :restaurantId AND r.restaurantReply IS NULL")
    Long countUnrepliedReviews(@Param("restaurantId") Long restaurantId);
    
    /**
     * Đếm số review có ảnh
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.order.restaurant.id = :restaurantId AND r.imageUrl IS NOT NULL")
    Long countReviewsWithImages(@Param("restaurantId") Long restaurantId);
    
    /**
     * Lấy trung bình rating
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.order.restaurant.id = :restaurantId")
    Double getAverageRating(@Param("restaurantId") Long restaurantId);
    
    /**
     * Phân bố rating (số lượng review theo từng mức sao)
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.order.restaurant.id = :restaurantId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("restaurantId") Long restaurantId);

    /**
     * Tìm review của một đơn hàng cụ thể (dùng bởi CustomerApiController)
     */
    java.util.Optional<Review> findByOrder(com.duong.salesmanagement.model.FoodOrder order);

    /**
     * Tìm các review của nhiều đơn hàng cùng lúc để tránh N+1 Query
     */
    List<Review> findByOrderIn(List<com.duong.salesmanagement.model.FoodOrder> orders);
    
    /**
     * Kiểm tra đơn hàng đã được review chưa (dùng bởi OrderService)
     */
    boolean existsByOrder(com.duong.salesmanagement.model.FoodOrder order);
    
    /**
     * Đếm số review của một nhà hàng (fix lỗi logic của ReviewService)
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.order.restaurant.id = :restaurantId")
    Long countByRestaurantId(@Param("restaurantId") Long restaurantId);

    /**
     * ④ Lấy tất cả review của khách hàng, sắp xếp mới nhất trước (cho trang "Đánh giá của tôi")
     */
    List<Review> findByOrder_CustomerOrderByCreatedAtDesc(
        com.duong.salesmanagement.model.CustomerProfile customer);
}
