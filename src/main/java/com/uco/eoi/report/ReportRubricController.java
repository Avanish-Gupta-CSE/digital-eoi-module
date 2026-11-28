package com.uco.eoi.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Part VI of Annex II: the graded experience items and the automatic grading of the conduct
 * rubric, from what the officer typed and from the derivative contracts of the report.
 */
@Controller
public class ReportRubricController {

    private final ReportChildWriteRepository childWriteRepository;
    private final ConductScoringService scoring;

    public ReportRubricController(ReportChildWriteRepository childWriteRepository,
            ConductScoringService scoring) {
        this.childWriteRepository = childWriteRepository;
        this.scoring = scoring;
    }

    /** Part VI.A: saves the grade of one experience item. */
    @PostMapping("/reports/{id}/experience/save")
    public String saveExperience(@PathVariable("id") long id,
            @RequestParam Map<String, String> p) {
        childWriteRepository.saveExperience(id, p.get("itemCode"), p.get("grade"));
        return backToEdit(id);
    }

    /** Part VI.A: removes the grade of one experience item. */
    @PostMapping("/reports/{id}/experience/{rowId}/delete")
    public String deleteExperience(@PathVariable("id") long id,
            @PathVariable("rowId") long rowId) {
        childWriteRepository.deleteExperience(id, rowId);
        return backToEdit(id);
    }

    /**
     * Part VI.B: grades the rubric from what the officer typed and from the derivative
     * contracts of the report, and writes the ten rows. A contract still NPA on the date of
     * the exchange of information shows the whole rubric as Below Par.
     */
    @PostMapping("/reports/{id}/rubric/auto-grade")
    public String autoGradeRubric(@PathVariable("id") long id,
            @RequestParam Map<String, String> p) {
        ReportChildItems.DerivativeShares shares = childWriteRepository.derivativeShares(id);
        boolean isNpa = p.containsKey("isNpaAccount");
        List<ReportChildItems.RubricEntry> rows = gradeEveryParameter(p, shares);
        childWriteRepository.replaceRubric(id, applyNpaOverride(rows, isNpa));
        return backToEdit(id);
    }

    /** The ten rubric rows, each graded from the value the officer typed. */
    private List<ReportChildItems.RubricEntry> gradeEveryParameter(Map<String, String> p,
            ReportChildItems.DerivativeShares shares) {
        List<ReportChildItems.RubricEntry> rows = new ArrayList<>();
        rows.add(rubricRow("I", 1, p.get("overdrawings"),
                scoring.gradeFundedOverdrawings(number(p.get("overdrawings")))));
        rows.add(rubricRow("I", 2, p.get("fundedAdjMonths"),
                scoring.gradeFundedAdjustmentMonths(number(p.get("fundedAdjMonths")))));
        rows.add(rubricRow("I", 3, p.get("overdrawingPct"),
                scoring.gradeFundedOverdrawingPercent(decimal(p.get("overdrawingPct")))));
        rows.add(rubricRow("II", 4, p.get("contingentDefaults"),
                scoring.gradeContingentDefaults(number(p.get("contingentDefaults")))));
        rows.add(rubricRow("II", 5, p.get("contingentAdjWeeks"),
                scoring.gradeContingentAdjustmentWeeks(number(p.get("contingentAdjWeeks")))));
        rows.add(rubricRow("III", 6, p.get("irregularities"),
                scoring.gradeMtmOverdueOver30DaysPercent(decimal(p.get("irregularities")))));
        rows.add(rubricRow("III", 7, percentText(shares.stillNpaPercent()),
                scoring.gradeMtmOverdueOver90DaysPercent(shares.stillNpaPercent())));
        rows.add(rubricRow("III", 8, percentText(shares.restructuredPercent()),
                scoring.gradeRestructuredContractsPercent(shares.restructuredPercent())));
        rows.add(rubricRow("IV", 9, p.get("stockStmtDays"),
                scoring.gradeStockStatementDelay(number(p.get("stockStmtDays")))));
        rows.add(rubricRow("IV", 10, p.get("turnoverPct"),
                scoring.gradeCreationOfChargeDelay(number(p.get("turnoverPct")))));
        return rows;
    }

    /** A contract still NPA on the exchange date shows every row as Below Par. */
    private List<ReportChildItems.RubricEntry> applyNpaOverride(
            List<ReportChildItems.RubricEntry> rows, boolean isNpa) {
        List<ReportChildItems.RubricEntry> overridden = new ArrayList<>();
        for (ReportChildItems.RubricEntry row : rows) {
            overridden.add(new ReportChildItems.RubricEntry(row.itemCode(), row.paramNo(),
                    row.observedValue(), scoring.applyNpaOverride(row.grade(), isNpa)));
        }
        return overridden;
    }

    /** One rubric row, with the value the officer typed as the observed value. */
    private ReportChildItems.RubricEntry rubricRow(String itemCode, int paramNo, String observedValue,
            String grade) {
        return new ReportChildItems.RubricEntry(itemCode, paramNo, observed(observedValue), grade);
    }

    /** A blank box is observed as "Not applicable", never as the word null. */
    private static String observed(String value) {
        return value == null || value.isBlank() ? "Not applicable" : value.trim();
    }

    /** A typed whole number; an empty box counts as zero. */
    private static int number(String value) {
        return value == null || value.isBlank() ? 0 : (int) Double.parseDouble(value.trim());
    }

    /** A typed decimal; an empty box counts as zero. */
    private static double decimal(String value) {
        return value == null || value.isBlank() ? 0d : Double.parseDouble(value.trim());
    }

    /** A computed share, printed with one decimal, as the annex prints its percentages. */
    private static String percentText(double percent) {
        return String.format(java.util.Locale.ROOT, "%.1f%%", percent);
    }

    /** Every child form ends at the entry screen, with the "saved" flag showing. */
    private static String backToEdit(long id) {
        return "redirect:/reports/" + id + "/edit?saved=1";
    }
}
