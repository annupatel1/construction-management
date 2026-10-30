package com.construction.construction_management.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * One place for monetary rounding. All amounts are stored and returned with two decimals,
 * rounded half-up (the convention used on Indian invoices). Rates and areas also keep two
 * decimals; quantities keep three.
 */
public final class Money {

    public static final int MONEY_SCALE = 2;
    public static final int QUANTITY_SCALE = 3;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private Money() {
    }

    public static BigDecimal money(BigDecimal value) {
        return value == null ? null : value.setScale(MONEY_SCALE, ROUNDING);
    }

    public static BigDecimal money(double value) {
        return BigDecimal.valueOf(value).setScale(MONEY_SCALE, ROUNDING);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE);
    }

    public static BigDecimal add(BigDecimal left, BigDecimal right) {
        return money(nvl(left).add(nvl(right)));
    }

    public static BigDecimal subtract(BigDecimal left, BigDecimal right) {
        return money(nvl(left).subtract(nvl(right)));
    }

    public static BigDecimal multiply(BigDecimal left, BigDecimal right) {
        return money(nvl(left).multiply(nvl(right)));
    }

    public static BigDecimal quantity(BigDecimal value) {
        return value == null ? null : value.setScale(QUANTITY_SCALE, ROUNDING);
    }

    public static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
