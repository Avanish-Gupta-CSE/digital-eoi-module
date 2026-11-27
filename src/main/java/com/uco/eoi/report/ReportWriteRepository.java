package com.uco.eoi.report;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The write side of the Annex II entry screen: read the form of a report, save Part I and
 * Part II, and copy a previous quarter into the report being edited. The SQL is native and
 * portable, one method per business action, and every new id comes from the shared
 * sequence, the same way on H2 and on Oracle 19c.
 */
@Repository
public class ReportWriteRepository {

    private static final String FIND_FORM =
            "SELECT b.address, b.constitution, b.business_activity_main, b.group_affiliation, "
                    + "b.associate_concerns, r.irac_classification, r.internal_rating, "
                    + "r.internal_rating_narration, r.external_rating, r.annual_report_latest, "
                    + "r.annual_report_as_on, r.changes_shareholding, r.special_information "
                    + "FROM eoi_report r JOIN eoi_borrower b ON b.id = r.borrower_id "
                    + "WHERE r.id = ?";

    private static final String FIND_BORROWER_ID =
            "SELECT borrower_id FROM eoi_report WHERE id = ?";

    private static final String FIND_LATEST_APPROVED =
            "SELECT id FROM eoi_report WHERE borrower_id = ? AND status = 'APPROVED' "
                    + "AND report_quarter <> ? ORDER BY id DESC";

    private static final String UPDATE_BORROWER =
            "UPDATE eoi_borrower SET address = ?, constitution = ?, business_activity_main = ?, "
                    + "group_affiliation = ?, associate_concerns = ? WHERE id = ?";

    private static final String UPDATE_PART2 =
            "UPDATE eoi_report SET irac_classification = ?, internal_rating = ?, "
                    + "internal_rating_narration = ?, external_rating = ?, "
                    + "annual_report_latest = ?, annual_report_as_on = ?, "
                    + "changes_shareholding = ?, special_information = ? WHERE id = ?";

    private static final String RESET_STATUS =
            "UPDATE eoi_report SET status = 'DRAFT' WHERE id = ? AND status IN ('DUE', 'OVERDUE')";

    private static final String COUNT_FACILITIES =
            "SELECT COUNT(*) FROM eoi_facility WHERE report_id = ?";

    private static final String FIND_FACILITY_IDS =
            "SELECT id FROM eoi_facility WHERE report_id = ? ORDER BY id";

    private static final String INSERT_FACILITY =
            "INSERT INTO eoi_facility (id, report_id, facility_type, purpose, facility_date, "
                    + "sanctioned_amount, outstanding_amount, repayment_terms, "
                    + "defaults_term_commitments) SELECT ?, ?, facility_type, purpose, "
                    + "facility_date, sanctioned_amount, outstanding_amount, repayment_terms, "
                    + "defaults_term_commitments FROM eoi_facility WHERE id = ?";

    private static final String INSERT_SECURITY =
            "INSERT INTO eoi_facility_security (id, facility_id, security_description) "
                    + "SELECT ?, ?, security_description FROM eoi_facility_security "
                    + "WHERE facility_id = ?";

    private static final String NEXT_ID = "SELECT eoi_seq.NEXTVAL FROM DUAL";

    private final JdbcTemplate jdbc;

