package com.uco.eoi.report;

import com.uco.eoi.common.IndianMoney;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The exposure children of Annex II: facilities with their security (Part III), derivative
 * contracts (Part IV) and currency positions (Part V), in native SQL that runs unchanged on
 * H2 and on Oracle 19c. Amounts are formatted here, so the screen only prints what it is given.
 */
@Repository
public class ReportExposureRepository {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final String FIND_FACILITIES =
            "SELECT id, facility_type, purpose, facility_date, sanctioned_amount, "
            + "outstanding_amount, repayment_terms, defaults_term_commitments "
            + "FROM eoi_facility WHERE report_id = ? ORDER BY id";

    private static final String FIND_SECURITY =
            "SELECT security_description FROM eoi_facility_security WHERE facility_id = ? ORDER BY id";

    private static final String FIND_DERIVATIVES =
            "SELECT id, serial_no, nature, notional_amount, weighted_avg_maturity, positive_mtm, "
            + "npa_amount, restructured_notional_amount, restructuring_reasons "
            + "FROM eoi_derivative WHERE report_id = ? ORDER BY id";

    private static final String FIND_CURRENCIES =
            "SELECT id, currency, short_long, short_short, long_long, long_short "
            + "FROM eoi_currency_position WHERE report_id = ? ORDER BY id";

    private static final String INSERT_FACILITY =
            "INSERT INTO eoi_facility (id, report_id, facility_type, purpose, facility_date, "
            + "sanctioned_amount, outstanding_amount, repayment_terms, defaults_term_commitments) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_SECURITY =
            "INSERT INTO eoi_facility_security (id, facility_id, security_description) "
            + "VALUES (?, ?, ?)";

    private static final String INSERT_DERIVATIVE =
            "INSERT INTO eoi_derivative (id, report_id, serial_no, nature, notional_amount, "
            + "weighted_avg_maturity, positive_mtm, npa_amount, restructured_notional_amount, "
            + "restructuring_reasons) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_CURRENCY =
            "INSERT INTO eoi_currency_position (id, report_id, currency, short_long, short_short, "
            + "long_long, long_short) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String NEXT_ID = "SELECT eoi_seq.NEXTVAL FROM DUAL";

    private final JdbcTemplate jdbc;

    public ReportExposureRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Adds one credit facility, with its security when the officer typed one: an empty
     * security row would print as a blank line on the report.
     */
    @Transactional
    public void insertFacility(long reportId, String facilityType, String purpose,
            String facilityDateText, String sanctionedText, String outstandingText,
            String repaymentTerms, String defaultsCommitments, String securityText) {
        long facilityId = nextId();
        jdbc.update(INSERT_FACILITY, facilityId, reportId, facilityType, purpose,
                date(facilityDateText), amount(sanctionedText), amount(outstandingText),
                repaymentTerms, defaultsCommitments);
        if (securityText != null && !securityText.isBlank()) {
            jdbc.update(INSERT_SECURITY, nextId(), facilityId, securityText);
        }
    }

    /** Removes one facility. The security goes first: it points at the facility. */
    @Transactional
    public void deleteFacility(long rowId) {
        jdbc.update("DELETE FROM eoi_facility_security WHERE facility_id = ?", rowId);
        jdbc.update("DELETE FROM eoi_facility WHERE id = ?", rowId);
    }

    /** Adds one derivative contract to the report. */
    public void insertDerivative(long reportId, int serialNo, String nature, String notionalText,
            String maturityText, String positiveMtmText, String npaText, String restructuredText,
            String reasons) {
        jdbc.update(INSERT_DERIVATIVE, nextId(), reportId, serialNo, nature,
                amount(notionalText), amount(maturityText), amount(positiveMtmText),
                amount(npaText), amount(restructuredText), reasons);
    }

    /** Removes one derivative contract. */
    public void deleteDerivative(long rowId) {
        jdbc.update("DELETE FROM eoi_derivative WHERE id = ?", rowId);
    }

    /** Adds one currency position to the report. */
    public void insertCurrency(long reportId, String currency, String shortLongText,
            String shortShortText, String longLongText, String longShortText) {
        jdbc.update(INSERT_CURRENCY, nextId(), reportId, currency, amount(shortLongText),
                amount(shortShortText), amount(longLongText), amount(longShortText));
    }

