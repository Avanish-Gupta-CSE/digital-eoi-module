package com.uco.eoi.report;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Thin controller: read the id, call the service, hand the view an object, return a view
 * name. No business rules and no formatting live here.
 */
@Controller
public class ReportController {

    private final ReportService reportService;
    private final WorkflowService workflowService;

    public ReportController(ReportService reportService, WorkflowService workflowService) {
        this.reportService = reportService;
        this.workflowService = workflowService;
    }

    @GetMapping("/reports/{id}")
    public String report(@PathVariable("id") long id, Model model) {
        ReportView view = reportService.report(id);
        if (view == null) {
            return "redirect:/dashboard?error=notfound";
        }
        model.addAttribute("view", view);
        model.addAttribute("history", workflowService.history(id));
        return "report";
    }
}
