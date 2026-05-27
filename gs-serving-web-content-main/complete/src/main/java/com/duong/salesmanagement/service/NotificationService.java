package com.duong.salesmanagement.service;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service quản lý thông báo hệ thống.
 *
 * <h3>Smart Notification Rules:</h3>
 * <ul>
 *   <li>Không gửi trùng: kiểm tra {@code existsByUserAndTypeAndRelatedOrderIdAndReadFalse}</li>
 *   <li>Giới hạn tối đa 50 thông báo mới nhất</li>
 *   <li>Auto mark-read khi user click (qua API PUT /api/notifications/{id}/read)</li>
 * </ul>
 */
@Service
public class NotificationService {

    private static final int MAX_NOTIFICATIONS = 50;

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CREATE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Tạo notification cho một user cụ thể.
     * Smart dedup: bỏ qua nếu đã có notification chưa đọc cùng type + orderId.
     */
    @Transactional
    public Notification createNotification(User user, String title, String message,
                                           NotificationType type, Long relatedOrderId) {
        if (user == null) return null;
        // Chống spam: không tạo trùng
        if (relatedOrderId != null &&
                notificationRepository.existsByUserAndTypeAndRelatedOrderIdAndReadFalse(
                        user, type, relatedOrderId)) {
            return null;
        }

        Notification notification = new Notification(user, title, message, type, relatedOrderId);
        return notificationRepository.save(notification);
    }

    /**
     * Lưu trực tiếp một Notification entity (bypass dedup).
     * Dùng cho tin nhắn chat — mỗi tin là một notification riêng biệt.
     */
    @Transactional
    public Notification save(Notification notification) {
        if (notification == null) return null;
        return notificationRepository.save(notification);
    }

