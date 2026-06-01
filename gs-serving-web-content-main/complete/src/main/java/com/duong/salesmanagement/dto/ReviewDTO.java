package com.duong.salesmanagement.dto;

import java.util.List;

/**
 * DTO cho Review (Đánh giá) của khách hàng
 * Hỗ trợ đánh giá kèm ảnh, phản hồi từ nhà hàng, lọc từ ngữ thô tục
 */
public class ReviewDTO {
    public Long id;
    public Long orderId;
    public String customerName;
    public String customerAvatar;  // URL avatar khách hàng
    public Integer rating;          // 1-5 sao
    public String comment;          // Bình luận (đã lọc từ ngữ thô tục)
    public String originalComment;  // Bình luận gốc (chưa lọc)
    public Boolean hasInappropriateWords; // Cảnh báo nếu có từ thô tục
    public List<String> imageUrls;  // Danh sách ảnh (tối đa 3)
    public String createdAt;
    public String createdAtFormatted; // "2 ngày trước"
    
    // Shop Reply
    public String restaurantReply;   // Phản hồi từ nhà hàng
    public String repliedAt;
    public String repliedAtFormatted; // "1 ngày trước"
    public Boolean hasReply;         // Đã phản hồi chưa
    
    // Statistics
    public Integer helpfulCount;     // Số người vote "hữu ích"
    public Boolean isHelpfulVotedByMe; // Khách hàng hiện tại đã vote chưa
    
    public ReviewDTO() {}
    
    public ReviewDTO(Long id, Long orderId, String customerName, String customerAvatar,
                     Integer rating, String comment, String originalComment,
                     List<String> imageUrls, String createdAt, String restaurantReply, String repliedAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerAvatar = customerAvatar;
        this.rating = rating;
        this.comment = comment;
        this.originalComment = originalComment;
        this.imageUrls = imageUrls;
        this.createdAt = createdAt;
        this.restaurantReply = restaurantReply;
        this.repliedAt = repliedAt;
        this.hasReply = restaurantReply != null && !restaurantReply.isEmpty();
    }
    
    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public String getRestaurantReply() { return restaurantReply; }
    public void setRestaurantReply(String restaurantReply) { this.restaurantReply = restaurantReply; }
    public Boolean getHasReply() { return hasReply; }
    public void setHasReply(Boolean hasReply) { this.hasReply = hasReply; }
}
