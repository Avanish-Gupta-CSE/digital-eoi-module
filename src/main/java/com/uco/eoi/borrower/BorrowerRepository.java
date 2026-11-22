package com.uco.eoi.borrower;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * All SQL for borrowers lives here, in native SQL that runs on H2 and Oracle 19c.
 * No ORM, no query builder: a beginner must be able to read the query.
 */
@Repository
public class BorrowerRepository {

    private static final String FIND_BY_QUARTER = """
            SELECT id, borrower_name, consortium_name, sanctioned_limit,
                   outstanding, report_quarter, status
              FROM eoi_borrower
             WHERE report_quarter = ?
             ORDER BY borrower_name
            """;

    private final JdbcTemplate jdbc;

    public BorrowerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Borrower> findByQuarter(String quarter) {
        return jdbc.query(FIND_BY_QUARTER, (rs, rowNumber) -> new Borrower(
                rs.getLong("id"),
                rs.getString("borrower_name"),
                rs.getString("consortium_name"),
                rs.getBigDecimal("sanctioned_limit"),
                rs.getBigDecimal("outstanding"),
                rs.getString("report_quarter"),
                rs.getString("status")), quarter);
    }
}
