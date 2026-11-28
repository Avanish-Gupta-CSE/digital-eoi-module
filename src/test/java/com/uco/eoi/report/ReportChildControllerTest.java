package com.uco.eoi.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * The child forms: a director and a facility can be added and removed, and the Part VI
 * rubric is graded into ten rows, every row Below Par when a contract is still NPA.
 * The h2 profile scripts run in Oracle mode, as the other report tests do.
 */
class ReportChildControllerTest {

    private static final List<String> SCRIPT_FILES = List.of(
            "db/schema.sql",
            "db/schema-annexii-exposure.sql",
            "db/schema-annexii-experience.sql",
            "db/schema-audit.sql",
            "db/h2-init.sql");

    private Connection connection;
    private JdbcTemplate jdbc;
    private MockMvc mockMvc;

    @BeforeEach
    void loadSeededDatabase() throws Exception {
        // A fresh in-memory database per test, named uniquely so repeat runs cannot collide.
        String url = "jdbc:h2:mem:reportchildcontrollertest_" + System.nanoTime() + ";MODE=Oracle";
        connection = DriverManager.getConnection(url, "sa", "");
        for (String file : SCRIPT_FILES) {
            try (InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
        }
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        ReportExposureRepository exposureRepository = new ReportExposureRepository(jdbc);
        ReportChildWriteRepository childWriteRepository = new ReportChildWriteRepository(jdbc);
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new ReportChildController(new ReportChildRepository(jdbc, exposureRepository,
                                childWriteRepository), exposureRepository),
                        new ReportRubricController(childWriteRepository, new ConductScoringService()))
                .build();
    }

    @AfterEach
    void close() throws Exception {
        connection.close();
    }

    @Test
    @DisplayName("a director added on the child form is saved on the borrower")
    void director_isAdded_andTheEntryScreenComesBack() throws Exception {
        mockMvc.perform(post("/reports/{id}/directors/add", steelsReportId())
                        .param("name", "Sample Director Six")
                        .param("designation", "Director")
                        .param("netWorth", "15000000"))
                .andExpect(redirectedUrl(editUrl()));

        assertThat(count("SELECT COUNT(*) FROM eoi_borrower_director "
                + "WHERE borrower_id = ? AND person_name = ?", steelsBorrowerId(),
                "Sample Director Six")).isEqualTo(1);
    }

    @Test
    @DisplayName("a director removed on the child form is gone from the borrower")
    void director_isDeleted() throws Exception {
        long directorId = seededDirectorId();

        mockMvc.perform(post("/reports/{id}/directors/{rowId}/delete",
                        steelsReportId(), directorId))
                .andExpect(redirectedUrl(editUrl()));

        assertThat(count("SELECT COUNT(*) FROM eoi_borrower_director WHERE id = ?", directorId))
                .isZero();
    }

    @Test
    @DisplayName("a facility added on the child form is saved with its security")
    void facility_isAdded_withItsSecurity() throws Exception {
        mockMvc.perform(post("/reports/{id}/facilities/add", steelsReportId())
                        .param("facilityType", "Cash Credit")
                        .param("facilityDate", "01-07-2026")
                        .param("sanctionedAmount", "50000000")
                        .param("balanceOutstanding", "12000000")
                        .param("securityText", "Hypothecation of stock"))
                .andExpect(redirectedUrl(editUrl()));

        assertThat(count("SELECT COUNT(*) FROM eoi_facility "
                + "WHERE report_id = ? AND facility_type = 'Cash Credit'", steelsReportId()))
                .isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM eoi_facility_security "
                + "WHERE security_description = 'Hypothecation of stock' AND facility_id IN "
                + "(SELECT id FROM eoi_facility WHERE report_id = ?)", steelsReportId()))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a facility removed on the child form takes its security with it")
    void facility_isDeleted_withItsSecurity() throws Exception {
        long facilityId = seededFacilityId();

        mockMvc.perform(post("/reports/{id}/facilities/{rowId}/delete",
                        steelsReportId(), facilityId))
                .andExpect(redirectedUrl(editUrl()));

        assertThat(count("SELECT COUNT(*) FROM eoi_facility WHERE id = ?", facilityId)).isZero();
        assertThat(count("SELECT COUNT(*) FROM eoi_facility_security WHERE facility_id = ?",
                facilityId)).isZero();
    }

    @Test
    @DisplayName("the automatic grading writes the ten rubric rows of the report")
    void autoGrade_writesTheTenRubricRows() throws Exception {
        mockMvc.perform(post("/reports/{id}/rubric/auto-grade", steelsReportId())
                        .param("overdrawings", "2")
                        .param("fundedAdjMonths", "1")
                        .param("overdrawingPct", "8")
                        .param("contingentDefaults", "1")
                        .param("contingentAdjWeeks", "1")
                        .param("irregularities", "10")
                        .param("stockStmtDays", "0")
                        .param("turnoverPct", "0"))
                .andExpect(redirectedUrl(editUrl()));

        assertThat(count("SELECT COUNT(*) FROM eoi_report_rubric WHERE report_id = ?",
                steelsReportId())).isEqualTo(10);
        assertThat(jdbc.queryForObject("SELECT grade FROM eoi_report_rubric "
                + "WHERE report_id = ? AND param_no = 1", String.class, steelsReportId()))
                .isEqualTo("Good");
        // The derivative share of the report is read, not typed: one contract, none still NPA.
        assertThat(jdbc.queryForObject("SELECT observed_value FROM eoi_report_rubric "
                + "WHERE report_id = ? AND param_no = 7", String.class, steelsReportId()))
                .isEqualTo("0.0%");
    }

    @Test
    @DisplayName("a contract still NPA shows every rubric row as Below Par")
    void autoGrade_withAnNpaContract_showsEveryRowBelowPar() throws Exception {
        mockMvc.perform(post("/reports/{id}/rubric/auto-grade", steelsReportId())
                        .param("overdrawings", "2")
                        .param("isNpaAccount", "yes"))
                .andExpect(redirectedUrl(editUrl()));

        assertThat(count("SELECT COUNT(*) FROM eoi_report_rubric WHERE report_id = ?",
                steelsReportId())).isEqualTo(10);
        assertThat(count("SELECT COUNT(*) FROM eoi_report_rubric "
                + "WHERE report_id = ? AND grade = 'Below Par'", steelsReportId())).isEqualTo(10);
    }

    /** The report id of the seeded borrower Sample Steels Ltd for the current quarter. */
    private long steelsReportId() {
        return jdbc.queryForObject(
                "SELECT r.id FROM eoi_report r JOIN eoi_borrower b ON b.id = r.borrower_id "
                        + "WHERE b.borrower_name = 'Sample Steels Ltd' "
                        + "AND r.report_quarter = 'Q3FY2627'",
                Long.class);
    }

    /** The borrower of that report, who owns the directors. */
    private long steelsBorrowerId() {
        return jdbc.queryForObject("SELECT borrower_id FROM eoi_report WHERE id = ?",
                Long.class, steelsReportId());
    }

    private long seededDirectorId() {
        return jdbc.queryForObject("SELECT id FROM eoi_borrower_director WHERE borrower_id = ? "
                + "AND person_name = 'Sample Director One'", Long.class, steelsBorrowerId());
    }

    private long seededFacilityId() {
        return jdbc.queryForObject("SELECT id FROM eoi_facility WHERE report_id = ? "
                + "AND facility_type = 'Term Loan'", Long.class, steelsReportId());
    }

    /** Every child form ends at the entry screen, with the "saved" flag showing. */
    private String editUrl() {
        return "/reports/" + steelsReportId() + "/edit?saved=1";
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }
}
