package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantProfileRepository extends JpaRepository<RestaurantProfile, Long> {
    Optional<RestaurantProfile> findByUser(User user);

    @Query("SELECT r FROM RestaurantProfile r WHERE " +
           "LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(r.address) LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<RestaurantProfile> searchByKeyword(@Param("kw") String keyword);
}
