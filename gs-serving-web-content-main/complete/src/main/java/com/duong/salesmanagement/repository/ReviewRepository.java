package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrder(FoodOrder order);
    boolean existsByOrder(FoodOrder order);
}
