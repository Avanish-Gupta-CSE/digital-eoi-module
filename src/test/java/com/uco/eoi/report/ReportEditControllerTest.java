package com.uco.eoi.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * The rules of the entry screen: the required fields are checked, a good entry is saved,
 * the previous quarter can be copied in, and the report can be taken away as CSV.
 *
 * The test runs the same H2 scripts as the h2 profile, in Oracle mode, so the SQL that
 * passes here is the SQL that runs on the bank's database.
 */
class ReportEditControllerTest {

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
        String url = "jdbc:h2:mem:reporteditcontrollertest_" + System.nanoTime() + ";MODE=Oracle";
        connection = DriverManager.getConnection(url, "sa", "");
        for (String file : SCRIPT_FILES) {
            try (InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
        }
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        ReportService service = new ReportService(new ReportRepository(jdbc));
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReportEditController(service, new ReportWriteRepository(jdbc)))
                .build();
    }

    @AfterEach
    void close() throws Exception {
        connection.close();
    }

    @Test
    @DisplayName("a blank address comes back to the entry screen with the field marked")
    void blankAddress_isRejected_andTheEntryScreenComesBack() throws Exception {
        mockMvc.perform(post("/reports/{id}/edit", steelsReportId())
                        .param("address", "")
                        .param("constitution", "Private Limited Company")
                        .param("mainActivity", "Steel manufacturing"))
                .andExpect(view().name("report-edit"))
                .andExpect(model().attributeHasFieldErrors("form", "address"));
    }

    @Test
    @DisplayName("a good entry is saved on the borrower and the report goes back to DRAFT")
    void goodEntry_isSaved_andTheReportGoesBackToDraft() throws Exception {
        mockMvc.perform(post("/reports/{id}/edit", steelsReportId())
                        .param("address", "9 New Sample Road, Kolkata 700009")
                        .param("constitution", "Private Limited Company")
                        .param("mainActivity", "Steel manufacturing")
                        .param("internalRating", "AA")
                        .param("annualReportAsOn", "2026-03-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports/" + steelsReportId() + "?saved=1"));

        assertThat(text("SELECT b.address FROM eoi_borrower b "
                + "JOIN eoi_report r ON r.borrower_id = b.id WHERE r.id = ?", steelsReportId()))
                .isEqualTo("9 New Sample Road, Kolkata 700009");
        assertThat(text("SELECT internal_rating FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("AA");
        assertThat(jdbc.queryForObject("SELECT annual_report_as_on FROM eoi_report WHERE id = ?",
                Date.class, steelsReportId())).isEqualTo(Date.valueOf("2026-03-31"));
        assertThat(text("SELECT status FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("the CSV export carries the borrower name and the facilities")
    void export_writesTheCsv_withTheBorrowerName() throws Exception {
        MvcResult result = mockMvc.perform(get("/reports/{id}/export.csv", steelsReportId()))
                .andExpect(status().isOk())
                .andReturn();

        String csv = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).contains("Sample Steels Ltd");
        assertThat(csv).contains("Term Loan");
        assertThat(result.getResponse().getContentType()).contains("text/csv");
        assertThat(result.getResponse().getHeader("Content-Disposition"))
                .contains("annex-ii-report-" + steelsReportId() + ".csv");
    }

    @Test
    @DisplayName("prefill copies the approved quarter and never doubles the facilities")
    void prefill_copiesTheApprovedQuarter_once() throws Exception {
        insertApprovedQuarter();
        clearFacilities();

        mockMvc.perform(post("/reports/{id}/prefill", steelsReportId()))
                .andExpect(redirectedUrl("/reports/" + steelsReportId() + "/edit?prefilled=1"));

        assertThat(text("SELECT internal_rating FROM eoi_report WHERE id = ?", steelsReportId()))
                .isEqualTo("BBB");
        assertThat(count("SELECT COUNT(*) FROM eoi_facility WHERE report_id = ?", steelsReportId()))
                .isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM eoi_facility_security "
                + "WHERE facility_id IN (SELECT id FROM eoi_facility WHERE report_id = ?)",
                steelsReportId())).isEqualTo(1);

        // A second prefill must not copy the facilities again.
        mockMvc.perform(post("/reports/{id}/prefill", steelsReportId()))
                .andExpect(redirectedUrl("/reports/" + steelsReportId() + "/edit?prefilled=1"));
        assertThat(count("SELECT COUNT(*) FROM eoi_facility WHERE report_id = ?", steelsReportId()))
                .isEqualTo(1);
    }

    /** The report id of the seeded borrower Sample Steels Ltd for the current quarter. */
    private long steelsReportId() {
        return jdbc.queryForObject(
                "SELECT r.id FROM eoi_report r JOIN eoi_borrower b ON b.id = r.borrower_id "
                        + "WHERE b.borrower_name = 'Sample Steels Ltd' "
                        + "AND r.report_quarter = 'Q3FY2627'",
                Long.class);
    }

    private String text(String sql, Object... args) {
        return jdbc.queryForObject(sql, String.class, args);
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    /** An approved report of the previous quarter, with one facility, to prefill from. */
    private void insertApprovedQuarter() {
        jdbc.update("""
                INSERT INTO eoi_report
                    (id, borrower_id, report_quarter, status, reported_on, irac_classification,
                     internal_rating, internal_rating_narration, external_rating,
                     annual_report_latest, annual_report_as_on, changes_shareholding,
                     special_information)
                SELECT eoi_seq.NEXTVAL, b.id, 'Q2FY2627', 'APPROVED', DATE '2026-07-10',
                       'Standard', 'BBB', 'Conduct satisfactory in the previous quarter.',
                       'CRISIL BBB (stable)', 'Annual Report FY2025-26', DATE '2025-03-31',
                       'No change in shareholding', 'None'
                  FROM eoi_borrower b
                 WHERE b.borrower_name = 'Sample Steels Ltd'
                """);
        jdbc.update("""
                INSERT INTO eoi_facility
                    (id, report_id, facility_type, purpose, facility_date, sanctioned_amount,
                     outstanding_amount, repayment_terms, defaults_term_commitments)
                SELECT eoi_seq.NEXTVAL, r.id, 'Term Loan', 'Capacity expansion',
                       DATE '2024-06-01', 250000000.00, 182500000.00,
                       'Monthly instalment', 'None'
                  FROM eoi_report r JOIN eoi_borrower b ON b.id = r.borrower_id
                 WHERE b.borrower_name = 'Sample Steels Ltd' AND r.report_quarter = 'Q2FY2627'
                """);
        jdbc.update("""
                INSERT INTO eoi_facility_security (id, facility_id, security_description)
                SELECT eoi_seq.NEXTVAL, f.id, 'Primary security on factory land'
                  FROM eoi_facility f JOIN eoi_report r ON r.id = f.report_id
                 WHERE r.report_quarter = 'Q2FY2627'
                """);
    }

    /** The maker's own facilities are removed, so prefill has room to copy into. */
    private void clearFacilities() {
        jdbc.update("DELETE FROM eoi_facility_security WHERE facility_id IN "
                + "(SELECT id FROM eoi_facility WHERE report_id = ?)", steelsReportId());
        jdbc.update("DELETE FROM eoi_facility WHERE report_id = ?", steelsReportId());
    }
}
