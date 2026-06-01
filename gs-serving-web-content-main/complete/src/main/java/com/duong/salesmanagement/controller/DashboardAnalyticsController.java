package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.dto.DashboardStatisticsDTO;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.Role;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.repository.RestaurantProfileRepository;
import com.duong.salesmanagement.repository.UserRepository;
import com.duong.salesmanagement.service.IDashboardAnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller cho Dashboard Analytics
 * Base path: /api/restaurant/analytics
 * Security: JWT Bearer token, role RESTAURANT only
 */
@RestController
@RequestMapping("/api/restaurant/analytics")
public class DashboardAnalyticsController {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final IDashboardAnalyticsService analyticsService;

    public DashboardAnalyticsController(UserRepository userRepository,
                                       RestaurantProfileRepository restaurantProfileRepository,
                                       IDashboardAnalyticsService analyticsService) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.analyticsService = analyticsService;
    }

    private RestaurantProfile getAuthenticatedRestaurant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null || user.getRole() != Role.RESTAURANT) return null;
        return restaurantProfileRepository.findByUser(user).orElse(null);
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Chưa đăng nhập hoặc không có quyền"));
    }

    /**
     * GET /api/restaurant/analytics/this-week
     * Thống kê tuần này
     */
    @GetMapping("/this-week")
    public ResponseEntity<?> getThisWeek(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        
        DashboardStatisticsDTO stats = analyticsService.getStatisticsThisWeek(restaurant);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/restaurant/analytics/this-month
     * Thống kê tháng này
     */
    @GetMapping("/this-month")
    public ResponseEntity<?> getThisMonth(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        
        DashboardStatisticsDTO stats = analyticsService.getStatisticsThisMonth(restaurant);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/restaurant/analytics/previous-month
     * Thống kê tháng trước
     */
    @GetMapping("/previous-month")
    public ResponseEntity<?> getPreviousMonth(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        
        DashboardStatisticsDTO stats = analyticsService.getStatisticsPreviousMonth(restaurant);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/restaurant/analytics/this-year
     * Thống kê năm nay
     */
    @GetMapping("/this-year")
    public ResponseEntity<?> getThisYear(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        
        DashboardStatisticsDTO stats = analyticsService.getStatisticsThisYear(restaurant);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/restaurant/analytics/custom?startDate=2026-01-01&endDate=2026-06-01
     * Thống kê tùy chọn khoảng thời gian
     */
    @GetMapping("/custom")
    public ResponseEntity<?> getCustomRange(Authentication auth,
                                           @RequestParam("startDate") String startDateStr,
                                           @RequestParam("endDate") String endDateStr) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ngày bắt đầu không được sau ngày kết thúc"));
            }
            
            DashboardStatisticsDTO stats = analyticsService.getStatisticsByDateRange(restaurant, startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Format ngày không hợp lệ. Sử dụng YYYY-MM-DD"));
        }
    }

    /**
     * GET /api/restaurant/analytics/best-sellers?startDate=2026-01-01&endDate=2026-06-01
     * Top 5 món ăn bán chạy nhất
     */
    @GetMapping("/best-sellers")
    public ResponseEntity<?> getBestSellers(Authentication auth,
                                          @RequestParam(value = "startDate", required = false) String startDateStr,
                                          @RequestParam(value = "endDate", required = false) String endDateStr) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        try {
            LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : LocalDate.now();
            
            DashboardStatisticsDTO.BestSellerDTO[] bestSellers = analyticsService.getTopBestSellers(restaurant, startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("bestSellers", bestSellers);
            response.put("count", bestSellers.length);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * GET /api/restaurant/analytics/slow-moving-items?startDate=2026-01-01&endDate=2026-06-01
     * Top 5 món ăn ế (slow moving items)
     */
    @GetMapping("/slow-moving-items")
    public ResponseEntity<?> getSlowMovingItems(Authentication auth,
                                               @RequestParam(value = "startDate", required = false) String startDateStr,
                                               @RequestParam(value = "endDate", required = false) String endDateStr) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();

        try {
            LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : LocalDate.now();
            
            DashboardStatisticsDTO.SlowMovingItemDTO[] slowItems = analyticsService.getSlowMovingItems(restaurant, startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("slowMovingItems", slowItems);
            response.put("count", slowItems.length);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }
}
