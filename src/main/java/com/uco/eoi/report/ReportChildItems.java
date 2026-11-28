package com.uco.eoi.report;

import java.util.List;

/**
 * The child tables of one Annex II report, as the entry screen sees them.
 *
 * Every amount is already a string, because the money formatter runs once in the
 * repository and the screen only prints what it is given. Dates are text in
 * dd-MM-yyyy, the same shape the rest of the report uses.
 */
public final class ReportChildItems {

    private ReportChildItems() {
    }

    /** Everything the child forms of one report hold, ready for the screen. */
    public record EditChildren(List<DirectorItem> directors, List<TextItem> allied,
            List<TextItem> banks, List<FacilityItem> facilities,
            List<DerivativeItem> derivatives, List<CurrencyItem> currencies,
            List<ExperienceItem> experience) {
    }

    /** Part I.III: a director or partner, with the net worth the bank asks for. */
    public record DirectorItem(long id, String name, String designation, String netWorthText) {
    }

    /** One line of free text: an allied activity or the name of another bank. */
    public record TextItem(long id, String text) {
    }

    /** Part III: one credit facility, with the security offered against it. */
    public record FacilityItem(long id, String facilityType, String purpose,
            String facilityDateText, String sanctionedText, String outstandingText,
            String repaymentTerms, String defaultsCommitments, String securityText) {
    }

    /** Part IV: one derivative contract. */
    public record DerivativeItem(long id, int serialNo, String nature, String notionalText,
            String maturityText, String positiveMtmText, String npaText,
            String restructuredText, String reasons) {
    }

    /** Part V: one currency, with the four position buckets. */
    public record CurrencyItem(long id, String currency, String shortLongText,
            String shortShortText, String longLongText, String longShortText) {
    }

    /** Part VI.A: one graded experience item. */
    public record ExperienceItem(long id, String itemCode, String grade) {
    }

    /** Part VI.B: one rubric parameter observation, as it is written to the table. */
    public record RubricEntry(String itemCode, int paramNo, String observedValue, String grade) {
    }

    /** Part VI.B: the two derivative shares the rubric asks for, as percentages. */
    public record DerivativeShares(double stillNpaPercent, double restructuredPercent) {
    }
}
