package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.Notification;
import com.duong.salesmanagement.model.NotificationType;
import com.duong.salesmanagement.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 50 thông báo mới nhất của user, sắp xếp DESC */
    List<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /** Đếm số thông báo chưa đọc */
    long countByUserAndReadFalse(User user);

    /** Tìm chưa đọc của user, sắp xếp mới nhất */
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    /** Xóa tất cả thông báo của user */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user")
    void deleteByUser(@Param("user") User user);

    /** Đánh dấu tất cả là đã đọc */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user AND n.read = false")
    void markAllReadByUser(@Param("user") User user);

    /** Kiểm tra notification trùng (chống spam) */
    boolean existsByUserAndTypeAndRelatedOrderIdAndReadFalse(
            User user, NotificationType type, Long relatedOrderId);

    @Modifying
    @Query("UPDATE Notification n SET n.title = :title, n.message = :message WHERE n.broadcastLogId = :broadcastId")
    void updateByBroadcastLogId(@Param("broadcastId") Long broadcastId, @Param("title") String title, @Param("message") String message);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.broadcastLogId = :broadcastId")
    void deleteByBroadcastLogId(@Param("broadcastId") Long broadcastId);
}
