package com.uco.eoi.report;

import java.math.BigDecimal;
import java.util.List;

/**
 * The Annex II report of one borrower and quarter, in the order the annex prints it.
 *
 * Rupee amounts are already formatted with IndianMoney, so this record holds text only and
 * no logic. The Part V currency amounts stay plain numbers: the annex prints them in foreign
 * currency units and that unit is still an open question (G22), so no rupee sign is added.
 */
public record ReportView(
        long reportId,
        String quarter,
        String status,
        BorrowerPart borrower,
        ReportPart report,
        List<DirectorRow> directors,
        List<String> alliedActivities,
        List<String> otherBanks,
        List<FacilityRow> facilities,
        List<DerivativeRow> derivatives,
        List<CurrencyRow> currencyPositions,
        List<ExperienceRow> experience,
        List<RubricRow> rubric) {
}

/** Part I: the static identity of the borrower. */
record BorrowerPart(
        String name,
        String address,
        String constitution,
        String mainActivity,
        String groupAffiliation,
        String associateConcerns,
        String consortiumName) {
}

/** Part II: the credit quality indicators of one report, plus the two carry-over items. */
record ReportPart(
        String iracClassification,
        String internalRating,
        String internalRatingNarration,
        String externalRating,
        String annualReportLatest,
        String annualReportAsOn,
        String changesShareholding,
        String specialInformation) {
}

/** The report header: which report it is, whose it is, and Parts I and II. */
record ReportHeader(
        long reportId,
        long borrowerId,
        String quarter,
        String status,
        BorrowerPart borrower,
        ReportPart report) {
}

/** Part I.III and I.VI: one director or partner, with the net worth the bank asks for. */
record DirectorRow(String name, String designation, String netWorthText) {
}

/** Part III: one credit facility, with the security offered against it (III.VII). */
record FacilityRow(
        String type,
        String purpose,
        String facilityDateText,
        String sanctionedText,
        String outstandingText,
        String repaymentTerms,
        String defaults,
        List<String> securities) {
}

/** One facility as it sits in the table, before its securities are attached to it. */
record FacilityWithId(
        long facilityId,
        String type,
        String purpose,
        String dateText,
        String sanctionedText,
        String outstandingText,
        String repaymentTerms,
        String defaults) {
}

/** Part IV: one derivative contract. NPA amount and restructured notional are two columns. */
record DerivativeRow(
        String serialNo,
        String nature,
        String notionalText,
        String maturityText,
        String positiveMtmText,
        String npaText,
        String restructuredText,
        String reasons) {
}

/** Part V as stored: the four position buckets of one currency, before any netting. */
record RawCurrency(
        String currency,
        BigDecimal shortLong,
        BigDecimal shortShort,
        BigDecimal longLong,
        BigDecimal longShort) {
}

/** Part V as printed: the four buckets plus the three nets computed from them. */
record CurrencyRow(
        String currency,
        BigDecimal shortLong,
        BigDecimal shortShort,
        BigDecimal shortNet,
        BigDecimal longLong,
        BigDecimal longShort,
        BigDecimal longNet,
        BigDecimal overallNet) {
}

/** Part VI.A: one graded experience item. */
record ExperienceRow(String itemCode, String grade) {
}

/** Part VI.B: one rubric parameter observation. */
record RubricRow(String itemCode, int paramNo, String observedValue, String grade) {
}
