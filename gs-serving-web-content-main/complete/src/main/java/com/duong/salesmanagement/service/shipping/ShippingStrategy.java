package com.duong.salesmanagement.service.shipping;

/**
 * ═══════════════════════════════════════════════════════════════════
 * STRATEGY PATTERN — Interface cho các chiến lược tính phí giao hàng
 * ═══════════════════════════════════════════════════════════════════
 *
 * VẤN ĐỀ TRƯỚC KHI DÙNG PATTERN:
 *   Nếu viết thẳng logic tính phí ship vào OrderService, khi cần thêm
 *   loại giao hàng mới (nhanh/chậm/miễn phí) thì phải sửa trực tiếp
 *   vào code cũ → vi phạm nguyên tắc Open/Closed.
 *
 * GIẢI PHÁP — Strategy Pattern:
 *   Tách riêng mỗi thuật toán tính phí thành một class riêng biệt,
 *   tất cả đều implement cùng interface ShippingStrategy.
 *   OrderService chỉ cần gọi strategy.calculate(distance) mà không
 *   cần biết đang dùng thuật toán nào bên trong.
 *
 * SƠ ĐỒ UML:
 *   ┌─────────────────────────────────┐
 *   │      «interface»                │
 *   │      ShippingStrategy           │
 *   │─────────────────────────────────│
 *   │ + calculate(distance): double   │
 *   │ + getName(): String             │
 *   └──────────────┬──────────────────┘
 *                  │ implements
 *        ┌─────────┼───────────┐
 *        ▼         ▼           ▼
 *  StandardShipping  ExpressShipping  FreeShipping
 *  Strategy          Strategy         Strategy
 */
public interface ShippingStrategy {

    /**
     * Tính phí giao hàng theo chiến lược cụ thể.
     *
     * @param distanceKm khoảng cách từ nhà hàng đến khách (km)
     * @return phí giao hàng tính bằng VNĐ
     */
    double calculate(double distanceKm);

    /**
     * Tên của chiến lược (dùng để hiển thị trên UI hoặc ghi log).
     */
    String getName();
}
