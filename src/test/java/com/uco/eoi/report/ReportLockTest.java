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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * A report that is with the checker or approved is frozen; a workflow action is only valid
 * from the right status; a child row can only be deleted through its own report.
 */
class ReportLockTest {

    private static final List<String> SCRIPT_FILES = List.of(
            "db/schema.sql", "db/schema-annexii-exposure.sql",
            "db/schema-annexii-experience.sql", "db/schema-audit.sql", "db/h2-init.sql");

    private Connection connection;
    private JdbcTemplate jdbc;
    private WorkflowService workflow;

    @BeforeEach
    void loadSeededDatabase() throws Exception {
        connection = DriverManager.getConnection(
                "jdbc:h2:mem:reportlocktest_" + System.nanoTime() + ";MODE=Oracle", "sa", "");
        for (String file : SCRIPT_FILES) {
            try (InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
        }
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        workflow = new WorkflowService(jdbc, new MockFinacleDataSource(jdbc));
    }

    @AfterEach
    void close() throws Exception {
        connection.close();
    }

    @Test
    @DisplayName("an edit of a submitted report is redirected away and never reaches the controller")
    void editOfASubmittedReport_isRefused() throws Exception {
        long id = firstReportId();
        workflow.submit(id, "maker01");

        MockHttpServletResponse response = post("/reports/" + id + "/edit");

        assertThat(response.getRedirectedUrl()).isEqualTo("/reports/" + id + "?error=locked");
    }

    @Test
    @DisplayName("a report that is still a draft can be edited")
    void editOfADraft_isAllowed() throws Exception {
        MockHttpServletResponse response = post("/reports/" + firstReportId() + "/edit");

        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    @DisplayName("a checker action on a report that was never submitted is refused")
    void approveOfADraft_isRefused() {
        long id = firstReportId();

        assertThatThrownBy(() -> workflow.approve(id, "checker01"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("SELECT status FROM eoi_report WHERE id = ?",
                String.class, id)).isNotEqualTo("APPROVED");
    }

    @Test
    @DisplayName("an approved report cannot be submitted or approved again")
    void approvedReport_cannotMoveAgain() {
        long id = firstReportId();
        workflow.submit(id, "maker01");
        workflow.approve(id, "checker01");

        assertThatThrownBy(() -> workflow.submit(id, "maker02"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> workflow.approve(id, "checker02"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("a currency row of another report is not deleted through this report's URL")
    void deleteThroughTheWrongReport_deletesNothing() {
        long owner = firstReportId();
        long other = jdbc.queryForObject(
                "SELECT MIN(id) FROM eoi_report WHERE id <> ?", Long.class, owner);
        jdbc.update("INSERT INTO eoi_currency_position (id, report_id, currency, short_long, "
                + "short_short, long_long, long_short) VALUES (eoi_seq.NEXTVAL, ?, 'XTS', 0, 0, 0, 0)", owner);
        long row = jdbc.queryForObject("SELECT MAX(id) FROM eoi_currency_position", Long.class);

        new ReportExposureRepository(jdbc).deleteCurrency(other, row);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM eoi_currency_position WHERE id = ?",
                Integer.class, row)).isEqualTo(1);
    }

    private MockHttpServletResponse post(String uri) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setAttribute(org.springframework.web.servlet.DispatcherServlet.FLASH_MAP_MANAGER_ATTRIBUTE,
                new org.springframework.web.servlet.support.SessionFlashMapManager());
        new ReportLockInterceptor(jdbc).preHandle(request, response, new Object());
        return response;
    }

    private long firstReportId() {
        return jdbc.queryForObject("SELECT MIN(id) FROM eoi_report WHERE status IN ('DUE','DRAFT')",
                Long.class);
    }
}
