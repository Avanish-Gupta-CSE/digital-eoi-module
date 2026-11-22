package com.uco.eoi.web;

import com.uco.eoi.borrower.BorrowerService;
import com.uco.eoi.borrower.DashboardView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Thin controller: read input, call the service, hand the view an object, return a view name.
 * No business rules and no formatting live here.
 */
@Controller
public class DashboardController {

    private final BorrowerService borrowerService;
    private final String defaultQuarter;

    public DashboardController(BorrowerService borrowerService,
                               @Value("${eoi.current-quarter}") String defaultQuarter) {
        this.borrowerService = borrowerService;
        this.defaultQuarter = defaultQuarter;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(name = "quarter", required = false) String quarter,
            Model model) {

        String selected = (quarter == null || quarter.isBlank()) ? defaultQuarter : quarter;
        DashboardView view = borrowerService.dashboard(selected);
        model.addAttribute("view", view);
        return "dashboard";
    }
}
