package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class FoodOrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Test
    public void whenFindByRestaurant_thenReturnOrders() {
        // given
        User user = new User();
        user.setUsername("restaurant_owner");
        user.setPassword("password");
        user.setFullName("Restaurant Owner");
        user.setEmail("owner@test.com");
        user.setRole(Role.RESTAURANT);
        entityManager.persist(user);

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setUser(user);
        restaurant.setRestaurantName("Test Restaurant");
        entityManager.persist(restaurant);

        User customerUser = new User();
        customerUser.setUsername("customer");
        customerUser.setPassword("password");
        customerUser.setFullName("Customer Name");
        customerUser.setEmail("customer@test.com");
        customerUser.setRole(Role.CUSTOMER);
        entityManager.persist(customerUser);

        CustomerProfile customer = new CustomerProfile();
        customer.setUser(customerUser);
        entityManager.persist(customer);

        FoodOrder order = new FoodOrder();
        order.setRestaurant(restaurant);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderTime(LocalDateTime.now());
        entityManager.persist(order);
        entityManager.flush();

        // when
        List<FoodOrder> found = foodOrderRepository.findByRestaurant(restaurant);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getRestaurant().getRestaurantName())
                .isEqualTo(restaurant.getRestaurantName());
    }

    @Test
    public void whenFindByStatus_thenReturnOrders() {
        // given
        User user = new User();
        user.setUsername("restaurant_owner2");
        user.setPassword("password");
        user.setFullName("Restaurant Owner");
        user.setEmail("owner2@test.com");
        user.setRole(Role.RESTAURANT);
        entityManager.persist(user);

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setUser(user);
        restaurant.setRestaurantName("Test Restaurant 2");
        entityManager.persist(restaurant);

        User customerUser = new User();
        customerUser.setUsername("customer2");
        customerUser.setPassword("password");
        customerUser.setFullName("Customer Name");
        customerUser.setEmail("customer2@test.com");
        customerUser.setRole(Role.CUSTOMER);
        entityManager.persist(customerUser);

        CustomerProfile customer = new CustomerProfile();
        customer.setUser(customerUser);
        entityManager.persist(customer);

        FoodOrder order = new FoodOrder();
        order.setStatus(OrderStatus.PREPARING);
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setOrderTime(LocalDateTime.now());
        entityManager.persist(order);
        entityManager.flush();

        // when
        List<FoodOrder> found = foodOrderRepository.findByStatus(OrderStatus.PREPARING);

        // then
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getStatus()).isEqualTo(OrderStatus.PREPARING);
    }
}
