package com.duong.salesmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // 1. Auth & Chung
    @GetMapping("/common/auth")
    public String authPage() { return "common/auth"; }

    @GetMapping("/common/profile")
    public String profilePage() { return "common/profile"; }

    // 2. Customer
    @GetMapping("/customer/home")
    public String customerHome() { return "customer/home"; }

    @GetMapping("/customer/dashboard") // Fallback matching older link
    public String customerDashboard() { return "customer/home"; }

    @GetMapping("/customer/detail")
    public String customerDetail() { return "customer/detail"; }

    @GetMapping("/customer/cart")
    public String customerCart() { return "customer/cart"; }

    @GetMapping("/customer/tracking")
    public String customerTracking() { return "customer/tracking"; }

    @GetMapping("/customer/history")
    public String customerHistory() { return "customer/history"; }

    // 3. Restaurant
    @GetMapping("/restaurant/dashboard")
    public String restaurantDashboard() { return "restaurant/dashboard"; }

    @GetMapping("/restaurant/menu")
    public String restaurantMenu() { return "restaurant/menu"; }

    @GetMapping("/restaurant/orders")
    public String restaurantOrders() { return "restaurant/orders"; }

    // 4. Driver
    @GetMapping("/driver/dashboard") // Fallback
    public String driverDashboard() { return "driver/new_orders"; }

    @GetMapping("/driver/new_orders")
    public String driverNewOrders() { return "driver/new_orders"; }

    @GetMapping("/driver/delivering")
    public String driverDelivering() { return "driver/delivering"; }

    // 5. Admin
    @GetMapping("/admin/dashboard")
    public String adminDashboard() { return "admin/dashboard"; }

    @GetMapping("/admin/partners")
    public String adminPartners() { return "admin/partners"; }

    @GetMapping("/admin/promotions")
    public String adminPromotions() { return "admin/promotions"; }
}
