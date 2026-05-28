package com.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private int rating; // 1 to 5 stars
    
    @Column(columnDefinition = "TEXT")
    private String comment;

    // ============================================
    // BỔ SUNG 3 TRƯỜNG MỚI THEO YÊU CẦU:
    // ============================================
    
    @Column(name = "image_url")
    private String imageUrl;        // URL ảnh đánh giá của khách hàng

    @Column(name = "restaurant_reply", columnDefinition = "TEXT")
    private String restaurantReply; // Nội dung phản hồi của nhà hàng

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;// Thời gian nhà hàng phản hồi
}
