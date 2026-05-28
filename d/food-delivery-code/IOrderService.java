package com.fooddelivery.service;

import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.CustomerProfile;

public interface IOrderService {
    // Cập nhật: Thêm tham số String imageUrl ở cuối để lưu ảnh đánh giá
    Review reviewOrder(Long orderId, CustomerProfile customer, int rating, String comment, String imageUrl);
}
