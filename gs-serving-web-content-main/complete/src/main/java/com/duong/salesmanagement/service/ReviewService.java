package com.duong.salesmanagement.service;

import com.duong.salesmanagement.dto.ReviewDTO;
import com.duong.salesmanagement.model.Review;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.repository.ReviewRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service quản lý Review (Đánh giá)
 * Hỗ trợ phân trang, lọc từ ngữ, phản hồi
 */
@Service
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ProfanityFilterService profanityFilterService;
    private final ObjectMapper objectMapper;
    
    public ReviewService(ReviewRepository reviewRepository, 
                        ProfanityFilterService profanityFilterService,
                        ObjectMapper objectMapper) {
        this.reviewRepository = reviewRepository;
        this.profanityFilterService = profanityFilterService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Lấy danh sách review với phân trang, sắp xếp theo ngày (mới nhất trước)
     */
    public Page<ReviewDTO> getReviewsByRestaurantPaginated(RestaurantProfile restaurant, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
        
        List<ReviewDTO> dtos = reviewPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, reviewPage.getTotalElements());
    }
    
    /**
     * Lấy danh sách review sắp xếp theo rating (cao nhất trước)
     */
    public Page<ReviewDTO> getReviewsByRestaurantSortedByRating(RestaurantProfile restaurant, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByRestaurantOrderByRatingDesc(restaurant, pageable);
        
        List<ReviewDTO> dtos = reviewPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, reviewPage.getTotalElements());
    }
    
    /**
     * Chuyển Review entity thành ReviewDTO (kèm format dữ liệu)
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setOrderId(review.getOrder().getId());
        dto.setCustomerName(review.getOrder().getCustomer().getUser().getFullName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setOriginalComment(review.getOriginalComment());
        dto.setHasInappropriateWords(review.getHasInappropriateWords());
        dto.setCreatedAt(review.getCreatedAt().toString());
        dto.setCreatedAtFormatted(formatTimeAgo(review.getCreatedAt()));
        dto.setRestaurantReply(review.getRestaurantReply());
        dto.setRepliedAt(review.getRepliedAt() != null ? review.getRepliedAt().toString() : null);
        dto.setRepliedAtFormatted(review.getRepliedAt() != null ? formatTimeAgo(review.getRepliedAt()) : null);
        dto.setHasReply(review.getRestaurantReply() != null && !review.getRestaurantReply().isEmpty());
        dto.setHelpfulCount(review.getHelpfulCount());
        
        // Parse JSON image URLs
        try {
            if (review.getImageUrlJson() != null && !review.getImageUrlJson().isEmpty()) {
                List<String> imageUrls = objectMapper.readValue(
                        review.getImageUrlJson(),
                        new TypeReference<List<String>>(){}
                );
                dto.setImageUrls(imageUrls);
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON images: " + e.getMessage());
        }
        
        return dto;
    }
    
    /**
     * Phản hồi review (Shop Reply)
     */
    public ReviewDTO replyToReview(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review không tồn tại"));
        
        // Lọc từ ngữ thô tục trong phản hồi
        String filteredReply = profanityFilterService.filterProfanity(reply);
        
        review.setRestaurantReply(filteredReply);
        review.setRepliedAt(LocalDateTime.now());
        
        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }
    
    /**
     * Lấy thống kê review
     */
    public Map<String, Object> getReviewStatistics(RestaurantProfile restaurant) {
        Long totalReviews = reviewRepository.count();
        Long unrepliedReviews = reviewRepository.countUnrepliedReviews(restaurant.getId());
        Long reviewsWithImages = reviewRepository.countReviewsWithImages(restaurant.getId());
        Double avgRating = reviewRepository.getAverageRating(restaurant.getId());
        
        List<Object[]> ratingDistribution = reviewRepository.getRatingDistribution(restaurant.getId());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReviews", totalReviews);
        stats.put("unrepliedReviews", unrepliedReviews);
        stats.put("repliedReviews", totalReviews - unrepliedReviews);
        stats.put("reviewsWithImages", reviewsWithImages);
        stats.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        stats.put("ratingDistribution", ratingDistribution);
        
        return stats;
    }
    
    /**
     * Format thời gian thành "X ngày trước", "X giờ trước", etc.
     */
    private String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long secondsDiff = java.time.temporal.ChronoUnit.SECONDS.between(dateTime, now);
        
        if (secondsDiff < 60) return "Vừa xong";
        if (secondsDiff < 3600) return (secondsDiff / 60) + " phút trước";
        if (secondsDiff < 86400) return (secondsDiff / 3600) + " giờ trước";
        if (secondsDiff < 2592000) return (secondsDiff / 86400) + " ngày trước"; // 30 days
        if (secondsDiff < 31536000) return (secondsDiff / 2592000) + " tháng trước"; // 365 days
        return (secondsDiff / 31536000) + " năm trước";
    }
}
