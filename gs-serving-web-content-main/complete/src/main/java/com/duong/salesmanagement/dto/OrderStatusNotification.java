package com.duong.salesmanagement.dto;

import java.time.LocalDateTime;

/**
 * Payload broadcast qua WebSocket khi trạng thái đơn hàng thay đổi.
 *
 * <pre>
 * Topic: /topic/order-status.{orderId}
 * </pre>
 *
 * Frontend (tracking.html) subscribe topic này để nhận cập nhật tức thời
 * mà không cần polling.
 */
public class OrderStatusNotification {

    private Long          orderId;
    private String        status;       // "PENDING" | "PREPARING" | "DELIVERING" | "COMPLETED" | "CANCELLED"
    private String        driverName;
    private String        driverPhone;
    private LocalDateTime updatedAt;

    public OrderStatusNotification() {}

    public OrderStatusNotification(Long orderId, String status,
                                   String driverName, String driverPhone,
                                   LocalDateTime updatedAt) {
        this.orderId     = orderId;
        this.status      = status;
        this.driverName  = driverName;
        this.driverPhone = driverPhone;
        this.updatedAt   = updatedAt;
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public Long getOrderId()                    { return orderId; }
    public void setOrderId(Long orderId)        { this.orderId = orderId; }

    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }

    public String getDriverName()               { return driverName; }
    public void setDriverName(String driverName){ this.driverName = driverName; }

    public String getDriverPhone()                { return driverPhone; }
    public void setDriverPhone(String driverPhone){ this.driverPhone = driverPhone; }

    public LocalDateTime getUpdatedAt()                  { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)    { this.updatedAt = updatedAt; }
}
