package com.duong.salesmanagement.service.shipping;

import org.springframework.stereotype.Component;

/**
 * ═══════════════════════════════════════════════════════════════════
 * ConcreteStrategy 1: Phí giao hàng tiêu chuẩn (Standard Shipping)
 * ═══════════════════════════════════════════════════════════════════
 *
 * Business rule (nghiệp vụ):
 *   - 3km đầu tiên: 15.000đ (cố định)
 *   - Mỗi km tiếp theo (làm tròn lên): +5.000đ
 *   - Tối đa: 75.000đ
 *
 * Ví dụ:
 *   - 1.5km  → 15.000đ (≤ 3km)
 *   - 3km    → 15.000đ (≤ 3km)
 *   - 4.2km  → 15.000đ + ceil(1.2)*5.000đ = 15.000đ + 10.000đ = 25.000đ
 *   - 20km   → 75.000đ (đã chạm tối đa)
 */
@Component("standardShipping")
public class StandardShippingStrategy implements ShippingStrategy {

    private static final double BASE_FEE     = 15_000.0;   // 15k cho 3km đầu
    private static final double EXTRA_KM_FEE = 5_000.0;    // +5k mỗi km tiếp
    private static final double MAX_FEE      = 75_000.0;   // tối đa 75k
    private static final int    BASE_KM      = 3;

    @Override
    public double calculate(double distanceKm) {
        if (distanceKm <= BASE_KM) {
            return BASE_FEE;
        }
        double extraKm = Math.ceil(distanceKm - BASE_KM);
        double fee = BASE_FEE + (extraKm * EXTRA_KM_FEE);
        return Math.min(fee, MAX_FEE);
    }

    @Override
    public String getName() {
        return "Giao hàng tiêu chuẩn (15k/3km + 5k/km)";
    }
}
