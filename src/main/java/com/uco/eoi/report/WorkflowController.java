package com.uco.eoi.report;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The four maker-checker actions of one report: submit for checking, approve, return to
 * the maker, and pull the Finacle figures.
 *
 * Thin controller: read the input, call the service, redirect. A rule the service
 * refuses comes back as a plain message on the report screen, never as an error page.
 */
@Controller
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /** The maker sends the report for checking. */
    @PostMapping("/reports/{id}/submit")
    public String submit(@PathVariable("id") long id,
            @RequestParam(name = "actorId", defaultValue = "maker01") String actorId,
            RedirectAttributes attributes) {
        try {
            workflowService.submit(id, actorId);
        } catch (IllegalStateException e) {
            return refused(id, e.getMessage(), attributes);
        }
        return "redirect:/reports/" + id + "?workflow=submitted";
    }

    /** The checker approves the report. */
    @PostMapping("/reports/{id}/approve")
    public String approve(@PathVariable("id") long id,
            @RequestParam(name = "actorId", defaultValue = "checker01") String actorId,
            RedirectAttributes attributes) {
        try {
            workflowService.approve(id, actorId);
        } catch (IllegalStateException e) {
            return refused(id, e.getMessage(), attributes);
        }
        return "redirect:/reports/" + id + "?workflow=approved";
    }

    /** The checker sends the report back to the maker with a reason. */
    @PostMapping("/reports/{id}/return")
    public String returnToMaker(@PathVariable("id") long id,
            @RequestParam(name = "actorId", defaultValue = "checker01") String actorId,
            @RequestParam(name = "remarks", required = false, defaultValue = "") String remarks,
            RedirectAttributes attributes) {
        try {
            workflowService.returnToMaker(id, actorId, remarks);
        } catch (IllegalStateException e) {
            return refused(id, e.getMessage(), attributes);
        }
        return "redirect:/reports/" + id + "?workflow=returned";
    }

    /** The maker pulls the Finacle figures into the report. */
    @PostMapping("/reports/{id}/sync-finacle")
    public String syncFinacle(@PathVariable("id") long id,
            @RequestParam(name = "actorId", defaultValue = "maker01") String actorId,
            RedirectAttributes attributes) {
        try {
            workflowService.syncFromFinacle(id, actorId);
        } catch (IllegalStateException e) {
            return refused(id, e.getMessage(), attributes);
        }
        return "redirect:/reports/" + id + "?workflow=synced";
    }

    /** Sends the officer back to the report with the reason the rule was refused. */
    private String refused(long reportId, String message, RedirectAttributes attributes) {
        attributes.addFlashAttribute("errorMessage", message);
        return "redirect:/reports/" + reportId + "?error=notallowed";
    }
}
