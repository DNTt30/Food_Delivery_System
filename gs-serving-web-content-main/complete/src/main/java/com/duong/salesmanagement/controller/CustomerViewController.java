package com.duong.salesmanagement.controller;

import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import com.duong.salesmanagement.service.OrderService;

@Controller
@RequestMapping("/customer")
public class CustomerViewController {

    private final OrderService orderService;
    private final FoodOrderRepository foodOrderRepository;

    public CustomerViewController(OrderService orderService, FoodOrderRepository foodOrderRepository) {
        this.orderService = orderService;
        this.foodOrderRepository = foodOrderRepository;
    }

    /**
     * Hiển thị danh sách đơn hàng đang xử lý
     */
    @GetMapping("/orders")
    public String showOrdersPage(@AuthenticationPrincipal User user, Model model) {
        java.util.List<FoodOrder> activeOrders;
        
        if (user != null) {
            // Lấy các đơn hàng thực tế của User
            activeOrders = orderService.getOrdersByUser(user).stream()
                .filter(o -> o.getStatus() != com.duong.salesmanagement.model.OrderStatus.COMPLETED 
                          && o.getStatus() != com.duong.salesmanagement.model.OrderStatus.CANCELLED
                          && o.getStatus() != com.duong.salesmanagement.model.OrderStatus.AWAITING_PAYMENT)
                .collect(java.util.stream.Collectors.toList());
        } else {
            // Nếu chưa login (test), lấy toàn bộ đơn hàng đang Delivering/Preparing
            activeOrders = foodOrderRepository.findAll().stream()
                    .filter(o -> o.getStatus() != com.duong.salesmanagement.model.OrderStatus.COMPLETED)
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        model.addAttribute("activeOrders", activeOrders);
        return "customer/orders";
    }

    /**
     * Hiển thị trang theo dõi đơn hàng
     */
    @GetMapping("/tracking")
    public String showTrackingPage(@RequestParam Long orderId,
                                   @AuthenticationPrincipal User user,
                                   Model model) {

        // 1. Lấy thông tin đơn hàng trước
        Optional<FoodOrder> orderOpt = orderService.getOrderById(orderId);

        if (orderOpt.isPresent()) {
            FoodOrder order = orderOpt.get();

            if (order.getStatus() == com.duong.salesmanagement.model.OrderStatus.AWAITING_PAYMENT) {
                return "redirect:/customer/cart?error=payment_required&orderId=" + orderId;
            }

            // 2. Kiểm tra quyền (Nếu có user thì check, nếu không có user thì cho qua để test)
            if (user != null && !orderService.hasPermissionToTrackOrder(orderId, user)) {
                return "redirect:/customer/orders?error=access_denied";
            }

            // 3. Self-healing logic for old orders that were created without coordinates
            boolean updated = false;
            if (order.getRestaurantLat() == null && order.getRestaurant().getLatitude() != null) {
                order.setRestaurantLat(order.getRestaurant().getLatitude());
                order.setRestaurantLng(order.getRestaurant().getLongitude());
                updated = true;
            }
            if (order.getDeliveryLat() == null && order.getCustomer().getLatitude() != null) {
                order.setDeliveryLat(order.getCustomer().getLatitude());
                order.setDeliveryLng(order.getCustomer().getLongitude());
                updated = true;
            }
            if (updated) {
                foodOrderRepository.save(order);
            }

            model.addAttribute("order", order);
            return "customer/tracking";
        }

        return "redirect:/customer/orders?error=order_not_found";
    }

    /**
     * Hiển thị trang lịch sử giao dịch
     */
    @GetMapping("/payment_history")
    public String showPaymentHistoryPage() {
        return "customer/payment_history";
    }
}
