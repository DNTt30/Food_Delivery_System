package com.duong.salesmanagement.service.shipping;

import org.springframework.stereotype.Component;

/**
 * ═══════════════════════════════════════════════════════════════════
 * ConcreteStrategy 2: Phí giao hàng nhanh (Express Shipping)
 * ═══════════════════════════════════════════════════════════════════
 *
 * Business rule (nghiệp vụ):
 *   - Ưu tiên giao trước, phí cao hơn Standard 1.5 lần.
 *   - Tối thiểu: 25.000đ
 *   - Tối đa: 120.000đ
 *
 * Ví dụ:
 *   - 2km    → 25.000đ (bằng tối thiểu)
 *   - 5km    → StandardFee(5km)*1.5 = 30.000đ * 1.5 = 45.000đ
 */
@Component("expressShipping")
public class ExpressShippingStrategy implements ShippingStrategy {

    private static final double MULTIPLIER = 1.5;
    private static final double MIN_FEE    = 25_000.0;
    private static final double MAX_FEE    = 120_000.0;

    // Dùng lại logic tính base fee của Standard
    private final StandardShippingStrategy standard = new StandardShippingStrategy();

    @Override
    public double calculate(double distanceKm) {
        double baseFee = standard.calculate(distanceKm);
        double expressFee = baseFee * MULTIPLIER;
        expressFee = Math.max(expressFee, MIN_FEE);
        return Math.min(expressFee, MAX_FEE);
    }

    @Override
    public String getName() {
        return "Giao hàng nhanh (x1.5 phí tiêu chuẩn, tối thiểu 25k)";
    }
}
