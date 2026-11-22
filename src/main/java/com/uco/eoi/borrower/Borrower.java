package com.uco.eoi.borrower;

import java.math.BigDecimal;

/**
 * One row of the dashboard: a borrower for one quarter.
 *
 * The two amounts are the SUM of that report's facilities (Annex II Part III), computed
 * by BorrowerRepository. They are deliberately not stored on the borrower or the report.
 */
public record Borrower(
        long borrowerId,
        String borrowerName,
        String consortiumName,
        BigDecimal sanctionedLimit,
        BigDecimal outstanding,
        String status) {
}