    /** Removes one currency position. */
    public void deleteCurrency(long rowId) {
        jdbc.update("DELETE FROM eoi_currency_position WHERE id = ?", rowId);
    }

    /** The facilities of one report, each with the security offered against it. */
    public List<ReportChildItems.FacilityItem> findFacilities(long reportId) {
        List<ReportChildItems.FacilityItem> facilities = new ArrayList<>();
        for (FacilityWithSecurity facility : jdbc.query(FIND_FACILITIES, (rs, rowNumber) ->
                new FacilityWithSecurity(rs.getLong("id"), rs.getString("facility_type"),
                        rs.getString("purpose"), rs.getDate("facility_date"),
                        rs.getBigDecimal("sanctioned_amount"), rs.getBigDecimal("outstanding_amount"),
                        rs.getString("repayment_terms"), rs.getString("defaults_term_commitments")),
                reportId)) {
            facilities.add(new ReportChildItems.FacilityItem(facility.id(), facility.type(),
                    facility.purpose(), dateText(facility.date()),
                    IndianMoney.format(facility.sanctioned()), IndianMoney.format(facility.outstanding()),
                    facility.repaymentTerms(), facility.defaults(),
                    String.join("; ", findSecurity(facility.id()))));
        }
        return facilities;
    }

    private List<String> findSecurity(long facilityId) {
        return jdbc.query(FIND_SECURITY,
                (rs, rowNumber) -> rs.getString("security_description"), facilityId);
    }

    /** The derivative contracts of one report, ready for the entry screen. */
    public List<ReportChildItems.DerivativeItem> findDerivatives(long reportId) {
        return jdbc.query(FIND_DERIVATIVES, (rs, rowNumber) -> new ReportChildItems.DerivativeItem(
                rs.getLong("id"), rs.getInt("serial_no"), rs.getString("nature"),
                IndianMoney.format(rs.getBigDecimal("notional_amount")),
                numberText(rs.getBigDecimal("weighted_avg_maturity")),
                IndianMoney.format(rs.getBigDecimal("positive_mtm")),
                IndianMoney.format(rs.getBigDecimal("npa_amount")),
                IndianMoney.format(rs.getBigDecimal("restructured_notional_amount")),
                rs.getString("restructuring_reasons")), reportId);
    }

    /** The currency positions of one report, ready for the entry screen. */
    public List<ReportChildItems.CurrencyItem> findCurrencies(long reportId) {
        return jdbc.query(FIND_CURRENCIES, (rs, rowNumber) -> new ReportChildItems.CurrencyItem(
                rs.getLong("id"), rs.getString("currency"),
                numberText(rs.getBigDecimal("short_long")), numberText(rs.getBigDecimal("short_short")),
                numberText(rs.getBigDecimal("long_long")), numberText(rs.getBigDecimal("long_short"))),
                reportId);
    }

    /** The next id of the shared sequence, handed out the same way on H2 and Oracle. */
    private long nextId() {
        Long id = jdbc.queryForObject(NEXT_ID, Long.class);
        return id == null ? 0L : id;
    }

    /** A typed amount, or null when the officer left the box empty. */
    private static BigDecimal amount(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return new BigDecimal(text.replace(",", "").trim());
    }

    /** A typed date (dd-MM-yyyy, yyyy-MM-dd, or dd/MM/yyyy), or null when blank. */
    private static Date date(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String clean = text.trim();
        for (DateTimeFormatter fmt : List.of(DATE_FORMAT, DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"))) {
            try {
                return Date.valueOf(LocalDate.parse(clean, fmt));
            } catch (java.time.format.DateTimeParseException ignored) {
                // try next accepted format
            }
        }
        return Date.valueOf(LocalDate.parse(clean, DATE_FORMAT));
    }

    /** A date is printed as dd-MM-yyyy; a missing date as empty text. */
    private static String dateText(Date date) {
        return date == null ? "" : date.toLocalDate().format(DATE_FORMAT);
    }

    /** A missing number is printed as empty text, never as the word null. */
    private static String numberText(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    /** One facility as it sits in the table, before its security is attached to it. */
    private record FacilityWithSecurity(long id, String type, String purpose, Date date,
            BigDecimal sanctioned, BigDecimal outstanding, String repaymentTerms, String defaults) {
    }
}
