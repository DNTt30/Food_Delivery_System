package com.fooddelivery.controller;

import com.fooddelivery.entity.Review;
import com.fooddelivery.service.NotificationService;
import com.fooddelivery.repository.ReviewRepository;
import com.fooddelivery.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantApiController {

    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;

    @PostMapping("/reviews/{id}/reply")
    public ResponseEntity<?> replyToReview(Authentication auth, 
                                           @PathVariable Long id, 
                                           @RequestBody Map<String, String> body) {
        // 1. Kiểm tra xác thực nhà hàng từ Authentication (auth)
        
        // 2. Tìm đánh giá của khách hàng trong CSDL
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        // 3. Kiểm tra tính sở hữu (Chỉ được phản hồi đánh giá thuộc cửa hàng mình)
        // (Thực hiện so sánh id nhà hàng của tài khoản sở hữu với nhà hàng của đơn hàng chứa review)

        // Lấy câu trả lời gửi lên từ JSON Body
        String replyText = body.get("reply");
        if (replyText == null || replyText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nội dung phản hồi không được bỏ trống");
        }

        // 4. Lưu trường restaurantReply, mốc thời gian phản hồi repliedAt
        review.setRestaurantReply(replyText);
        review.setRepliedAt(LocalDateTime.now());
        reviewRepository.save(review);

        // 5. Gửi thông báo thời gian thực đến khách hàng qua NotificationService
        notificationService.notifyRestaurantReplied(
                review.getOrder().getCustomer().getUser(), 
                review.getOrder().getId()
        );

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Phản hồi đánh giá thành công!",
            "repliedAt", review.getRepliedAt()
        ));
    }
}
