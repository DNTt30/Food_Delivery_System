package com.fooddelivery.service;

import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.CustomerProfile;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.ReviewRepository;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public Review reviewOrder(Long orderId, CustomerProfile customer, int rating, String comment, String imageUrl) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        // Kiểm tra quyền sở hữu đơn hàng (CustomerProfile)
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("Bạn không thể đánh giá đơn hàng của người khác");
        }

        // Tạo đối tượng đánh giá mới kèm đường dẫn ảnh gửi về từ Client
        Review review = Review.builder()
                .order(order)
                .rating(rating)
                .comment(comment)
                .imageUrl(imageUrl) // Cập nhật: Lưu link ảnh đánh ảnh chụp
                .build();

        review = reviewRepository.save(review);
        
        // Gắn review vào order và cập nhật trạng thái
        order.setReview(review);
        orderRepository.save(order);

        return review;
    }
}
