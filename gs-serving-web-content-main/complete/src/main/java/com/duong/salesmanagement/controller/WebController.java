package com.duong.salesmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // 0. Landing Page
    @GetMapping("/")
    public String index() { return "index"; }

    // 1. Auth & Chung
    @GetMapping("/common/auth")
    public String authPage() { return "common/auth"; }

    @GetMapping("/common/profile")
    public String profilePage() { return "common/profile"; }

    // 2. Customer
    @GetMapping("/customer/home")
    public String customerHome() { return "customer/home"; }

    @GetMapping("/customer/dashboard")
    public String customerDashboard() { return "customer/home"; }

    @GetMapping("/customer/feed")
    public String customerFeed() { return "customer/feed"; }

    @GetMapping("/customer/detail")
    public String customerDetail() { return "customer/detail"; }

    @GetMapping("/customer/cart")
    public String customerCart() { return "customer/cart"; }

    @GetMapping("/customer/carts")
    public String customerCartsList() { return "customer/carts"; }

    @GetMapping("/customer/favorites")
    public String customerFavorites() { return "customer/favorites"; }


    @GetMapping("/customer/history")
    public String customerHistory() { return "customer/history"; }

    // 3. Restaurant
    @GetMapping("/restaurant/dashboard")
    public String restaurantDashboard() { return "restaurant/dashboard"; }

    @GetMapping("/restaurant/menu")
    public String restaurantMenu() { return "restaurant/menu"; }

    @GetMapping("/restaurant/orders")
    public String restaurantOrders() { return "restaurant/orders"; }

    @GetMapping("/restaurant/vouchers")
    public String restaurantVouchers() { return "restaurant/vouchers"; }

    @GetMapping("/restaurant/reviews")
    public String restaurantReviews() { return "restaurant/reviews"; }

    @GetMapping("/restaurant/profile")
    public String restaurantProfile() { return "restaurant/profile"; }

    // 4. Driver
    @GetMapping("/driver/dashboard")
    public String driverDashboard() { return "driver/dashboard"; }

    @GetMapping("/driver/new_orders")
    public String driverNewOrders() { return "driver/new_orders"; }

    @GetMapping("/driver/delivering")
    public String driverDelivering() { return "driver/delivering"; }

    @GetMapping("/driver/active_order")
    public String driverActiveOrder() { return "driver/delivering"; }

    @GetMapping("/driver/settings")
    public String driverSettings() { return "driver/settings"; }

    @GetMapping("/driver/history")
    public String driverHistory() { return "driver/history"; }

    @GetMapping("/driver/profile")
    public String driverProfile() { return "driver/profile"; }

    @GetMapping("/driver/notifications")
    public String driverNotifications() { return "driver/notifications"; }

    // 5. Admin
    @GetMapping("/admin/dashboard")
    public String adminDashboard() { return "admin/dashboard"; }

    @GetMapping("/admin/partners")
    public String adminPartners() { return "admin/partners"; }

    @GetMapping("/admin/promotions")
    public String adminPromotions() { return "admin/promotions"; }

    @GetMapping("/admin/users")
    public String adminUsers() { return "admin/partners"; }

    @GetMapping("/admin/restaurants")
    public String adminRestaurants() { return "admin/partners"; }

    @GetMapping("/admin/profile")
    public String adminProfile() { return "admin/profile"; }

    @GetMapping("/admin/orders")
    public String adminOrders() { return "admin/orders"; }

    @GetMapping("/admin/notifications")
    public String adminNotifications() { return "admin/notifications"; }

    @GetMapping("/admin/reviews")
    public String adminReviews() { return "admin/reviews"; }

    @GetMapping("/customer/my-reviews")
    public String customerMyReviews() { return "customer/history"; }
}
