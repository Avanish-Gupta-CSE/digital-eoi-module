package com.uco.eoi.report;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The maker-checker rules of one report, and the audit trail they leave.
 *
 * The service owns the rules of REQ-003: the maker prepares and submits, a different
 * officer checks, a return needs a real reason, and every state change is written to
 * eoi_audit_log with the officer, the role and the time.
 *
 * Audit rows are only ever added, never changed and never deleted. The history is the
 * record the bank shows, so the oldest rows must stay exactly as they were written.
 */
@Service
public class WorkflowService {

    static final String ROLE_MAKER = "MAKER";
    static final String ROLE_CHECKER = "CHECKER";

    private static final String REPORT_MISSING =
            "This report is not in a state where that is allowed, or it does not exist. "
                    + "Reload the page and check its status.";

    private static final String FIND_STATUS = "SELECT status FROM eoi_report WHERE id = ?";

    private static final DateTimeFormatter AUDIT_TIME =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final String FIND_SUBMITTED_BY =
            "SELECT submitted_by FROM eoi_report WHERE id = ?";

    private static final String SET_SUBMITTED =
            "UPDATE eoi_report SET status = 'SUBMITTED', submitted_by = ? WHERE id = ? "
                    + "AND status IN ('DRAFT', 'DUE', 'OVERDUE', 'RETURNED')";

    private static final String SET_APPROVED =
            "UPDATE eoi_report SET status = 'APPROVED', checked_by = ? WHERE id = ? "
                    + "AND status = 'SUBMITTED'";

    private static final String SET_RETURNED =
            "UPDATE eoi_report SET status = 'RETURNED', checked_by = ? WHERE id = ? "
                    + "AND status = 'SUBMITTED'";

    private static final String INSERT_AUDIT =
            "INSERT INTO eoi_audit_log (id, report_id, action, actor_id, actor_role, "
                    + "remarks, created_at) "
                    + "VALUES (eoi_seq.NEXTVAL, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

    private static final String FIND_AUDIT =
            "SELECT action, actor_id, actor_role, remarks, created_at "
                    + "FROM eoi_audit_log WHERE report_id = ? ORDER BY id";

    private final JdbcTemplate jdbc;
    private final FinacleDataSource finacleDataSource;

    public WorkflowService(JdbcTemplate jdbc, FinacleDataSource finacleDataSource) {
        this.jdbc = jdbc;
        this.finacleDataSource = finacleDataSource;
    }

    /** The maker hands the report over for checking. */
    @Transactional
    public void submit(long reportId, String actorId) {
        setStatus(SET_SUBMITTED, actorId, reportId);
        writeAudit(reportId, "SUBMIT", actorId, ROLE_MAKER, null);
    }

    /**
     * The checker approves the report.
     *
     * The officer who prepared the report may not check it: one officer must not both
     * prepare and approve, because the exchange of information is a statement to other
     * lenders about the borrower's conduct.
     */
    @Transactional
    public void approve(long reportId, String actorId) {
        if (actorId.equals(findSubmittedBy(reportId))) {
            throw new IllegalStateException(
                    "You prepared this report. A different officer must check it.");
        }
        setStatus(SET_APPROVED, actorId, reportId);
        writeAudit(reportId, "APPROVE", actorId, ROLE_CHECKER, null);
    }

    /**
     * The checker sends the report back to the maker.
     *
     * A reason is required, because the maker has to know what to correct. Ten
     * characters is the shortest reason that can say that.
     */
    @Transactional
    public void returnToMaker(long reportId, String actorId, String remarks) {
        if (remarks == null || remarks.trim().length() < 10) {
            throw new IllegalStateException(
                    "Enter a reason for returning the report, at least 10 characters.");
        }
        setStatus(SET_RETURNED, actorId, reportId);
        writeAudit(reportId, "RETURN", actorId, ROLE_CHECKER, remarks.trim());
    }

    /** Pulls the Finacle figures of the report and records the pull. */
    @Transactional
    public void syncFromFinacle(long reportId, String actorId) {
        List<String> status = jdbc.query(FIND_STATUS, (rs, rowNumber) -> rs.getString(1), reportId);
        if (status.isEmpty() || !List.of("DRAFT", "DUE", "OVERDUE", "RETURNED").contains(status.get(0))) {
            throw new IllegalStateException(REPORT_MISSING);
        }
        finacleDataSource.syncReport(reportId);
        writeAudit(reportId, "FINACLE_SYNC", actorId, ROLE_MAKER,
                "Finacle data pulled for this report.");
    }

    /** The history of one report, oldest event first. */
    public List<AuditEntry> history(long reportId) {
        return jdbc.query(FIND_AUDIT, (rs, rowNumber) -> new AuditEntry(
                rs.getString("action"),
                rs.getString("actor_id"),
                rs.getString("actor_role"),
                rs.getString("remarks"),
                formatTime(rs.getTimestamp("created_at"))), reportId);
    }

    /**
     * Changes the status of the report and the officer who made the change.
     *
     * A report that is not there is refused with a plain message, so the officer is not
     * shown a database error.
     */
    private void setStatus(String updateSql, String actorId, long reportId) {
        if (jdbc.update(updateSql, actorId, reportId) == 0) {
            throw new IllegalStateException(REPORT_MISSING);
        }
    }

    /** One audit row: what happened, who did it, in which role, with what reason. */
    private void writeAudit(long reportId, String action, String actorId, String role,
            String remarks) {
        jdbc.update(INSERT_AUDIT, reportId, action, actorId, role, remarks);
    }

    /** The officer who prepared the report, or null when nobody has prepared it yet. */
    private String findSubmittedBy(long reportId) {
        List<String> names = jdbc.query(FIND_SUBMITTED_BY,
                (rs, rowNumber) -> rs.getString("submitted_by"), reportId);
        return names.isEmpty() ? null : names.get(0);
    }

    /** The time of the audit row as the history screen shows it. */
    private static String formatTime(Timestamp time) {
        return time == null ? "" : AUDIT_TIME.format(time.toLocalDateTime());
    }

    /**
     * One line of the audit trail, as the history screen shows it.
     *
     * The record is public because the JSP reads it with EL: EL calls the accessors by
     * reflection, and reflection cannot reach a package-private type. The rows are
     * immutable, so a public type carries no risk.
     */
    public record AuditEntry(String action, String actorId, String actorRole, String remarks,
            String createdAtText) {
    }
}
