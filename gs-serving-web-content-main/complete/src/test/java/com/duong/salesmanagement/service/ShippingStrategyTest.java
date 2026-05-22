package com.duong.salesmanagement.service.shipping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test — Strategy Pattern (ShippingStrategy)
 * Kiểm tra từng ConcreteStrategy hoạt động đúng theo business rule.
 */
@DisplayName("Shipping Strategy Pattern Tests")
public class ShippingStrategyTest {

    private StandardShippingStrategy standard;
    private ExpressShippingStrategy  express;
    private FreeShippingStrategy     free;

    @BeforeEach
    void setUp() {
        standard = new StandardShippingStrategy();
        express  = new ExpressShippingStrategy();
        free     = new FreeShippingStrategy();
    }

    @Test
    @DisplayName("Standard: Khoảng cách ≤ 3km → phí cố định 15.000đ")
    public void standardShipping_withinBaseKm_returnBaseFee() {
        assertEquals(15_000.0, standard.calculate(2.0));
        assertEquals(15_000.0, standard.calculate(3.0));
    }

    @Test
    @DisplayName("Standard: 4.2km → 15k + ceil(1.2)*5k = 25.000đ")
    public void standardShipping_extraKm_calculatesCorrectly() {
        // 4.2km - 3km = 1.2km, ceil = 2km extra → 15k + 2*5k = 25k
        assertEquals(25_000.0, standard.calculate(4.2));
    }

    @Test
    @DisplayName("Standard: Khoảng cách rất xa → tối đa 75.000đ")
    public void standardShipping_veryFar_capAtMaxFee() {
        assertEquals(75_000.0, standard.calculate(50.0));
    }

    @Test
    @DisplayName("Express: Phí cao hơn Standard 1.5 lần")
    public void expressShipping_alwaysHigherThanStandard() {
        double dist = 5.0;
        assertTrue(express.calculate(dist) > standard.calculate(dist));
    }

    @Test
    @DisplayName("Free: Phí ship luôn bằng 0")
    public void freeShipping_alwaysZero() {
        assertEquals(0.0, free.calculate(0.0));
        assertEquals(0.0, free.calculate(10.0));
        assertEquals(0.0, free.calculate(100.0));
    }

    @Test
    @DisplayName("Context: ShippingCalculator có thể đổi strategy khi runtime")
    public void shippingCalculator_canSwitchStrategy() {
        ShippingCalculator calculator = new ShippingCalculator(standard);
        double standardFee = calculator.calculate(5.0);

        calculator.setStrategy(free);
        double freeFee = calculator.calculate(5.0);

        assertTrue(standardFee > 0, "Standard phải > 0");
        assertEquals(0.0, freeFee, "Free phải = 0 sau khi đổi strategy");
    }
}
