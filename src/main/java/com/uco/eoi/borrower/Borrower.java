package com.uco.eoi.borrower;

import java.math.BigDecimal;

/**
 * One borrower row as stored in EOI_BORROWER. A plain carrier: no logic.
 */
public record Borrower(
        long id,
        String borrowerName,
        String consortiumName,
        BigDecimal sanctionedLimit,
        BigDecimal outstanding,
        String reportQuarter,
        String status) {
}
