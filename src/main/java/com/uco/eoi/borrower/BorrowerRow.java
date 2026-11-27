package com.uco.eoi.borrower;

/**
 * What the view needs for one row. Money is already formatted and the initials are already
 * derived here, so the JSP contains markup only and no logic.
 *
 * The report id is the row's address: it opens the Annex II report of that quarter.
 */
public record BorrowerRow(
        long reportId,
        String initials,
        String borrowerName,
        String consortiumName,
        String sanctionedLimitText,
        String outstandingText,
        String status,
        String statusClass) {
}
