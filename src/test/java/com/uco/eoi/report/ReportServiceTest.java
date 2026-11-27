package com.uco.eoi.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.math.BigDecimal;
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
 * Proves the Part V arithmetic of Annex II (Decision 018) and that the whole report can be
 * read back from the seeded database.
 *
 * The arithmetic test calls the rule directly, with no Spring and no database. The second
 * test loads the real schema and the seed into an in-memory H2 in Oracle mode, exactly as
 * the h2 profile does, and reads the report through the repository.
 */
class ReportServiceTest {

    private static final List<String> SCRIPT_FILES = List.of(
            "db/schema.sql",
            "db/schema-annexii-exposure.sql",
            "db/schema-annexii-experience.sql",
            "db/schema-audit.sql",
            "db/h2-init.sql");

    private Connection connection;
    private JdbcTemplate jdbc;
    private ReportService service;

    @BeforeEach
    void loadSeededDatabase() throws Exception {
        // A fresh in-memory database per test, named uniquely so repeat runs cannot collide.
        String url = "jdbc:h2:mem:reportservicetest_" + System.nanoTime() + ";MODE=Oracle";
        connection = DriverManager.getConnection(url, "sa", "");
        for (String file : SCRIPT_FILES) {
            try (InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
        }
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        service = new ReportService(new ReportRepository(jdbc));
    }

    @AfterEach
    void close() throws Exception {
        connection.close();
    }

    @Test
    @DisplayName("Part V nets are short long minus short short, long long minus long short, "
            + "and overall I minus II")
    void netExposure_matchesAnnexArithmetic() {
        List<RawCurrency> raw = List.of(
                new RawCurrency("USD", new BigDecimal("100"), new BigDecimal("40"),
                        new BigDecimal("70"), new BigDecimal("10")),
                new RawCurrency("EUR", new BigDecimal("40"), new BigDecimal("100"),
                        new BigDecimal("80"), new BigDecimal("10")));

        List<CurrencyRow> rows = ReportService.netExposures(raw);

        assertThat(rows).hasSameSizeAs(raw);

        CurrencyRow usd = rows.get(0);
        assertThat(usd.shortNet()).isEqualByComparingTo("60");
        assertThat(usd.longNet()).isEqualByComparingTo("60");
        assertThat(usd.overallNet()).isEqualByComparingTo("0");

        CurrencyRow eur = rows.get(1);
        assertThat(eur.shortNet()).isEqualByComparingTo("-60");
        assertThat(eur.longNet()).isEqualByComparingTo("70");
        assertThat(eur.overallNet()).isEqualByComparingTo("-130");
    }

    @Test
    @DisplayName("the seeded report shows all six parts of the annex, and an unknown id gives null")
    void report_loadsAllSixPartsFromSeededH2_andReturnsNullWhenMissing() {
        ReportView view = service.report(steelsReportId());

        assertThat(view).isNotNull();
        assertThat(view.borrower().name()).isEqualTo("Sample Steels Ltd");
        assertThat(view.directors()).hasSize(1);
        assertThat(view.alliedActivities()).hasSize(1);
        assertThat(view.otherBanks()).hasSize(2);
        assertThat(view.report().iracClassification()).isEqualTo("Standard");
        assertThat(view.facilities()).hasSize(1);
        assertThat(view.facilities().get(0).securities()).hasSize(1);
        assertThat(view.derivatives()).hasSize(1);
        assertThat(view.currencyPositions()).hasSize(2);
        assertThat(view.experience()).hasSize(6);
        assertThat(view.rubric()).hasSize(3);

        assertThat(service.report(999999L)).isNull();
    }

    /** The report id of the seeded borrower Sample Steels Ltd. */
    private long steelsReportId() {
        return jdbc.queryForObject(
                "SELECT r.id FROM eoi_report r JOIN eoi_borrower b ON b.id = r.borrower_id "
                        + "WHERE b.borrower_name = ?",
                Long.class, "Sample Steels Ltd");
    }
}
