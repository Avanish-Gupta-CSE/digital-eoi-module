package com.uco.eoi.report;

import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeParseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Turns bad numbers, bad dates, and duplicate child rows on the Annex II child forms into a
 * plain message on the entry screen instead of a 500 error page.
 */
@ControllerAdvice(assignableTypes = {ReportChildController.class, ReportRubricController.class})
public class ReportChildExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class,
            DataIntegrityViolationException.class})
    public String handleChildInputError(Exception ex, HttpServletRequest request,
            RedirectAttributes attributes) {
        attributes.addFlashAttribute("errorMessage", friendlyMessage(ex));
        String reportId = reportIdFromPath(request.getRequestURI());
        if (reportId != null) {
            return "redirect:/reports/" + reportId + "/edit?error=1";
        }
        return "redirect:/dashboard";
    }

    private static String friendlyMessage(Exception ex) {
        if (ex instanceof DuplicateKeyException) {
            return "That entry is already recorded for this report.";
        }
        if (ex instanceof DateTimeParseException) {
            return "Enter dates as dd-MM-yyyy (for example 31-03-2026).";
        }
        if (ex instanceof NumberFormatException) {
            return "Enter plain numbers only (no text or currency symbols) in numeric fields.";
        }
        return "Could not save that entry. Check the required fields and try again.";
    }

    private static String reportIdFromPath(String uri) {
        if (uri == null) {
            return null;
        }
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("reports".equals(parts[i]) && parts[i + 1].matches("\\d+")) {
                return parts[i + 1];
            }
        }
        return null;
    }
}
