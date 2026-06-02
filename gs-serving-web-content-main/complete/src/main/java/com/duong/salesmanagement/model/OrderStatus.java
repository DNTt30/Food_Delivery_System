package com.duong.salesmanagement.model;

public enum OrderStatus {
    /** Đơn online chưa thanh toán — chưa hiển thị cho nhà hàng / lịch sử khách */
    AWAITING_PAYMENT,
    PENDING,
    PREPARING,
    DELIVERING,
    COMPLETED,
    CANCELLED
}
