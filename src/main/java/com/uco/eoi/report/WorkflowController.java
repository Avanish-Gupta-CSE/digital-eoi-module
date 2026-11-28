package com.uco.eoi.report;

import com.uco.eoi.auth.StaffPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The four maker-checker actions of one report: submit for checking, approve, return to
 * the maker, and pull the Finacle figures.
 *
 * Thin controller: read the input, call the service, redirect. A rule the service
 * refuses comes back as a plain message on the report screen, never as an error page.
 *
 * The person who acts is the signed-in officer. With nobody signed in every action is
 * refused, even on the h2 profile where the login redirect is off.
 */
@Controller
public class WorkflowController {

    private static final String CHECKER_ONLY =
            "Only a checker can approve, return or re-open a report. Your role is maker.";

    private static final String SIGN_IN = "Sign in before you act on a report.";

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /** The maker sends the report for checking. */
    @PostMapping("/reports/{id}/submit")
    public String submit(@PathVariable("id") long id,
            @SessionAttribute(name = "staff", required = false) StaffPrincipal staff,
            RedirectAttributes attributes) {
        if (staff == null) {
            return refused(id, SIGN_IN, attributes);
        }
        try {
            workflowService.submit(id, staff.empNo());
        } catch (IllegalStateException e) {
            return refused(id, e.getMessage(), attributes);
        }
        return "redirect:/reports/" + id + "?workflow=submitted";
    }

    /** The checker approves the report. */
    @PostMapping("/reports/{id}/approve")
    public String approve(@PathVariable("id") long id,
            @SessionAttribute(name = "staff", required = false) StaffPrincipal staff,
            RedirectAttributes attributes) {
        if (notChecker(staff)) {
            return refused(id, staff == null ? SIGN_IN : CHECKER_ONLY, attributes);
        }
        try {
            workflowService.approve(id, staff.empNo());
        } catch (IllegalStateException e) {
            return refused(id, e.getMessage(), attributes);
        }
        return "redirect:/reports/" + id + "?workflow=approved";
    }

    /** The checker sends the report back to the maker with a reason. */
    @PostMapping("/reports/{id}/return")
    public String returnToMaker(@PathVariable("id") long id,
            @RequestParam(name = "remarks", required = false, defaultValue = "") String remarks,
            @SessionAttribute(name = "staff", required = false) StaffPrincipal staff,
            RedirectAttributes attributes) {
        if (notChecker(staff)) {
            return refused(id, staff == null ? SIGN_IN : CHECKER_ONLY, attributes);
        }
        try {
            workflowService.returnToMaker(id, staff.empNo(), remarks);
        } catch (IllegalStateException e) {
            return refused(id, e.getMessage(), attributes);
        }
        return "redirect:/reports/" + id + "?workflow=returned";
    }

    /** The checker re-opens an approved report, with a reason. */
    @PostMapping("/reports/{id}/reopen")
    public String reopen(@PathVariable("id") long id,
            @RequestParam(name = "remarks", required = false, defaultValue = "") String remarks,
            @SessionAttribute(name = "staff", required = false) StaffPrincipal staff,
            RedirectAttributes attributes) {
        if (notChecker(staff)) {
            return refused(id, staff == null ? SIGN_IN : CHECKER_ONLY, attributes);
        }
        try {
            workflowService.reopen(id, staff.empNo(), remarks);
        } catch (IllegalStateException e) {
            return refused(id, e.getMessage(), attributes);
        }
        return "redirect:/reports/" + id + "?workflow=reopened";
    }

    /** The maker pulls the Finacle figures into the report. */
    @PostMapping("/reports/{id}/sync-finacle")
    public String syncFinacle(@PathVariable("id") long id,
            @SessionAttribute(name = "staff", required = false) StaffPrincipal staff,
            RedirectAttributes attributes) {
        if (staff == null) {
            return refused(id, SIGN_IN, attributes);
        }
        try {
            workflowService.syncFromFinacle(id, staff.empNo());
        } catch (IllegalStateException e) {
            return refused(id, e.getMessage(), attributes);
        }
        return "redirect:/reports/" + id + "?workflow=synced";
    }

    /** Nobody signed in, or a signed-in officer who is not a checker. */
    private boolean notChecker(StaffPrincipal staff) {
        return staff == null || !staff.isChecker();
    }

    /** Sends the officer back to the report with the reason the rule was refused. */
    private String refused(long reportId, String message, RedirectAttributes attributes) {
        attributes.addFlashAttribute("errorMessage", message);
        return "redirect:/reports/" + reportId + "?error=notallowed";
    }
}