    /**
     * Tạo notification hệ thống (không gắn với đơn hàng cụ thể).
     */
    @Transactional
    public Notification createSystemNotification(User user, String title, String message) {
        return createNotification(user, title, message, NotificationType.SYSTEM_ALERT, null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // QUERY
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Lấy tối đa 50 thông báo mới nhất của user.
     */
    public List<Notification> getNotificationsByUser(User user) {
        if (user == null) return List.of();
        return notificationRepository.findByUserOrderByCreatedAtDesc(
                user, PageRequest.of(0, MAX_NOTIFICATIONS));
    }

    /**
     * Đếm số thông báo chưa đọc.
     */
    public long countUnread(User user) {
        if (user == null) return 0;
        return notificationRepository.countByUserAndReadFalse(user);
    }

    // ════════════════════════════════════════════════════════════════════════
    // MARK READ
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Đánh dấu một thông báo là đã đọc.
     * Kiểm tra ownership để tránh user khác mark read.
     */
    @Transactional
    public boolean markAsRead(Long notificationId, User user) {
        if (notificationId == null || user == null) return false;
        Notification n = notificationRepository.findById(notificationId).orElse(null);
        if (n == null || n.getUser() == null || !n.getUser().getId().equals(user.getId())) return false;
        n.setRead(true);
        notificationRepository.save(n);
        return true;
    }

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc.
     */
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllReadByUser(user);
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONVENIENCE FACTORY METHODS — gọi từ OrderService, ReviewService...
    // ════════════════════════════════════════════════════════════════════════

    /** Gửi thông báo khi đơn hàng được tạo (cho Customer) */
    @Transactional
    public void notifyOrderCreated(User customer, Long orderId, String restaurantName) {
        createNotification(customer,
                "🍔 Đặt hàng thành công!",
                "Đơn #" + orderId + " tại " + restaurantName + " đang chờ nhà hàng xác nhận.",
                NotificationType.ORDER_CREATED, orderId);
    }

    /** Gửi thông báo khi nhà hàng xác nhận đơn (cho Customer + Restaurant) */
    @Transactional
    public void notifyOrderAccepted(User customer, User restaurant, Long orderId) {
        createNotification(customer,
                "✅ Nhà hàng đã xác nhận!",
                "Đơn #" + orderId + " đã được xác nhận và đang chuẩn bị.",
                NotificationType.ORDER_ACCEPTED, orderId);
    }

    /** Gửi thông báo khi nhà hàng từ chối đơn */
    @Transactional
    public void notifyOrderCancelledByRestaurant(User customer, Long orderId) {
        createNotification(customer,
                "❌ Đơn hàng bị hủy",
                "Đơn #" + orderId + " đã bị nhà hàng hủy.",
                NotificationType.ORDER_CANCELLED, orderId);
    }

    /** Gửi thông báo khi tài xế nhận đơn (cho Customer và Restaurant) */
    @Transactional
    public void notifyDriverAssigned(User customer, User restaurantUser,
                                     Long orderId, String driverName) {
        String msg = "Tài xế " + driverName + " đã nhận đơn #" + orderId + " và đang trên đường.";
        createNotification(customer,
                "🛵 Tài xế đang đến!",
                msg, NotificationType.DRIVER_ASSIGNED, orderId);

        if (restaurantUser != null) {
            createNotification(restaurantUser,
                    "🛵 Tài xế đến lấy đơn",
                    "Tài xế " + driverName + " đã đến nhận đơn #" + orderId + ".",
                    NotificationType.DRIVER_ASSIGNED, orderId);
        }
    }

    /** Gửi thông báo khi giao hàng thành công */
    @Transactional
    public void notifyOrderCompleted(User customer, Long orderId) {
        createNotification(customer,
                "🎉 Giao hàng thành công!",
                "Đơn #" + orderId + " đã được giao. Chúc bạn ngon miệng! Hãy để lại đánh giá nhé.",
                NotificationType.ORDER_COMPLETED, orderId);
    }

    /** Gửi thông báo khi customer hủy đơn */
    @Transactional
    public void notifyOrderCancelledByCustomer(User restaurantUser, Long orderId) {
        createNotification(restaurantUser,
                "⚠️ Khách hàng đã hủy đơn",
                "Đơn #" + orderId + " vừa bị khách hàng hủy.",
                NotificationType.ORDER_CANCELLED, orderId);
    }

    /** Gửi thông báo khi có đơn mới (cho Restaurant) */
    @Transactional
    public void notifyNewOrderForRestaurant(User restaurantUser, Long orderId, String customerName) {
        createNotification(restaurantUser,
                "🔔 Có đơn hàng mới!",
                "Khách hàng " + customerName + " vừa đặt đơn #" + orderId + ". Vui lòng xác nhận!",
                NotificationType.ORDER_CREATED, orderId);
    }

    /** Gửi thông báo khi có đánh giá mới (cho Restaurant) */
    @Transactional
    public void notifyNewReview(User restaurantUser, Long orderId, int rating) {
        String stars = "⭐".repeat(rating);
        createNotification(restaurantUser,
                "⭐ Đánh giá mới!",
                "Bạn vừa nhận được đánh giá " + stars + " từ đơn #" + orderId + ".",
                NotificationType.NEW_REVIEW, orderId);
    }

    /** Gửi thông báo cho Driver khi có đơn mới cần nhận */
    @Transactional
    public void notifyNewOrderForDriver(User driverUser, Long orderId, String restaurantName) {
        createNotification(driverUser,
                "📦 Có đơn cần giao!",
                "Đơn #" + orderId + " từ " + restaurantName + " đang chờ tài xế nhận.",
                NotificationType.ORDER_CREATED, orderId);
    }

    /** Gửi thông báo giao hàng thành công (cho Driver) */
    @Transactional
    public void notifyDeliveryCompletedForDriver(User driverUser, Long orderId, double earnings) {
        createNotification(driverUser,
                "💰 Giao thành công!",
                String.format("Bạn đã giao thành công đơn #%d. Thu nhập: +%.0f₫", orderId, earnings),
                NotificationType.ORDER_COMPLETED, orderId);
    }

    /** Gửi thông báo khi nhà hàng phản hồi đánh giá (cho Customer) */
    @Transactional
    public void notifyRestaurantReplied(User customerUser, Long orderId) {
        createNotification(customerUser,
                "💬 Phản hồi đánh giá mới!",
                "Nhà hàng đã phản hồi đánh giá của bạn cho đơn #" + orderId + ".",
                NotificationType.NEW_REVIEW, orderId);
    }
}
