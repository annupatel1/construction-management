package com.construction.construction_management;

import com.construction.construction_management.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyTest {

    @Test
    void roundsHalfUpToPaise() {
        assertEquals(new BigDecimal("10.13"), Money.money(new BigDecimal("10.125")));
        assertEquals(new BigDecimal("10.12"), Money.money(new BigDecimal("10.124")));
        assertEquals(new BigDecimal("0.00"), Money.zero());
    }

    @Test
    void multipliesRoundedOperands() {
        BigDecimal area = Money.money(new BigDecimal("100.555"));
        BigDecimal rate = Money.money(new BigDecimal("12.345"));
        assertEquals(new BigDecimal("100.56"), area);
        assertEquals(new BigDecimal("12.35"), rate);
        assertEquals(new BigDecimal("1241.92"), Money.multiply(area, rate));
    }

    @Test
    void nullsAreTreatedAsZeroForTotals() {
        assertEquals(new BigDecimal("5.00"), Money.add(null, new BigDecimal("5")));
        assertEquals(new BigDecimal("-5.00"), Money.subtract(null, new BigDecimal("5")));
    }
}
