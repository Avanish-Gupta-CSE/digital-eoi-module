package com.uco.eoi.report;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Refuses every change to the figures of a report that is with the checker or already
 * approved. Only a DRAFT, DUE, OVERDUE or RETURNED report can be edited; once it is
 * SUBMITTED or APPROVED the numbers the checker sees must not move under him.
 *
 * It sits in front of every write URL of the entry screen, so no endpoint can be forgotten.
 * The maker-checker actions (submit, approve, return) are not covered here: the workflow
 * service guards those itself.
 */
public class ReportLockInterceptor implements HandlerInterceptor {

    static final String LOCKED_MESSAGE =
            "This report is with the checker or already approved, so it cannot be changed.";

    private static final Pattern REPORT_PATH = Pattern.compile("^/reports/(\\d+)/.*");

    private static final String FIND_STATUS = "SELECT status FROM eoi_report WHERE id = ?";

    private static final List<String> EDITABLE = List.of("DRAFT", "DUE", "OVERDUE", "RETURNED");

    private final JdbcTemplate jdbc;

    public ReportLockInterceptor(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        if (!"POST".equals(request.getMethod())) {
            return true;
        }
        Matcher path = REPORT_PATH.matcher(request.getRequestURI().substring(
                request.getContextPath().length()));
        if (!path.matches()) {
            return true;
        }
        long reportId = Long.parseLong(path.group(1));
        List<String> status = jdbc.query(FIND_STATUS, (rs, row) -> rs.getString(1), reportId);
        if (status.isEmpty() || EDITABLE.contains(status.get(0))) {
            return true;
        }
        FlashMap flash = new FlashMap();
        flash.put("errorMessage", LOCKED_MESSAGE);
        RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flash, request, response);
        response.sendRedirect(request.getContextPath() + "/reports/" + reportId + "?error=locked");
        return false;
    }
}
