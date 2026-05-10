package com.duong.salesmanagement.util;

/**
 * Utility class – phone number masking.
 *
 * <pre>
 * Input:  0987654321
 * Output: 098****321
 * </pre>
 *
 * Rule: keep first 3 + last 3 digits, replace middle with {@code ****}.
 * Applied when order status = COMPLETED or CANCELLED.
 */
public final class PhoneMaskUtil {

    private static final int KEEP_PREFIX = 3;
    private static final int KEEP_SUFFIX = 3;
    private static final int MIN_LENGTH  = KEEP_PREFIX + KEEP_SUFFIX + 1;

    private PhoneMaskUtil() { /* utility class */ }

    /**
     * Mask a phone number unconditionally.
     *
     * @param phone raw phone number (may be {@code null})
     * @return masked string, or {@code null} if input is null/blank,
     *         or original string if too short to mask
     */
    public static String mask(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String digits = phone.trim();
        if (digits.length() < MIN_LENGTH) return digits;
        return digits.substring(0, KEEP_PREFIX)
             + "****"
             + digits.substring(digits.length() - KEEP_SUFFIX);
    }

    /**
     * Mask only when {@code shouldMask} is {@code true}.
     *
     * @param phone      raw phone number
     * @param shouldMask {@code true} for COMPLETED / CANCELLED orders
     * @return masked or original phone string
     */
    public static String maskIf(String phone, boolean shouldMask) {
        return shouldMask ? mask(phone) : phone;
    }
}
