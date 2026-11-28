package com.uco.eoi.report;

import com.uco.eoi.common.IndianMoney;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * The Part I children of Annex II — directors, allied activities and other banks — read for
 * the entry screen and written by the child forms. The rest of the children are read through
 * the exposure and Part VI repositories, so the screen still gets all of them in one call.
 *
 * These children belong to the borrower, not to the report, so their methods take a report id
 * and look the borrower up from it. All SQL is native, so it runs on H2 and on Oracle 19c.
 */
@Repository
public class ReportChildRepository {

    private static final String FIND_BORROWER_ID =
            "SELECT borrower_id FROM eoi_report WHERE id = ?";

    private static final String FIND_DIRECTORS =
            "SELECT id, person_name, designation, net_worth FROM eoi_borrower_director "
            + "WHERE borrower_id = ? ORDER BY id";

    private static final String FIND_ALLIED =
            "SELECT id, activity FROM eoi_borrower_allied WHERE borrower_id = ? ORDER BY id";

    private static final String FIND_BANKS =
            "SELECT id, bank_name FROM eoi_borrower_bank WHERE borrower_id = ? ORDER BY id";

    private static final String INSERT_DIRECTOR =
            "INSERT INTO eoi_borrower_director (id, borrower_id, person_name, designation, net_worth) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String INSERT_ALLIED =
            "INSERT INTO eoi_borrower_allied (id, borrower_id, activity) VALUES (?, ?, ?)";

    private static final String INSERT_BANK =
            "INSERT INTO eoi_borrower_bank (id, borrower_id, bank_name) VALUES (?, ?, ?)";

    private static final String NEXT_ID = "SELECT eoi_seq.NEXTVAL FROM DUAL";

    private final JdbcTemplate jdbc;
    private final ReportExposureRepository exposure;
    private final ReportChildWriteRepository childWrite;

    public ReportChildRepository(JdbcTemplate jdbc, ReportExposureRepository exposure,
            ReportChildWriteRepository childWrite) {
        this.jdbc = jdbc;
        this.exposure = exposure;
        this.childWrite = childWrite;
    }

    /** Every child table of one report, ready for the entry screen. */
    public ReportChildItems.EditChildren loadAll(long reportId) {
        long borrowerId = findBorrowerId(reportId);
        return new ReportChildItems.EditChildren(
                findDirectors(borrowerId),
                findAllied(borrowerId),
                findBanks(borrowerId),
                exposure.findFacilities(reportId),
                exposure.findDerivatives(reportId),
                exposure.findCurrencies(reportId),
                childWrite.findExperience(reportId));
    }

    /** Adds one director or partner to the borrower of the report. */
    public void insertDirector(long reportId, String name, String designation, String netWorthText) {
        jdbc.update(INSERT_DIRECTOR, nextId(), findBorrowerId(reportId), name, designation,
                amount(netWorthText));
    }

    /** Removes one director or partner. */
    public void deleteDirector(long reportId, long rowId) {
        jdbc.update("DELETE FROM eoi_borrower_director WHERE id = ? AND borrower_id = ?", rowId,
                findBorrowerId(reportId));
    }

    /** Adds one allied business activity to the borrower of the report. */
    public void insertAllied(long reportId, String activityName) {
        jdbc.update(INSERT_ALLIED, nextId(), findBorrowerId(reportId), activityName);
    }

    /** Removes one allied business activity. */
    public void deleteAllied(long reportId, long rowId) {
        jdbc.update("DELETE FROM eoi_borrower_allied WHERE id = ? AND borrower_id = ?", rowId,
                findBorrowerId(reportId));
    }

    /** Adds one other financing bank to the borrower of the report. */
    public void insertBank(long reportId, String bankName) {
        jdbc.update(INSERT_BANK, nextId(), findBorrowerId(reportId), bankName);
    }

    /** Removes one other financing bank. */
    public void deleteBank(long reportId, long rowId) {
        jdbc.update("DELETE FROM eoi_borrower_bank WHERE id = ? AND borrower_id = ?", rowId,
                findBorrowerId(reportId));
    }

    /** The borrower of one report, or 0 when the report is unknown. */
    public long findBorrowerId(long reportId) {
        List<Long> ids = jdbc.query(FIND_BORROWER_ID,
                (rs, rowNumber) -> rs.getLong("borrower_id"), reportId);
        return ids.isEmpty() ? 0L : ids.get(0);
    }

    private List<ReportChildItems.DirectorItem> findDirectors(long borrowerId) {
        return jdbc.query(FIND_DIRECTORS, (rs, rowNumber) -> new ReportChildItems.DirectorItem(
                rs.getLong("id"), rs.getString("person_name"), rs.getString("designation"),
                IndianMoney.format(rs.getBigDecimal("net_worth"))), borrowerId);
    }

    private List<ReportChildItems.TextItem> findAllied(long borrowerId) {
        return jdbc.query(FIND_ALLIED, (rs, rowNumber) -> new ReportChildItems.TextItem(
                rs.getLong("id"), rs.getString("activity")), borrowerId);
    }

    private List<ReportChildItems.TextItem> findBanks(long borrowerId) {
        return jdbc.query(FIND_BANKS, (rs, rowNumber) -> new ReportChildItems.TextItem(
                rs.getLong("id"), rs.getString("bank_name")), borrowerId);
    }

    /** The next id of the shared sequence, handed out the same way on H2 and Oracle. */
    private long nextId() {
        Long id = jdbc.queryForObject(NEXT_ID, Long.class);
        return id == null ? 0L : id;
    }

    /** A typed amount, or null when the officer left the box empty. */
    private static BigDecimal amount(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return new BigDecimal(text.replace(",", "").trim());
    }
}
