package com.uco.eoi.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IndianMoneyTest {

    @Test
    @DisplayName("formats 25 crore with Indian digit grouping")
    void formatsCrore() {
        assertThat(IndianMoney.format(new BigDecimal("250000000")))
                .isEqualTo("\u20B9 25,00,00,000.00");
    }

    @Test
    @DisplayName("formats 7.5 crore with Indian digit grouping")
    void formatsLakhAndCrore() {
        assertThat(IndianMoney.format(new BigDecimal("75000000")))
                .isEqualTo("\u20B9 7,50,00,000.00");
    }

    @Test
    @DisplayName("keeps small amounts ungrouped and keeps two decimals")
    void formatsSmallAmount() {
        assertThat(IndianMoney.format(new BigDecimal("1234.5")))
                .isEqualTo("\u20B9 1,234.50");
    }

    @Test
    @DisplayName("returns empty text for a missing amount instead of printing null")
    void handlesNull() {
        assertThat(IndianMoney.format(null)).isEmpty();
    }
}
