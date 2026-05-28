package com.fooddelivery.service;

import com.fooddelivery.entity.User;
import com.fooddelivery.entity.Notification;
import com.fooddelivery.enums.NotificationType;
import com.fooddelivery.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notifyRestaurantReplied(User customerUser, Long orderId) {
        // Tạo thông báo mới thời gian thực đến khách hàng khi nhà hàng gửi phản hồi
        createNotification(customerUser, 
                "💬 Phản hồi đánh giá mới!", 
                "Nhà hàng đã phản hồi đánh giá của bạn cho đơn #" + orderId + ".", 
                NotificationType.NEW_REVIEW, 
                orderId);
    }

    private void createNotification(User user, String title, String content, NotificationType type, Long orderId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .associationId(orderId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        
        // (Tùy chọn) Gửi qua WebSocket STOMP tới topic "/topic/notifications/{userId}"
    }
}
