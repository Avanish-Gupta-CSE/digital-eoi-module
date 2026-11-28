package com.uco.eoi.report;

import com.uco.eoi.common.IndianMoney;
import com.uco.eoi.report.ReportView.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * All SQL for the Annex II report lives here, in native SQL that runs on H2 and Oracle 19c.
 * One method per table. Rupee amounts are formatted here with IndianMoney; Part V amounts
 * stay plain numbers because they are in foreign currency units (G22). A column that was
 * never filled stays null and the page prints it as an empty cell.
 */
@Repository
public class ReportRepository {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String FIND_HEADER =
            "SELECT r.id AS report_id, b.id AS borrower_id, r.report_quarter, r.status, "
            + "b.borrower_name, b.address, b.constitution, b.business_activity_main, "
            + "b.group_affiliation, b.associate_concerns, b.consortium_name, "
            + "r.irac_classification, r.internal_rating, r.internal_rating_narration, "
            + "r.external_rating, r.annual_report_latest, r.annual_report_as_on, "
            + "r.changes_shareholding, r.special_information "
            + "FROM eoi_report r JOIN eoi_borrower b ON b.id = r.borrower_id WHERE r.id = ?";

    private static final String FIND_DIRECTORS =
            "SELECT person_name, designation, net_worth FROM eoi_borrower_director "
            + "WHERE borrower_id = ? ORDER BY id";

    private static final String FIND_ALLIED =
            "SELECT activity FROM eoi_borrower_allied WHERE borrower_id = ? ORDER BY id";

    private static final String FIND_BANKS =
            "SELECT bank_name FROM eoi_borrower_bank WHERE borrower_id = ? ORDER BY id";

    private static final String FIND_FACILITIES =
            "SELECT id AS facility_id, facility_type, purpose, facility_date, sanctioned_amount, "
            + "outstanding_amount, repayment_terms, defaults_term_commitments "
            + "FROM eoi_facility WHERE report_id = ? ORDER BY id";

    private static final String FIND_SECURITIES =
            "SELECT security_description FROM eoi_facility_security "
            + "WHERE facility_id = ? ORDER BY id";

    private static final String FIND_DERIVATIVES =
            "SELECT serial_no, nature, notional_amount, weighted_avg_maturity, positive_mtm, "
            + "npa_amount, restructured_notional_amount, restructuring_reasons "
            + "FROM eoi_derivative WHERE report_id = ? ORDER BY id";

    private static final String FIND_CURRENCIES =
            "SELECT currency, COALESCE(short_long, 0) AS short_long, "
            + "COALESCE(short_short, 0) AS short_short, COALESCE(long_long, 0) AS long_long, "
            + "COALESCE(long_short, 0) AS long_short FROM eoi_currency_position "
            + "WHERE report_id = ? ORDER BY id";

    private static final String FIND_EXPERIENCE =
            "SELECT item_code, grade FROM eoi_report_experience "
            + "WHERE report_id = ? ORDER BY item_code";

