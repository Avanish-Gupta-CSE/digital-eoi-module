package com.uco.eoi.borrower;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * All SQL for the dashboard lives here, in native SQL that runs on H2 and Oracle 19c.
 *
 * Annex II puts the amounts on the FACILITIES, not on the report: one borrower can have
 * several facilities in the same quarter. So the screen's "sanctioned limit" and
 * "outstanding" are the sum of that report's facilities, never a stored total.
 */
@Repository
public class BorrowerRepository {

    private static final String FIND_BY_QUARTER = """
            SELECT b.borrower_name,
                   b.consortium_name,
                   COALESCE(SUM(f.sanctioned_amount), 0) AS sanctioned_limit,
                   COALESCE(SUM(f.outstanding_amount), 0) AS outstanding,
                   r.report_quarter,
                   r.status
              FROM eoi_report r
              JOIN eoi_borrower b ON b.id = r.borrower_id
              LEFT JOIN eoi_facility f ON f.report_id = r.id
             WHERE r.report_quarter = ?
             GROUP BY b.borrower_name, b.consortium_name, r.report_quarter, r.status
             ORDER BY b.borrower_name
            """;

    private final JdbcTemplate jdbc;

    public BorrowerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Borrower> findByQuarter(String quarter) {
        return jdbc.query(FIND_BY_QUARTER, (rs, rowNumber) -> new Borrower(
                rs.getString("borrower_name"),
                rs.getString("consortium_name"),
                rs.getBigDecimal("sanctioned_limit"),
                rs.getBigDecimal("outstanding"),
                rs.getString("report_quarter"),
                rs.getString("status")), quarter);
    }
}
