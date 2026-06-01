package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.dto.ReviewDTO;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.Role;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.repository.RestaurantProfileRepository;
import com.duong.salesmanagement.repository.UserRepository;
import com.duong.salesmanagement.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller cho Review Management
 * Base path: /api/restaurant/reviews
 * Security: JWT Bearer token, role RESTAURANT only
 */
@RestController
@RequestMapping("/api/restaurant/reviews")
public class ReviewManagementController {
    
    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final ReviewService reviewService;
    
    public ReviewManagementController(UserRepository userRepository,
                                    RestaurantProfileRepository restaurantProfileRepository,
                                    ReviewService reviewService) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.reviewService = reviewService;
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
     * GET /api/restaurant/reviews?page=0&size=10&sort=newest
     * Lấy danh sách review với phân trang
     * @param page Trang (0-indexed)
     * @param size Số lượng review trên 1 trang
     * @param sort Sắp xếp: newest (mặc định), rating
     */
    @GetMapping
    public ResponseEntity<?> getReviews(Authentication auth,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(defaultValue = "newest") String sort) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        
        Pageable pageable = PageRequest.of(page, Math.min(size, 50)); // Max 50 items per page
        
        Page<ReviewDTO> reviewPage;
        if ("rating".equalsIgnoreCase(sort)) {
            reviewPage = reviewService.getReviewsByRestaurantSortedByRating(restaurant, pageable);
        } else {
            reviewPage = reviewService.getReviewsByRestaurantPaginated(restaurant, pageable);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewPage.getContent());
        response.put("currentPage", reviewPage.getNumber());
        response.put("totalItems", reviewPage.getTotalElements());
        response.put("totalPages", reviewPage.getTotalPages());
        response.put("hasNext", reviewPage.hasNext());
        response.put("hasPrevious", reviewPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/restaurant/reviews/{id}/reply
     * Phản hồi review từ nhà hàng
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<?> replyToReview(Authentication auth,
                                          @PathVariable Long id,
                                          @RequestBody Map<String, String> body) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        
        String reply = body.get("reply");
        if (reply == null || reply.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Phản hồi không được để trống"));
        }
        
        try {
            ReviewDTO updatedReview = reviewService.replyToReview(id, reply);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Phản hồi thành công!",
                    "review", updatedReview
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/restaurant/reviews/statistics
     * Lấy thống kê review
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(Authentication auth) {
        RestaurantProfile restaurant = getAuthenticatedRestaurant(auth);
        if (restaurant == null) return unauthorized();
        
        Map<String, Object> stats = reviewService.getReviewStatistics(restaurant);
        return ResponseEntity.ok(stats);
    }
}
