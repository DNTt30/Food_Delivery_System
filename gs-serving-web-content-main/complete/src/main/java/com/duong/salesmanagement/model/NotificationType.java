package com.duong.salesmanagement.model;

/**
 * Loại thông báo hệ thống — dùng để quyết định icon, màu sắc,
 * và đường dẫn liên quan khi người dùng nhấn vào notification.
 */
public enum NotificationType {
    // ── Order lifecycle ──────────────────────────────────────────
    ORDER_CREATED,
    ORDER_ACCEPTED,
    ORDER_PREPARING,
    DRIVER_ASSIGNED,
    ORDER_DELIVERING,
    ORDER_COMPLETED,
    ORDER_CANCELLED,

    // ── Payment ───────────────────────────────────────────────────
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,

    // ── Review ────────────────────────────────────────────────────
    NEW_REVIEW,

    // ── Promotion ─────────────────────────────────────────────────
    NEW_PROMOTION,

    // ── System / Admin ────────────────────────────────────────────
    SYSTEM_ALERT
}
