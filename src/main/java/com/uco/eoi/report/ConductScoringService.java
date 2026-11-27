package com.uco.eoi.report;

import org.springframework.stereotype.Service;

/**
 * Grades the Part VI conduct rubric of Annex II (pp. 50-51 of RBI/DOR/2025-26/159).
 *
 * Every rule is a pure function on one observed number, so each band can be proven on its
 * own. The bands are quoted in the comment of each method, in the words the annex prints.
 * The one rule that overrides a computed grade is the NPA rule the annex prints inside the
 * rubric itself.
 */
@Service
public class ConductScoringService {

    public static final String GRADE_GOOD = "Good";
    public static final String GRADE_SATISFACTORY = "Satisfactory";
    public static final String GRADE_BELOW_PAR = "Below Par";

    /**
     * Rubric row 1, conduct of funded facilities: over-drawings (no. of times).
     * The annex prints "Upto 4 times / 5 to 6 times / Above 6 times".
     */
    public String gradeFundedOverdrawings(int overdrawingCount) {
        return gradeWithinLimits(overdrawingCount, 4, 6);
    }

    /**
     * Rubric row 2, conduct of funded facilities: average period of adjustment.
     * The annex prints "Within 1 month / Within 2 months / Beyond 2 months".
     */
    public String gradeFundedAdjustmentMonths(int adjustmentMonths) {
        return gradeWithinLimits(adjustmentMonths, 1, 2);
    }

    /**
     * Rubric row 3, conduct of funded facilities: extent of over-drawings as a percentage
     * of the limit. The annex prints "Upto 10% / 10 to 20% / Above 20%".
     */
    public String gradeFundedOverdrawingPercent(double percentOfLimit) {
        return gradeWithinLimits(percentOfLimit, 10, 20);
    }

    /**
     * Rubric row 4, conduct of contingent facilities: no. of defaults.
     * The annex prints "Upto 2 times / 3 to 4 times / Above 4 times".
     */
    public String gradeContingentDefaults(int defaultCount) {
        return gradeWithinLimits(defaultCount, 2, 4);
    }

    /**
     * Rubric row 5, conduct of contingent facilities: average period of adjustment.
     * The annex prints "Within 1 week / Within 2 weeks / Beyond 2 weeks".
     */
    public String gradeContingentAdjustmentWeeks(int adjustmentWeeks) {
        return gradeWithinLimits(adjustmentWeeks, 1, 2);
    }

    /**
     * Rubric row 6, conduct of derivatives: contracts whose positive MTM due to the bank
     * stayed overdue for more than 30 days. The annex prints
     * "less than 25% / 25-50% / more than 50%".
     */
    public String gradeMtmOverdueOver30DaysPercent(double percentOfContracts) {
        return gradeBelowLimit(percentOfContracts, 25, 50);
    }

    /**
     * Rubric row 7, conduct of derivatives: contracts whose positive MTM stayed overdue for
     * more than 90 days and was still NPA on the exchange date. The annex prints
     * "less than 1% / 1-5% / more than 5%".
     */
    public String gradeMtmOverdueOver90DaysPercent(double percentOfContracts) {
        return gradeBelowLimit(percentOfContracts, 1, 5);
    }

    /**
     * Rubric row 8, conduct of derivatives: contracts restructured during the period.
     * The annex prints "less than 25% / 25-50% / more than 50%".
     */
    public String gradeRestructuredContractsPercent(double percentOfContracts) {
        return gradeBelowLimit(percentOfContracts, 25, 50);
    }

    /**
     * Rubric row 9, company's internal systems and procedures: stock statement and
     * financial data. The annex prints "Timely / Delay upto 15 days / Delay over 15 days".
     */
    public String gradeStockStatementDelay(int delayDays) {
        return gradeDelayDays(delayDays, 15);
    }

    /**
     * Rubric row 10, company's internal systems and procedures: creation of charge.
     * The annex prints "Prompt / Delay upto 2 months / Delay over 2 months"; the delay is
     * counted in days here, so two months is the 60th day.
     */
    public String gradeCreationOfChargeDelay(int delayDays) {
        return gradeDelayDays(delayDays, 60);
    }

    /**
     * The rule the annex prints inside the rubric: a contract classified NPA and still NPA
     * on the date of the exchange of information is shown as Below Par, whatever was computed.
     */
    public String applyNpaOverride(String computedGrade, boolean hasActiveNpaContract) {
        if (hasActiveNpaContract) {
            return GRADE_BELOW_PAR;
        }
        return computedGrade;
    }

    /** Bands printed as "up to" or "within": Good up to the first limit, Satisfactory up to the second. */
    private String gradeWithinLimits(double value, double goodLimit, double satisfactoryLimit) {
        if (value <= goodLimit) {
            return GRADE_GOOD;
        }
        if (value <= satisfactoryLimit) {
            return GRADE_SATISFACTORY;
        }
        return GRADE_BELOW_PAR;
    }

    /** Bands printed as "less than": Good below the first limit, Satisfactory up to the second. */
    private String gradeBelowLimit(double value, double goodLimit, double satisfactoryLimit) {
        if (value < goodLimit) {
            return GRADE_GOOD;
        }
        if (value <= satisfactoryLimit) {
            return GRADE_SATISFACTORY;
        }
        return GRADE_BELOW_PAR;
    }

    /** "Prompt" and "timely" mean no delay at all; a delay up to the limit is only Satisfactory. */
    private String gradeDelayDays(int delayDays, int satisfactoryLimitDays) {
        if (delayDays <= 0) {
            return GRADE_GOOD;
        }
        if (delayDays <= satisfactoryLimitDays) {
            return GRADE_SATISFACTORY;
        }
        return GRADE_BELOW_PAR;
    }
}
