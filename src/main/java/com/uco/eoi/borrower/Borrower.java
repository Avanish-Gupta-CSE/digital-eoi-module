package com.uco.eoi.borrower;

import java.math.BigDecimal;

/**
 * One row of the dashboard: a borrower for one quarter.
 *
 * The two amounts are the SUM of that report's facilities (Annex II Part III), computed
 * by BorrowerRepository. They are deliberately not stored on the borrower or the report.
 *
 * The report id travels with the row, so the screen can open the Annex II report of the
 * quarter the row belongs to.
 */
public record Borrower(
        long reportId,
        long borrowerId,
        String borrowerName,
        String consortiumName,
        BigDecimal sanctionedLimit,
        BigDecimal outstanding,
        String status) {
}
