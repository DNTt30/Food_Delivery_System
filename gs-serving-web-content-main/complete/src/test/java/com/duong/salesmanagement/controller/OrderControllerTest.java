package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import com.duong.salesmanagement.service.IOrderService;
import com.duong.salesmanagement.service.IShippingCalculationService;
import com.duong.salesmanagement.service.GeocodingService;
import com.duong.salesmanagement.security.JwtUtil;
import com.duong.salesmanagement.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerApiController.class)

public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;
    @MockBean
    private RestaurantProfileRepository restaurantProfileRepository;
    @MockBean
    private MenuItemRepository menuItemRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CustomerProfileRepository customerProfileRepository;
    @MockBean
    private VoucherRepository voucherRepository;
    @MockBean
    private ReviewRepository reviewRepository;
    @MockBean
    private IShippingCalculationService shippingCalculationService;
    @MockBean
    private GeocodingService geocodingService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void whenPlaceOrder_thenReturnCreated() throws Exception {
        // given
        User user = new User();
        user.setUsername("customer");
        user.setRole(Role.CUSTOMER);
        
        CustomerProfile customer = new CustomerProfile();
        customer.setUser(user);
        
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setId(1L);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOpen(true);

        FoodOrder order = new FoodOrder();
        order.setId(100L);
        order.setTotalAmount(250.0);

        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(customerProfileRepository.findByUser(any(User.class))).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(orderService.createOrder(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(order);

        CustomerApiController.PlaceOrderRequest request = new CustomerApiController.PlaceOrderRequest();
        request.restaurantId = 1L;
        request.deliveryAddress = "123 Street";
        request.items = new ArrayList<>();

        // when & then
        mockMvc.perform(post("/api/customer/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(100L))
                .andExpect(jsonPath("$.message").value("Đặt hàng thành công!"));
    }
}
