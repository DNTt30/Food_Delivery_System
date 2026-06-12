package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DatabaseCheckTest {

    @Autowired
    private RestaurantProfileRepository repo;

    @Test
    public void printRestaurants() {
        System.out.println("====================================================");
        System.out.println("RESTAURANTS FOUND IN DATABASE: " + repo.count());
        repo.findAll().forEach(r -> {
            System.out.println("Restaurant: " + r.getRestaurantName() + " (username: " + (r.getUser() != null ? r.getUser().getUsername() : "null") + ", rating: " + r.getAverageRating() + ")");
        });
        System.out.println("====================================================");
    }
}
