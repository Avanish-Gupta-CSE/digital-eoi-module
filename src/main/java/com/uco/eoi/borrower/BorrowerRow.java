package com.uco.eoi.borrower;

/**
 * What the view needs for one row. Money is already formatted here, so the JSP
 * contains markup only and no formatting logic.
 */
public record BorrowerRow(
        String borrowerName,
        String consortiumName,
        String sanctionedLimitText,
        String outstandingText,
        String status,
        String statusClass) {
}
