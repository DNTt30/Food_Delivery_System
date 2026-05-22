package com.duong.salesmanagement.service.shipping;

import org.springframework.stereotype.Component;

/**
 * ═══════════════════════════════════════════════════════════════════
 * ConcreteStrategy 3: Miễn phí giao hàng (Free Shipping)
 * ═══════════════════════════════════════════════════════════════════
 *
 * Business rule: Dùng cho các chương trình khuyến mãi, voucher
 * miễn phí ship, hoặc đơn hàng đặc biệt do Admin cấu hình.
 */
@Component("freeShipping")
public class FreeShippingStrategy implements ShippingStrategy {

    @Override
    public double calculate(double distanceKm) {
        return 0.0; // Miễn phí hoàn toàn
    }

    @Override
    public String getName() {
        return "Miễn phí giao hàng (Free Shipping)";
    }
}