    private static final String FIND_RUBRIC =
            "SELECT item_code, param_no, observed_value, grade FROM eoi_report_rubric "
            + "WHERE report_id = ? ORDER BY param_no";

    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** The report header with the borrower's Part I identity. Empty when the id is unknown. */
    public Optional<ReportHeader> findHeader(long reportId) {
        List<ReportHeader> rows = jdbc.query(FIND_HEADER, (rs, rowNumber) -> new ReportHeader(
                rs.getLong("report_id"), rs.getLong("borrower_id"),
                rs.getString("report_quarter"), rs.getString("status"),
                borrowerPart(rs), reportPart(rs)), reportId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Part I.III and I.VI: the directors and partners, with their net worth. */
    public List<DirectorRow> findDirectors(long borrowerId) {
        return jdbc.query(FIND_DIRECTORS, (rs, rowNumber) -> new DirectorRow(
                rs.getString("person_name"), rs.getString("designation"),
                IndianMoney.format(rs.getBigDecimal("net_worth"))), borrowerId);
    }

    /** Part I.IV(b): the allied business activities of the same group. */
    public List<String> findAlliedActivities(long borrowerId) {
        return jdbc.query(FIND_ALLIED, (rs, rowNumber) -> rs.getString("activity"), borrowerId);
    }

    /** Part I.V: the other banks financing the same borrower. */
    public List<String> findOtherBanks(long borrowerId) {
        return jdbc.query(FIND_BANKS, (rs, rowNumber) -> rs.getString("bank_name"), borrowerId);
    }

    /** Part III: the credit facilities of the report, each with its security (III.VII). */
    public List<FacilityRow> findFacilities(long reportId) {
        List<FacilityRow> facilities = new ArrayList<>();
        for (FacilityWithId facility : jdbc.query(FIND_FACILITIES, (rs, rowNumber) ->
                new FacilityWithId(rs.getLong("facility_id"), rs.getString("facility_type"),
                        rs.getString("purpose"), dateText(rs.getDate("facility_date")),
                        IndianMoney.format(rs.getBigDecimal("sanctioned_amount")),
                        IndianMoney.format(rs.getBigDecimal("outstanding_amount")),
                        rs.getString("repayment_terms"),
                        rs.getString("defaults_term_commitments")), reportId)) {
            facilities.add(new FacilityRow(facility.type(), facility.purpose(),
                    facility.dateText(), facility.sanctionedText(), facility.outstandingText(),
                    facility.repaymentTerms(), facility.defaults(),
                    findSecurities(facility.facilityId())));
        }
        return facilities;
    }

    /** Part III.VII: the security offered against one facility. */
    public List<String> findSecurities(long facilityId) {
        return jdbc.query(FIND_SECURITIES, (rs, rowNumber) -> rs.getString("security_description"),
                facilityId);
    }

    /** Part IV: the derivative contracts of the report. */
    public List<DerivativeRow> findDerivatives(long reportId) {
        return jdbc.query(FIND_DERIVATIVES, (rs, rowNumber) -> new DerivativeRow(
                numberText(rs.getBigDecimal("serial_no")), rs.getString("nature"),
                IndianMoney.format(rs.getBigDecimal("notional_amount")),
                numberText(rs.getBigDecimal("weighted_avg_maturity")),
                IndianMoney.format(rs.getBigDecimal("positive_mtm")),
                IndianMoney.format(rs.getBigDecimal("npa_amount")),
                IndianMoney.format(rs.getBigDecimal("restructured_notional_amount")),
                rs.getString("restructuring_reasons")), reportId);
    }

    /** Part V: the currency positions as stored. The three nets are computed in the service. */
    public List<RawCurrency> findCurrencyPositions(long reportId) {
        return jdbc.query(FIND_CURRENCIES, (rs, rowNumber) -> new RawCurrency(
                rs.getString("currency"), rs.getBigDecimal("short_long"),
                rs.getBigDecimal("short_short"), rs.getBigDecimal("long_long"),
                rs.getBigDecimal("long_short")), reportId);
    }

    /** Part VI.A: the six graded experience items. */
    public List<ExperienceRow> findExperience(long reportId) {
        return jdbc.query(FIND_EXPERIENCE, (rs, rowNumber) -> new ExperienceRow(
                rs.getString("item_code"), rs.getString("grade")), reportId);
    }

    /** Part VI.B: the rubric parameter observations. */
    public List<RubricRow> findRubric(long reportId) {
        return jdbc.query(FIND_RUBRIC, (rs, rowNumber) -> new RubricRow(
                rs.getString("item_code"), rs.getInt("param_no"),
                rs.getString("observed_value"), rs.getString("grade")), reportId);
    }

    /** Part I: the borrower's own row. */
    private BorrowerPart borrowerPart(ResultSet rs) throws SQLException {
        return new BorrowerPart(
                rs.getString("borrower_name"), rs.getString("address"),
                rs.getString("constitution"), rs.getString("business_activity_main"),
                rs.getString("group_affiliation"), rs.getString("associate_concerns"),
                rs.getString("consortium_name"));
    }

    /** Part II: the credit quality indicators of the report. */
    private ReportPart reportPart(ResultSet rs) throws SQLException {
        return new ReportPart(
                rs.getString("irac_classification"), rs.getString("internal_rating"),
                rs.getString("internal_rating_narration"), rs.getString("external_rating"),
                rs.getString("annual_report_latest"), dateText(rs.getDate("annual_report_as_on")),
                rs.getString("changes_shareholding"), rs.getString("special_information"));
    }

    /** A missing number is printed as empty text, never as the word null. */
    private static String numberText(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    /** A missing date is printed as empty text; a present date as dd-MM-yyyy. */
    private static String dateText(Date date) {
        return date == null ? "" : date.toLocalDate().format(DATE_FORMAT);
    }
}
