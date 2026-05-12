package com.duong.salesmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity thông báo hệ thống.
 *
 * <p>Hibernate sẽ tự tạo bảng {@code notifications} theo schema:
 * <pre>
 * CREATE TABLE notifications (
 *     id              BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     user_id         BIGINT NOT NULL,
 *     title           VARCHAR(255),
 *     message         TEXT,
 *     type            VARCHAR(50),
 *     related_order_id BIGINT,
 *     is_read         BOOLEAN DEFAULT FALSE,
 *     created_at      DATETIME
 * );
 * </pre>
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_user_id",   columnList = "user_id"),
        @Index(name = "idx_notif_is_read",   columnList = "is_read"),
        @Index(name = "idx_notif_created_at", columnList = "created_at")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Người nhận thông báo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Tiêu đề ngắn — hiển thị in đậm */
    @Column(length = 255)
    private String title;

    /** Nội dung chi tiết */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** Loại thông báo — quyết định icon, màu sắc */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private NotificationType type;

    /** ID đơn hàng liên quan (nullable nếu là notification hệ thống) */
    @Column(name = "related_order_id")
    private Long relatedOrderId;

    /** Trạng thái đã đọc / chưa đọc */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructors ───────────────────────────────────────────────────────

    public Notification() {}

    public Notification(User user, String title, String message,
                        NotificationType type, Long relatedOrderId) {
        this.user           = user;
        this.title          = title;
        this.message        = message;
        this.type           = type;
        this.relatedOrderId = relatedOrderId;
        this.read           = false;
        this.createdAt      = LocalDateTime.now();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public User getUser()                          { return user; }
    public void setUser(User user)                 { this.user = user; }

    public String getTitle()                       { return title; }
    public void setTitle(String title)             { this.title = title; }

    public String getMessage()                     { return message; }
    public void setMessage(String message)         { this.message = message; }

    public NotificationType getType()              { return type; }
    public void setType(NotificationType type)     { this.type = type; }

    public Long getRelatedOrderId()                { return relatedOrderId; }
    public void setRelatedOrderId(Long id)         { this.relatedOrderId = id; }

    public boolean isRead()                        { return read; }
    public void setRead(boolean read)              { this.read = read; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime t)      { this.createdAt = t; }
}
