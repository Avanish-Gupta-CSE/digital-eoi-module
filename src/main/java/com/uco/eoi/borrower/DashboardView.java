package com.uco.eoi.borrower;

import java.util.List;

/**
 * Everything the dashboard screen needs, in one object.
 * Counts are computed once on the server so the view does no arithmetic.
 */
public record DashboardView(
        String quarter,
        List<BorrowerRow> rows,
        int total,
        int dueCount,
        int submittedCount,
        int overdueCount) {
}
