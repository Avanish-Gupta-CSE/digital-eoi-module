package com.uco.eoi.auth;

import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Reads the officer's details from the bank's HRMS schema.
 *
 * Two tables are used: employee_contact_details for the name and the post,
 * employee_tenure for the scale code that is valid today. The schema name comes
 * from the property uco.sso.hrms-schema; when it is empty the lookup returns
 * null, because a laptop has no HRMS.
 */
@Service
public class HrmsLookupService {

    /** The scales that belong to a checker. Every other scale is a maker. */
    private static final List<String> CHECKER_SCALES = List.of("IV", "V", "VI", "VII");

    /** The SOL ids that belong to a checker, whatever the scale says. */
    private static final List<String> CHECKER_SOL_IDS = List.of("7115", "7103");

    private final JdbcTemplate jdbc;
    private final String schema;

    public HrmsLookupService(JdbcTemplate jdbc,
                              @Value("${uco.sso.hrms-schema:HRMS_DBA}") String schema) {
        this.jdbc = jdbc;
        this.schema = schema == null ? "" : schema.trim();
    }

    /**
     * The officer with this employee number, or null when HRMS has no row.
     */
    public StaffPrincipal findByEmployeeNumber(String empNo) {
        if (schema.isEmpty() || empNo == null || empNo.isBlank()) {
            return null;
        }
        String sql = "SELECT e.EMPNO, e.NAME, e.POSTING_SOL_ID, e.SCALE_CODE, e.DESIG "
                + "FROM " + schema + ".employee_contact_details e "
                + "WHERE e.EMPNO = ?";
        List<StaffPrincipal> found = jdbc.query(sql, this::toPrincipal, empNo);
        if (found.isEmpty()) {
            return null;
        }
        StaffPrincipal contact = found.get(0);
        return withTenure(contact);
    }

    /** Reads the scale code that is valid today, and fixes the role from it. */
    private StaffPrincipal withTenure(StaffPrincipal contact) {
        String sql = "SELECT t.SCALE_CODE FROM " + schema + ".employee_tenure t "
                + "WHERE t.EMPNO = ? "
                + "AND SYSDATE BETWEEN t.EFF_FROM_DATE AND t.EFF_TO_DATE "
                + "AND t.STATUS_FLAG = 'V'";
        List<String> scales = jdbc.query(sql,
                (rs, row) -> rs.getString(1), contact.empNo());
        String scale = scales.isEmpty() ? contact.scaleCode() : scales.get(0);
        return new StaffPrincipal(contact.empNo(), contact.name(), contact.solId(),
                scale, contact.designation(), roleFor(scale, contact.solId()));
    }

    /** Builds a principal from one row of employee_contact_details. */
    private StaffPrincipal toPrincipal(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        String empNo = rs.getString("EMPNO");
        String name = rs.getString("NAME");
        String solId = rs.getString("POSTING_SOL_ID");
        String scaleCode = rs.getString("SCALE_CODE");
        String designation = rs.getString("DESIG");
        return new StaffPrincipal(empNo, name, solId, scaleCode, designation,
                roleFor(scaleCode, solId));
    }

    /** A checker scale or a checker SOL id means checker; everything else is a maker. */
    public String roleFor(String scaleCode, String solId) {
        String scale = scaleCode == null ? "" : scaleCode.trim().toUpperCase(Locale.ROOT);
        String sol = solId == null ? "" : solId.trim();
        if (CHECKER_SCALES.contains(scale) || CHECKER_SOL_IDS.contains(sol)) {
            return StaffPrincipal.CHECKER;
        }
        return StaffPrincipal.MAKER;
    }
}
