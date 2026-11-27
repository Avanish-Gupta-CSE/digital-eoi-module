package com.uco.eoi.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * The maker-checker rules of one report: the maker submits, a different officer checks,
 * a return without a real reason is refused, and every state change lands in the audit
 * trail.
 *
 * The test runs the same H2 scripts as the h2 profile, in Oracle mode, so the SQL that
 * passes here is the SQL that runs on the bank's database.
 */
class WorkflowServiceTest {

    private static final List<String> SCRIPT_FILES = List.of(
            "db/schema.sql",
            "db/schema-annexii-exposure.sql",
            "db/schema-annexii-experience.sql",
            "db/schema-audit.sql",
            "db/h2-init.sql");

    private Connection connection;
    private JdbcTemplate jdbc;
    private WorkflowService service;

    @BeforeEach
    void loadSeededDatabase() throws Exception {
        // A fresh in-memory database per test, named uniquely so repeat runs cannot collide.
        String url = "jdbc:h2:mem:workflowservicetest_" + System.nanoTime() + ";MODE=Oracle";
        connection = DriverManager.getConnection(url, "sa", "");
        for (String file : SCRIPT_FILES) {
            try (InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
        }
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        service = new WorkflowService(jdbc, new MockFinacleDataSource(jdbc));
    }

    @AfterEach
    void close() throws Exception {
        connection.close();
    }

    @Test
    @DisplayName("the maker submits the report and the submission lands in the audit trail")
    void submit_setsTheStatusAndWritesTheAuditRow() {
        service.submit(steelsReportId(), "maker01");

        assertThat(text("SELECT status FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("SUBMITTED");
        assertThat(text("SELECT submitted_by FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("maker01");
        assertThat(auditActions()).containsExactly("SUBMIT");
    }

    @Test
    @DisplayName("the maker cannot check their own report")
    void approve_byTheMaker_isRefused() {
        service.submit(steelsReportId(), "maker01");

        assertThatThrownBy(() -> service.approve(steelsReportId(), "maker01"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You prepared this report. A different officer must check it.");

        assertThat(text("SELECT status FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("SUBMITTED");
        assertThat(auditActions()).containsExactly("SUBMIT");
    }

    @Test
    @DisplayName("a second officer checks the report and the check lands in the audit trail")
    void approve_byAnotherOfficer_approvesAndWritesTheAuditRow() {
        service.submit(steelsReportId(), "maker01");
        service.approve(steelsReportId(), "checker01");

        assertThat(text("SELECT status FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("APPROVED");
        assertThat(text("SELECT checked_by FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("checker01");
        assertThat(auditActions()).containsExactly("SUBMIT", "APPROVE");
    }

    @Test
    @DisplayName("a return without a real reason is refused and nothing is written")
    void return_withAShortReason_isRefused() {
        service.submit(steelsReportId(), "maker01");

        assertThatThrownBy(() -> service.returnToMaker(steelsReportId(), "checker01", "fix it"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(text("SELECT status FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("SUBMITTED");
        assertThat(auditActions()).containsExactly("SUBMIT");
    }

    @Test
    @DisplayName("the checker returns the report with a reason the maker can read")
    void return_withAReason_sendsTheReportBackToTheMaker() {
        service.submit(steelsReportId(), "maker01");
        service.returnToMaker(steelsReportId(), "checker01", "Internal rating narration is missing.");

        assertThat(text("SELECT status FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("RETURNED");
        assertThat(text("SELECT checked_by FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("checker01");
        assertThat(auditActions()).containsExactly("SUBMIT", "RETURN");
        assertThat(service.history(steelsReportId()).get(1).remarks())
                .isEqualTo("Internal rating narration is missing.");
    }

    @Test
    @DisplayName("the Finacle sync fills the IRAC classification and lands in the audit trail")
    void syncFromFinacle_fillsTheClassificationAndWritesTheAuditRow() {
        jdbc.update("UPDATE eoi_report SET irac_classification = NULL WHERE id = ?",
                steelsReportId());

        service.syncFromFinacle(steelsReportId(), "maker01");

        assertThat(text("SELECT irac_classification FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("Standard");
        assertThat(auditActions()).containsExactly("FINACLE_SYNC");
    }

    @Test
    @DisplayName("the audit trail of a report that was never touched is empty")
    void history_ofAFreshReport_isEmpty() {
        assertThat(service.history(steelsReportId())).isEmpty();
    }

    /** The report id of the seeded borrower Sample Steels Ltd for the current quarter. */
    private long steelsReportId() {
        return jdbc.queryForObject(
                "SELECT r.id FROM eoi_report r JOIN eoi_borrower b ON b.id = r.borrower_id "
                        + "WHERE b.borrower_name = 'Sample Steels Ltd' "
                        + "AND r.report_quarter = 'Q3FY2627'",
                Long.class);
    }

    /** The actions written in the audit trail of the seeded report, oldest first. */
    private List<String> auditActions() {
        return jdbc.queryForList(
                "SELECT action FROM eoi_audit_log WHERE report_id = ? ORDER BY id",
                String.class, steelsReportId());
    }

    private String text(String sql, Object... args) {
        return jdbc.queryForObject(sql, String.class, args);
    }
}
