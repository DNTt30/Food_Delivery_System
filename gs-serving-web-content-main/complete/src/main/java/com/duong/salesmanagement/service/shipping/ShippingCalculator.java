package com.duong.salesmanagement.service.shipping;

import org.springframework.stereotype.Component;

/**
 * ═══════════════════════════════════════════════════════════════════
 * Context class — ShippingCalculator (dùng Strategy Pattern)
 * ═══════════════════════════════════════════════════════════════════
 *
 * Đây là lớp "Context" trong Strategy Pattern.
 * Nó giữ một tham chiếu đến ShippingStrategy hiện tại,
 * và cho phép thay đổi chiến lược tính phí khi runtime.
 *
 * CÁCH DÙNG TRONG CODE NGHIỆP VỤ (OrderService):
 *
 *   // 1. Tạo Context với chiến lược tiêu chuẩn (mặc định)
 *   ShippingCalculator calculator = new ShippingCalculator(new StandardShippingStrategy());
 *   double fee = calculator.calculate(5.3); // → 30.000đ
 *
 *   // 2. Đổi sang giao hàng nhanh khi runtime (không cần sửa code cũ!)
 *   calculator.setStrategy(new ExpressShippingStrategy());
 *   double expressFee = calculator.calculate(5.3); // → 45.000đ
 *
 *   // 3. Dùng voucher miễn phí ship
 *   calculator.setStrategy(new FreeShippingStrategy());
 *   double freeFee = calculator.calculate(5.3); // → 0đ
 */
@Component
public class ShippingCalculator {

    private ShippingStrategy strategy;

    public ShippingCalculator(ShippingStrategy strategy) {
        this.strategy = strategy;
    }

    /** Thay đổi chiến lược tính phí ship khi runtime */
    public void setStrategy(ShippingStrategy strategy) {
        this.strategy = strategy;
    }

    /** Tính phí ship theo chiến lược hiện tại */
    public double calculate(double distanceKm) {
        return strategy.calculate(distanceKm);
    }

    /** Lấy tên chiến lược đang áp dụng (để ghi log hoặc hiển thị UI) */
    public String getCurrentStrategyName() {
        return strategy.getName();
    }
}
