package com.uco.eoi.report;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Part VI children of Annex II: the graded experience items and the rubric rows written
 * when the officer saves a grade or runs the automatic grading.
 *
 * The two derivative shares the rubric asks for are computed here as well, because they are
 * read for that rubric and for nothing else. All SQL is native, so it runs on H2 and Oracle.
 */
@Repository
public class ReportChildWriteRepository {

    private static final String FIND_EXPERIENCE =
            "SELECT id, item_code, grade FROM eoi_report_experience "
            + "WHERE report_id = ? ORDER BY item_code";

    private static final String INSERT_EXPERIENCE =
            "INSERT INTO eoi_report_experience (id, report_id, item_code, grade) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_EXPERIENCE =
            "UPDATE eoi_report_experience SET grade = ? WHERE report_id = ? AND item_code = ?";

    private static final String DELETE_RUBRIC =
            "DELETE FROM eoi_report_rubric WHERE report_id = ?";

    private static final String INSERT_RUBRIC =
            "INSERT INTO eoi_report_rubric (id, report_id, item_code, param_no, observed_value, grade) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String NEXT_ID = "SELECT eoi_seq.NEXTVAL FROM DUAL";

    private final JdbcTemplate jdbc;

    public ReportChildWriteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Saves one graded experience item.
     *
     * An item code already graded for this report is re-graded, not duplicated: the
     * database keeps one row per report and item code.
     */
    @Transactional
    public void saveExperience(long reportId, String itemCode, String grade) {
        int updated = jdbc.update(UPDATE_EXPERIENCE, grade, reportId, itemCode);
        if (updated == 0) {
            jdbc.update(INSERT_EXPERIENCE, nextId(), reportId, itemCode, grade);
        }
    }

    /** Removes one graded experience item. */
    public void deleteExperience(long reportId, long rowId) {
        jdbc.update("DELETE FROM eoi_report_experience WHERE id = ? AND report_id = ?", rowId,
                reportId);
    }

    /** The graded experience items of one report, ready for the entry screen. */
    public List<ReportChildItems.ExperienceItem> findExperience(long reportId) {
        return jdbc.query(FIND_EXPERIENCE, (rs, rowNumber) -> new ReportChildItems.ExperienceItem(
                rs.getLong("id"), rs.getString("item_code"), rs.getString("grade")), reportId);
    }

    /**
     * The two derivative shares the rubric asks for, as percentages of the contracts of
     * the report.
     *
     * Param 7 is the share of contracts still NPA on the date of the exchange of
     * information; param 8 is the share of contracts restructured during the period.
     * A report with no derivative contracts has both shares at zero.
     */
    public ReportChildItems.DerivativeShares derivativeShares(long reportId) {
        int total = 0;
        int stillNpa = 0;
        int restructured = 0;
        for (DerivativeFlag flag : jdbc.query(
                "SELECT CASE WHEN npa_amount > 0 THEN 1 ELSE 0 END AS npa_flag, "
                        + "CASE WHEN restructured_notional_amount > 0 THEN 1 ELSE 0 END "
                        + "AS restructured_flag FROM eoi_derivative WHERE report_id = ?",
                (rs, rowNumber) -> new DerivativeFlag(rs.getInt("npa_flag"),
                        rs.getInt("restructured_flag")), reportId)) {
            total++;
            if (flag.npa() == 1) {
                stillNpa++;
            }
            if (flag.restructured() == 1) {
                restructured++;
            }
        }
        return new ReportChildItems.DerivativeShares(percent(stillNpa, total), percent(restructured, total));
    }

    /** A share of contracts as a percentage; no contracts means no share. */
    private static double percent(int part, int total) {
        if (total == 0) {
            return 0d;
        }
        return (part * 100d) / total;
    }

    /**
     * Replaces the whole rubric of one report with the rows given.
     *
     * The delete and the inserts happen in one transaction, so the report never shows a
     * half-written rubric.
     */
    @Transactional
    public void replaceRubric(long reportId, List<ReportChildItems.RubricEntry> rows) {
        jdbc.update(DELETE_RUBRIC, reportId);
        for (ReportChildItems.RubricEntry row : rows) {
            jdbc.update(INSERT_RUBRIC, nextId(), reportId, row.itemCode(), row.paramNo(),
                    row.observedValue(), row.grade());
        }
    }

    /** The next id of the shared sequence, handed out the same way on H2 and Oracle. */
    private long nextId() {
        Long id = jdbc.queryForObject(NEXT_ID, Long.class);
        return id == null ? 0L : id;
    }

    /** One derivative row, reduced to the two flags the rubric counts. */
    private record DerivativeFlag(int npa, int restructured) {
    }
}
