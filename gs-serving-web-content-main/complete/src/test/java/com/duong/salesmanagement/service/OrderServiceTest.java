package com.duong.salesmanagement.service;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class OrderServiceTest {

    @Mock
    private FoodOrderRepository foodOrderRepository;
    @Mock
    private DriverProfileRepository driverProfileRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private FoodOrder order;
    private DriverProfile driver;

    @BeforeEach
    void setUp() {
        User customerUser = new User();
        customerUser.setFullName("Customer Name");
        CustomerProfile customer = new CustomerProfile();
        customer.setId(1L);
        customer.setUser(customerUser);

        User restaurantUser = new User();
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setId(1L);
        restaurant.setUser(restaurantUser);
        restaurant.setRestaurantName("Test Restaurant");

        order = new FoodOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.PREPARING);
        order.setCustomer(customer);
        order.setRestaurant(restaurant);

        User driverUser = new User();
        driverUser.setFullName("Driver Name");
        
        driver = new DriverProfile();
        driver.setId(1L);
        driver.setUser(driverUser);
    }

    @Test
    public void whenAcceptOrder_thenStatusChangesToDelivering() {
        // given
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenReturn(order);

        // when
        FoodOrder acceptedOrder = orderService.acceptOrderByDriver(1L, driver);

        // then
        assertEquals(OrderStatus.DELIVERING, acceptedOrder.getStatus());
        assertEquals(driver, acceptedOrder.getDriver());
        verify(foodOrderRepository, times(1)).save(order);
    }

    @Test
    public void whenCancelPendingOrder_thenStatusChangesToCancelled() {
        // given
        order.setStatus(OrderStatus.PENDING);
        CustomerProfile customer = new CustomerProfile();
        customer.setId(1L);
        order.setCustomer(customer);
        
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        orderService.cancelOrder(1L, customer);

        // then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(foodOrderRepository, times(1)).save(order);
    }
}
