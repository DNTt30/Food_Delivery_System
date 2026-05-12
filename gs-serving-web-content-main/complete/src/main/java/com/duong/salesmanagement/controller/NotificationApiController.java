package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.UserRepository;
import com.duong.salesmanagement.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API cho hệ thống Notification.
 *
 * <pre>
 * GET  /api/notifications                → lấy danh sách (tối đa 50)
 * GET  /api/notifications/unread-count   → đếm chưa đọc
 * PUT  /api/notifications/{id}/read      → đánh dấu 1 notification đã đọc
 * PUT  /api/notifications/read-all       → đánh dấu tất cả đã đọc
 * </pre>
 *
 * Bảo mật: JWT required. Chỉ xem được notification của chính mình.
 */
@RestController
@RequestMapping("/api/notifications")
@SuppressWarnings("null")
public class NotificationApiController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationApiController(NotificationService notificationService,
                                     UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // ── Auth helper ───────────────────────────────────────────────────────

    private User getUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
    }

    // ═════════════════════════════════════════════════════════════════════
    // GET /api/notifications
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách thông báo của user đang đăng nhập (tối đa 50, mới nhất trước).
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication auth) {
        User user = getUser(auth);
        if (user == null) return unauthorized();

        List<Notification> notifications = notificationService.getNotificationsByUser(user);
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ═════════════════════════════════════════════════════════════════════
    // GET /api/notifications/unread-count
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Trả về số thông báo chưa đọc — dùng cho badge trên bell icon.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication auth) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.ok(Map.of("count", 0));
        long count = notificationService.countUnread(user);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // ═════════════════════════════════════════════════════════════════════
    // PUT /api/notifications/{id}/read
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Đánh dấu một notification là đã đọc.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(Authentication auth, @PathVariable Long id) {
        User user = getUser(auth);
        if (user == null) return unauthorized();

        boolean success = notificationService.markAsRead(id, user);
        if (!success) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ═════════════════════════════════════════════════════════════════════
    // PUT /api/notifications/read-all
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Đánh dấu tất cả notification của user là đã đọc.
     */
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        User user = getUser(auth);
        if (user == null) return unauthorized();
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ═════════════════════════════════════════════════════════════════════
    // DTO + MAPPING
    // ═════════════════════════════════════════════════════════════════════

    private NotificationDTO toDTO(Notification n) {
        return new NotificationDTO(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType() != null ? n.getType().name() : null,
                n.getRelatedOrderId(),
                n.isRead(),
                n.getCreatedAt() != null ? n.getCreatedAt().toString() : null,
                formatTimeAgo(n.getCreatedAt()),
                getActionUrl(n),
                getTypeIcon(n.getType()),
                getTypeColor(n.getType())
        );
    }

    /**
     * Đường dẫn chuyển trang khi click vào notification.
     * Role-aware: Customer → /customer/tracking?orderId=X
     *             Restaurant → /restaurant/orders
     *             Driver → /driver/delivering
     */
    private String getActionUrl(Notification n) {
        if (n.getRelatedOrderId() == null) return null;
        String role = n.getUser().getRole().name();
        return switch (role) {
            case "CUSTOMER"   -> "/customer/tracking?orderId=" + n.getRelatedOrderId();
            case "RESTAURANT" -> "/restaurant/orders";
            case "DRIVER"     -> "/driver/delivering";
            default           -> null;
        };
    }

    /** Bootstrap icon class tương ứng với từng loại notification */
    private String getTypeIcon(NotificationType type) {
        if (type == null) return "bi-bell";
        return switch (type) {
            case ORDER_CREATED, ORDER_ACCEPTED, ORDER_PREPARING -> "bi-bag-check-fill";
            case DRIVER_ASSIGNED, ORDER_DELIVERING              -> "bi-bicycle";
            case ORDER_COMPLETED                                -> "bi-check-circle-fill";
            case ORDER_CANCELLED                                -> "bi-x-circle-fill";
            case PAYMENT_SUCCESS                                -> "bi-credit-card-fill";
            case PAYMENT_FAILED                                 -> "bi-exclamation-triangle-fill";
            case NEW_REVIEW                                     -> "bi-star-fill";
            case NEW_PROMOTION                                  -> "bi-gift-fill";
            case SYSTEM_ALERT                                   -> "bi-shield-exclamation";
        };
    }

    /** Bootstrap text-color class */
    private String getTypeColor(NotificationType type) {
        if (type == null) return "text-secondary";
        return switch (type) {
            case ORDER_CREATED, ORDER_ACCEPTED, ORDER_PREPARING -> "text-primary";
            case DRIVER_ASSIGNED, ORDER_DELIVERING              -> "text-success";
            case ORDER_COMPLETED                                -> "text-success";
            case ORDER_CANCELLED                                -> "text-danger";
            case PAYMENT_SUCCESS                                -> "text-success";
            case PAYMENT_FAILED                                 -> "text-danger";
            case NEW_REVIEW                                     -> "text-warning";
            case NEW_PROMOTION                                  -> "text-info";
            case SYSTEM_ALERT                                   -> "text-warning";
        };
    }

    /** "2 phút trước", "1 giờ trước", v.v. */
    private String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "";
        long mins  = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (mins < 1)   return "Vừa xong";
        if (mins < 60)  return mins + " phút trước";
        long hrs   = ChronoUnit.HOURS.between(time, LocalDateTime.now());
        if (hrs  < 24)  return hrs  + " giờ trước";
        long days  = ChronoUnit.DAYS.between(time, LocalDateTime.now());
        if (days < 7)   return days + " ngày trước";
        return time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // ─── DTO ──────────────────────────────────────────────────────────────

    public static class NotificationDTO {
        public Long   id;
        public String title;
        public String message;
        public String type;
        public Long   relatedOrderId;
        public boolean read;
        public String createdAt;
        public String timeAgo;
        public String actionUrl;
        public String icon;
        public String color;

        public NotificationDTO(Long id, String title, String message, String type,
                               Long relatedOrderId, boolean read, String createdAt,
                               String timeAgo, String actionUrl, String icon, String color) {
            this.id = id; this.title = title; this.message = message; this.type = type;
            this.relatedOrderId = relatedOrderId; this.read = read;
            this.createdAt = createdAt; this.timeAgo = timeAgo;
            this.actionUrl = actionUrl; this.icon = icon; this.color = color;
        }
    }
}
