package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantProfileRepository extends JpaRepository<RestaurantProfile, Long> {
    Optional<RestaurantProfile> findByUser(User user);
}