    public ReportWriteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** The Part I and Part II values of one report, ready for the entry form. */
    public ReportEditForm loadForm(long reportId) {
        List<ReportEditForm> rows = jdbc.query(FIND_FORM, ReportWriteRepository::mapForm, reportId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** The borrower of one report, or 0 when the report is unknown. */
    public long findBorrowerId(long reportId) {
        List<Long> ids = jdbc.query(FIND_BORROWER_ID,
                (rs, rowNumber) -> rs.getLong("borrower_id"), reportId);
        return ids.isEmpty() ? 0L : ids.get(0);
    }

    /**
     * The newest approved report of the borrower, or 0 when there is none. Newest means the
     * highest id: ids rise in the order the quarters were created, while the quarter text
     * does not sort by date (Q4 of one year beats Q1 of the next).
     */
    public long findLatestApprovedReportId(long borrowerId, String excludeQuarter) {
        List<Long> ids = jdbc.query(FIND_LATEST_APPROVED,
                (rs, rowNumber) -> rs.getLong("id"), borrowerId, excludeQuarter);
        return ids.isEmpty() ? 0L : ids.get(0);
    }

    /**
     * Copies the Part II values and the facilities of the source report into the target.
     *
     * The target keeps whatever the maker already typed: the facilities are copied only
     * when the target has none.
     */
    @Transactional
    public void prefillFromReport(long targetReportId, long sourceReportId) {
        ReportEditForm source = loadForm(sourceReportId);
        if (source == null) {
            return;
        }
        updatePart2(targetReportId, source);
        copyFacilities(sourceReportId, targetReportId);
    }

    /**
     * Saves Part I on the borrower and Part II on the report.
     *
     * A report that was DUE or OVERDUE goes back to DRAFT: the maker has changed the
     * values, so the return has to be checked again before it is sent.
     */
    @Transactional
    public void updatePart1And2(long reportId, ReportEditForm form) {
        long borrowerId = findBorrowerId(reportId);
        if (borrowerId == 0L) {
            return;
        }
        jdbc.update(UPDATE_BORROWER, form.getAddress(), form.getConstitution(),
                form.getMainActivity(), form.getGroupAffiliation(),
                form.getAssociateConcerns(), borrowerId);
        updatePart2(reportId, form);
        jdbc.update(RESET_STATUS, reportId);
    }

    /** The eight Part II columns, the same for a save and for a prefill. */
    private void updatePart2(long reportId, ReportEditForm form) {
        jdbc.update(UPDATE_PART2, form.getIracClassification(), form.getInternalRating(),
                form.getInternalRatingNarration(), form.getExternalRating(),
                form.getAnnualReportLatest(), dateParam(form.getAnnualReportAsOn()),
                form.getChangesShareholding(), form.getSpecialInformation(), reportId);
    }

    /** Copies the facilities of the source report, but only into a target that has none. */
    private void copyFacilities(long sourceReportId, long targetReportId) {
        if (countFacilities(targetReportId) > 0) {
            return;
        }
        for (long sourceFacilityId : findFacilityIds(sourceReportId)) {
            copyFacility(sourceFacilityId, targetReportId);
        }
    }

    /** One facility of the source report, copied onto the target report with a new id. */
    private void copyFacility(long sourceFacilityId, long targetReportId) {
        long newFacilityId = nextId();
        jdbc.update(INSERT_FACILITY, newFacilityId, targetReportId, sourceFacilityId);
        jdbc.update(INSERT_SECURITY, nextId(), newFacilityId, sourceFacilityId);
    }

    private List<Long> findFacilityIds(long reportId) {
        return jdbc.query(FIND_FACILITY_IDS, (rs, rowNumber) -> rs.getLong("id"), reportId);
    }

    private int countFacilities(long reportId) {
        Integer count = jdbc.queryForObject(COUNT_FACILITIES, Integer.class, reportId);
        return count == null ? 0 : count;
    }

    /** The next id of the shared sequence, handed out the same way on H2 and Oracle. */
    private long nextId() {
        Long id = jdbc.queryForObject(NEXT_ID, Long.class);
        return id == null ? 0L : id;
    }

    private static ReportEditForm mapForm(ResultSet rs, int rowNumber) throws SQLException {
        ReportEditForm form = new ReportEditForm();
        form.setAddress(rs.getString("address"));
        form.setConstitution(rs.getString("constitution"));
        form.setMainActivity(rs.getString("business_activity_main"));
        form.setGroupAffiliation(rs.getString("group_affiliation"));
        form.setAssociateConcerns(rs.getString("associate_concerns"));
        form.setIracClassification(rs.getString("irac_classification"));
        form.setInternalRating(rs.getString("internal_rating"));
        form.setInternalRatingNarration(rs.getString("internal_rating_narration"));
        form.setExternalRating(rs.getString("external_rating"));
        form.setAnnualReportLatest(rs.getString("annual_report_latest"));
        form.setAnnualReportAsOn(localDate(rs.getDate("annual_report_as_on")));
        form.setChangesShareholding(rs.getString("changes_shareholding"));
        form.setSpecialInformation(rs.getString("special_information"));
        return form;
    }

    private static LocalDate localDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static Date dateParam(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }
}
