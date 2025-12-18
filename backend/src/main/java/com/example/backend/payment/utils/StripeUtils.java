package com.example.backend.payment.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Small Stripe helper utilities used by PaymentService.
 */
public final class StripeUtils {

    private StripeUtils() {}

    /**
     * Convert a major currency BigDecimal to the smallest unit (cents) as a long.
     * Throws NullPointerException if amount is null and ArithmeticException if value can't be represented.
     */
    public static long amountToCents(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        BigDecimal cents = amount.setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        return cents.longValueExact();
    }





}