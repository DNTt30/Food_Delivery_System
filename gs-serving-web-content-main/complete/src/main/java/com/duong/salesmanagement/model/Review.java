package com.duong.salesmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity cho Review (Đánh giá) của khách hàng
 * Hỗ trợ đánh giá kèm ảnh, phản hồi từ nhà hàng
 */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private FoodOrder order;  // Liên kết tới đơn hàng

    @Column(nullable = false)
    private Integer rating;   // 1-5 sao

    @Column(columnDefinition = "TEXT")
    private String comment;   // Bình luận (đã lọc)

    @Column(columnDefinition = "TEXT")
    private String originalComment; // Bình luận gốc (chưa lọc)

    @Column(name = "has_inappropriate_words")
    private Boolean hasInappropriateWords = false; // Cảnh báo từ thô tục

    @Column(name = "image_url")
    private String imageUrl;  // URL ảnh (có thể JSON array hoặc comma-separated)

    @Column(columnDefinition = "TEXT")
    private String imageUrlJson; // JSON array URLs ảnh

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "restaurant_reply", columnDefinition = "TEXT")
    private String restaurantReply; // Phản hồi từ nhà hàng

    @Column(name = "replied_at")
    private LocalDateTime repliedAt; // Thời gian phản hồi

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0; // Số người vote "hữu ích"

    @Column(name = "is_verified_purchase")
    private Boolean isVerifiedPurchase = true; // Xác nhận mua hàng

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FoodOrder getOrder() { return order; }
    public void setOrder(FoodOrder order) { this.order = order; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getOriginalComment() { return originalComment; }
    public void setOriginalComment(String originalComment) { this.originalComment = originalComment; }
    public Boolean getHasInappropriateWords() { return hasInappropriateWords; }
    public void setHasInappropriateWords(Boolean hasInappropriateWords) { this.hasInappropriateWords = hasInappropriateWords; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImageUrlJson() { return imageUrlJson; }
    public void setImageUrlJson(String imageUrlJson) { this.imageUrlJson = imageUrlJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getRestaurantReply() { return restaurantReply; }
    public void setRestaurantReply(String restaurantReply) { this.restaurantReply = restaurantReply; }
    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
    public Integer getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Integer helpfulCount) { this.helpfulCount = helpfulCount; }
    public Boolean getIsVerifiedPurchase() { return isVerifiedPurchase; }
    public void setIsVerifiedPurchase(Boolean isVerifiedPurchase) { this.isVerifiedPurchase = isVerifiedPurchase; }
}
