package com.fooddelivery.controller;

import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.CustomerProfile;
import com.fooddelivery.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerApiController {

    private final IOrderService orderService;

    @PostMapping("/orders/{orderId}/review")
    public ResponseEntity<?> reviewOrder(@PathVariable Long orderId,
                                         @RequestBody ReviewRequest request,
                                         @ModelAttribute CustomerProfile customer) {
        
        // Lấy trường imageUrl từ request payload chuyển tiếp tới orderService
        Review review = orderService.reviewOrder(
                orderId, 
                customer, 
                request.getRating(), 
                request.getComment(), 
                request.getImageUrl()
        );
        
        return ResponseEntity.ok(review);
    }

    // DTO tĩnh nhận payload JSON đánh giá gửi lên từ Client
    public static class ReviewRequest {
        private int rating;
        private String comment;
        private String imageUrl; // Trường URL ảnh đính kèm của sản phẩm
        
        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}
