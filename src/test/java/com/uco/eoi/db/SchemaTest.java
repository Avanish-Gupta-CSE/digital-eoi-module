package com.uco.eoi.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Proves the Annex II schema actually runs, and that the database itself prevents two
 * reports for the same borrower and quarter.
 *
 * Plain JDBC, no Spring context: this test is about the SQL, not about wiring.
 */
class SchemaTest {

    private static final List<String> SCHEMA_FILES = List.of(
            "db/schema.sql",
            "db/schema-annexii-exposure.sql",
            "db/schema-annexii-experience.sql",
            "db/schema-audit.sql");

    private static final String[] EXPECTED_TABLES = {
            "eoi_borrower", "eoi_report", "eoi_borrower_director", "eoi_borrower_allied",
            "eoi_borrower_bank", "eoi_facility", "eoi_facility_security", "eoi_derivative",
            "eoi_currency_position", "eoi_report_experience", "eoi_report_rubric"};

    private Connection connection;

    @BeforeEach
    void loadSchema() throws Exception {
        // A fresh in-memory database per test, named uniquely so repeat runs cannot collide.
        String url = "jdbc:h2:mem:schematest_" + System.nanoTime() + ";MODE=Oracle";
        connection = DriverManager.getConnection(url, "sa", "");
        for (String file : SCHEMA_FILES) {
            try (InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
        }
    }

    @AfterEach
    void close() throws SQLException {
        connection.close();
    }

    @Test
    @DisplayName("all eleven Annex II tables are created by the three schema files")
    void schemaCreatesEveryTable() throws SQLException {
        for (String table : EXPECTED_TABLES) {
            assertThat(tableExists(table)).as("table %s", table).isTrue();
        }
    }

    @Test
    @DisplayName("a second report for the same borrower and quarter is rejected by the database")
    void rejectsDuplicateReportForSameBorrowerAndQuarter() throws SQLException {
        long borrowerId = insertBorrower("Sample Duplicate Ltd");
        insertReport(borrowerId, "Q3FY2627");

        assertThatThrownBy(() -> insertReport(borrowerId, "Q3FY2627"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("eoi_derivative keeps the NPA amount and the restructured notional in separate columns")
    void derivativeHasSplitNpaAndRestructuredColumns() throws SQLException {
        assertThat(columnNames("eoi_derivative"))
                .contains("NPA_AMOUNT", "RESTRUCTURED_NOTIONAL_AMOUNT")
                .doesNotContain("NPA_RESTRUCTURED_AMOUNT");
    }

    private boolean tableExists(String table) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = 'PUBLIC' AND table_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table.toUpperCase());
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1) == 1;
            }
        }
    }

    private List<String> columnNames(String table) throws SQLException {
        String sql = "SELECT column_name FROM information_schema.columns "
                + "WHERE table_schema = 'PUBLIC' AND table_name = ?";
        List<String> names = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table.toUpperCase());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    names.add(rows.getString(1));
                }
            }
        }
        return names;
    }

    private long insertBorrower(String name) throws SQLException {
        String sql = "INSERT INTO eoi_borrower (id, borrower_name, address, constitution, "
                + "business_activity_main, consortium_name) "
                + "VALUES (eoi_seq.NEXTVAL, ?, '1 Test Road', 'Private Limited Company', "
                + "'Testing', 'Test Consortium')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT MAX(id) FROM eoi_borrower");
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getLong(1);
        }
    }

    private void insertReport(long borrowerId, String quarter) throws SQLException {
        String sql = "INSERT INTO eoi_report (id, borrower_id, report_quarter, status) "
                + "VALUES (eoi_seq.NEXTVAL, ?, ?, 'DUE')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, borrowerId);
            statement.setString(2, quarter);
            statement.executeUpdate();
        }
    }
}
