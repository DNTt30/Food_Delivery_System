package com.duong.salesmanagement.service;

import java.time.LocalDateTime;

/**
 * IShippingCalculationService — Interface tầng Service cho nghiệp vụ Tính phí ship.
 *
 * Nguyên lý SOLID áp dụng:
 *  - D (Dependency Inversion): OrderService phụ thuộc vào interface này,
 *    không phụ thuộc vào lớp ShippingCalculationService cụ thể.
 *  - O (Open/Closed): Có thể thêm implementation mới (vd: ExpressShippingStrategy)
 *    mà không cần sửa code cũ trong OrderService.
 */
public interface IShippingCalculationService {

    /**
     * Tính phí giao hàng dựa trên khoảng cách.
     * Business rule:
     *   - 3km đầu: 15.000đ cố định
     *   - Mỗi 1km tiếp theo: +5.000đ
     *   - Tối đa: 75.000đ
     *
     * @param distanceKm khoảng cách tính bằng km
     * @return phí giao hàng tính bằng VNĐ
     */
    double calculateShippingFee(double distanceKm);

    /**
     * Ước tính thời gian giao hàng (Estimated Time of Arrival).
     * Business rule: 15p chuẩn bị + 2p/km + 5p dự phòng.
     *
     * @param distanceKm khoảng cách tính bằng km
     * @return thời điểm dự kiến giao đến nơi
     */
    LocalDateTime estimateETA(double distanceKm);

    /**
     * Tính khoảng cách giữa 2 điểm tọa độ GPS trên bề mặt Trái Đất.
     * Sử dụng thuật toán Haversine:
     *   d = 2r · arcsin(√(sin²(Δφ/2) + cos(φ₁)·cos(φ₂)·sin²(Δλ/2)))
     *
     * @param lat1 vĩ độ điểm 1 (nhà hàng)
     * @param lon1 kinh độ điểm 1 (nhà hàng)
     * @param lat2 vĩ độ điểm 2 (khách hàng)
     * @param lon2 kinh độ điểm 2 (khách hàng)
     * @return khoảng cách tính bằng km
     */
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);
}
