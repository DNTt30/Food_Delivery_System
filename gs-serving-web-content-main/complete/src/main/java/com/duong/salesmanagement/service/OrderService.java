package com.duong.salesmanagement.service;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(FoodOrderRepository foodOrderRepository,
                        OrderItemRepository orderItemRepository,
                        MenuItemRepository menuItemRepository) {
        this.foodOrderRepository = foodOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public FoodOrder createOrder(CustomerProfile customer, RestaurantProfile restaurant, List<OrderItemRequest> itemRequests, String deliveryAddress) {
        FoodOrder order = new FoodOrder();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(deliveryAddress);

        double totalAmount = 0;
        
        // Save order first to get ID for items (though cascade might handle it, explicit is often safer in complex flows)
        FoodOrder savedOrder = foodOrderRepository.save(order);

        for (OrderItemRequest req : itemRequests) {
            Long itemId = req.getMenuItemId();
            if (itemId == null) {
                throw new IllegalArgumentException("Menu item ID cannot be null");
            }
            MenuItem menuItem = menuItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemId));
            
            if (!menuItem.isAvailable()) {
                throw new RuntimeException("Item is not available: " + menuItem.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(req.getQuantity());
            orderItem.setPriceAtTimeOfOrder(menuItem.getPrice()); // Capture price at time of order
            
            orderItemRepository.save(orderItem);
            totalAmount += menuItem.getPrice() * req.getQuantity();
        }

        savedOrder.setTotalAmount(totalAmount);
        return foodOrderRepository.save(savedOrder);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, RestaurantProfile restaurant) {
        if (orderId == null) throw new IllegalArgumentException("Order ID cannot be null");
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new RuntimeException("Unauthorized: Order does not belong to this restaurant");
        }

        // Add logic for valid transitions if needed
        order.setStatus(newStatus);
        foodOrderRepository.save(order);
    }

    public List<FoodOrder> getRestaurantOrders(RestaurantProfile restaurant) {
        return foodOrderRepository.findByRestaurant(restaurant);
    }

    // DTO for order creation request
    public static class OrderItemRequest {
        private Long menuItemId;
        private int quantity;

        public Long getMenuItemId() { return menuItemId; }
        public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
