package com.uco.eoi.report;

import com.uco.eoi.report.ReportView.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * The Annex II entry screen: open the form, save it, fill it from the previous quarter,
 * and export the report as CSV.
 *
 * Thin controller: read the input, call the service or the repository, put the objects in
 * the model, return the view name. The required-field rule lives here because it is a
 * screen rule, and it says in plain words what the officer has to type.
 */
@Controller
public class ReportEditController {

    private static final List<String> REPORT_HEADERS = List.of(
            "Borrower Name", "Quarter", "Status", "Address", "Constitution",
            "Business Activity (Main)", "Group Affiliation", "Associate Concerns",
            "IRAC Classification", "Internal Rating", "Internal Rating Narration",
            "External Rating", "Annual Report Latest", "Annual Report As On",
            "Changes in Shareholding", "Special Information");

    private static final List<String> FACILITY_HEADERS = List.of(
            "Facility Type", "Purpose", "Facility Date", "Sanctioned Amount",
            "Balance Outstanding", "Repayment Terms", "Defaults / Term Commitments",
            "Security Offered");

    private final ReportService reportService;
    private final ReportWriteRepository writeRepository;
    private final ReportChildRepository childRepository;

    public ReportEditController(ReportService reportService,
            ReportWriteRepository writeRepository,
            ReportChildRepository childRepository) {
        this.reportService = reportService;
        this.writeRepository = writeRepository;
        this.childRepository = childRepository;
    }

    /** The entry screen of one report, with the values already in it. */
    @GetMapping("/reports/{id}/edit")
    public String edit(@PathVariable("id") long id, Model model) {
        ReportView view = reportService.report(id);
        if (view == null) {
            return "redirect:/dashboard?error=notfound";
        }
        model.addAttribute("view", view);
        model.addAttribute("form", writeRepository.loadForm(id));
        model.addAttribute("editChildren", childRepository.loadAll(id));
        return "report-edit";
    }

    /** Saves the entry. A blank required field brings the officer back to the form. */
    @PostMapping("/reports/{id}/edit")
    public String save(@PathVariable("id") long id,
            @ModelAttribute("form") ReportEditForm form,
            BindingResult binding, Model model) {
        rejectBlankFields(form, binding);
        if (binding.hasErrors()) {
            model.addAttribute("view", reportService.report(id));
            model.addAttribute("editChildren", childRepository.loadAll(id));
            return "report-edit";
        }
        writeRepository.updatePart1And2(id, form);
        return "redirect:/reports/" + id + "?saved=1";
    }

    /** Fills the report from the last approved quarter of the same borrower. */
    @PostMapping("/reports/{id}/prefill")
    public String prefill(@PathVariable("id") long id) {
        ReportView view = reportService.report(id);
        if (view == null) {
            return "redirect:/dashboard?error=notfound";
        }
        long borrowerId = writeRepository.findBorrowerId(id);
        long sourceId = writeRepository.findLatestApprovedReportId(borrowerId, view.quarter());
        if (sourceId == 0L) {
            return "redirect:/reports/" + id + "/edit?prefill=none";
        }
        writeRepository.prefillFromReport(id, sourceId);
        return "redirect:/reports/" + id + "/edit?prefilled=1";
    }

    /** The report as a CSV file, for the officer to keep or to send on. */
    @GetMapping("/reports/{id}/export.csv")
    public void export(@PathVariable("id") long id, HttpServletResponse response)
            throws IOException {
        ReportView view = reportService.report(id);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"annex-ii-report-" + id + ".csv\"");
        PrintWriter out = response.getWriter();
        if (view == null) {
            out.println("Report not found");
            return;
        }
        writeCsv(view, out);
    }

    /** The three fields the bank asks for every time. The message says what to type. */
    private void rejectBlankFields(ReportEditForm form, BindingResult binding) {
        rejectIfBlank(binding, "address", form.getAddress(),
                "Address is required. Enter the address of the borrower.");
        rejectIfBlank(binding, "constitution", form.getConstitution(),
                "Constitution is required. Enter the constitution of the company, "
                        + "for example Private Limited Company.");
        rejectIfBlank(binding, "mainActivity", form.getMainActivity(),
                "Business activity is required. Enter the main business activity "
                        + "of the company.");
    }

    private void rejectIfBlank(BindingResult binding, String field, String value,
            String message) {
        if (value == null || value.isBlank()) {
            binding.rejectValue(field, "required", message);
        }
    }

    /**
     * The report on one sheet: the borrower and Part II first, the facilities below.
     *
     * The amounts are the same figures the screen shows, because both come from the one
     * money formatter.
     */
    private void writeCsv(ReportView view, PrintWriter out) {
        out.println(csvRow(REPORT_HEADERS.toArray(new String[0])));
        out.println(csvRow(view.borrower().name(), view.quarter(), view.status(),
                view.borrower().address(), view.borrower().constitution(),
                view.borrower().mainActivity(), view.borrower().groupAffiliation(),
                view.borrower().associateConcerns(), view.report().iracClassification(),
                view.report().internalRating(), view.report().internalRatingNarration(),
                view.report().externalRating(), view.report().annualReportLatest(),
                view.report().annualReportAsOn(), view.report().changesShareholding(),
                view.report().specialInformation()));
        out.println();
        out.println(csvRow(FACILITY_HEADERS.toArray(new String[0])));
        for (FacilityRow facility : view.facilities()) {
            out.println(csvRow(facility.type(), facility.purpose(),
                    facility.facilityDateText(), facility.sanctionedText(),
                    facility.outstandingText(), facility.repaymentTerms(), facility.defaults(),
                    String.join("; ", facility.securities())));
        }
    }

    /** One line of the file. A cell with a comma, a quote or a line break is quoted. */
    private static String csvRow(String... cells) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(csvCell(cells[i]));
        }
        return line.toString();
    }

    private static String csvCell(String value) {
        String text = value == null ? "" : value;
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
