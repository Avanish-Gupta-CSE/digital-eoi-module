package com.uco.eoi.borrower;

/**
 * What the view needs for one row. Money is already formatted and the initials are already
 * derived here, so the JSP contains markup only and no logic.
 */
public record BorrowerRow(
        String initials,
        String borrowerName,
        String consortiumName,
        String sanctionedLimitText,
        String outstandingText,
        String status,
        String statusClass) {
}
