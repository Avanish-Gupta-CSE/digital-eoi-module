package com.uco.eoi.report;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Proves every band of the Part VI rubric (Annex II Part VI, pp. 50-51 of
 * RBI/DOR/2025-26/159) and the mandatory NPA rule printed inside it.
 *
 * No Spring and no database: the engine is a set of pure functions, so every boundary is
 * checked by calling it directly. The expected words are written out in full because the
 * three band names are what the bank sees on the report.
 */
class ConductScoringServiceTest {

    private final ConductScoringService service = new ConductScoringService();

    @Test
    @DisplayName("over-drawings: up to 4 times Good, 5 to 6 times Satisfactory, above 6 Below Par")
    void fundedOverdrawings_areBandedAsTheAnnexPrints() {
        assertThat(service.gradeFundedOverdrawings(0)).isEqualTo("Good");
        assertThat(service.gradeFundedOverdrawings(4)).isEqualTo("Good");
        assertThat(service.gradeFundedOverdrawings(5)).isEqualTo("Satisfactory");
        assertThat(service.gradeFundedOverdrawings(6)).isEqualTo("Satisfactory");
        assertThat(service.gradeFundedOverdrawings(7)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("funded adjustment: within 1 month Good, within 2 months Satisfactory, "
            + "beyond 2 months Below Par")
    void fundedAdjustmentMonths_areBandedAsTheAnnexPrints() {
        assertThat(service.gradeFundedAdjustmentMonths(1)).isEqualTo("Good");
        assertThat(service.gradeFundedAdjustmentMonths(2)).isEqualTo("Satisfactory");
        assertThat(service.gradeFundedAdjustmentMonths(3)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("extent of overdrawings: up to 10% Good, 10 to 20% Satisfactory, "
            + "above 20% Below Par")
    void fundedOverdrawingPercent_areBandedAsTheAnnexPrints() {
        assertThat(service.gradeFundedOverdrawingPercent(10)).isEqualTo("Good");
        assertThat(service.gradeFundedOverdrawingPercent(15)).isEqualTo("Satisfactory");
        assertThat(service.gradeFundedOverdrawingPercent(20)).isEqualTo("Satisfactory");
        assertThat(service.gradeFundedOverdrawingPercent(21)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("contingent defaults: up to 2 times Good, 3 to 4 times Satisfactory, "
            + "above 4 times Below Par")
    void contingentDefaults_areBandedAsTheAnnexPrints() {
        assertThat(service.gradeContingentDefaults(2)).isEqualTo("Good");
        assertThat(service.gradeContingentDefaults(3)).isEqualTo("Satisfactory");
        assertThat(service.gradeContingentDefaults(4)).isEqualTo("Satisfactory");
        assertThat(service.gradeContingentDefaults(5)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("contingent adjustment: within 1 week Good, within 2 weeks Satisfactory, "
            + "beyond 2 weeks Below Par")
    void contingentAdjustmentWeeks_areBandedAsTheAnnexPrints() {
        assertThat(service.gradeContingentAdjustmentWeeks(1)).isEqualTo("Good");
        assertThat(service.gradeContingentAdjustmentWeeks(2)).isEqualTo("Satisfactory");
        assertThat(service.gradeContingentAdjustmentWeeks(3)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("MTM overdue over 30 days: below 25% Good, 25 to 50% Satisfactory, "
            + "above 50% Below Par")
    void mtmOverdueOver30Days_areBandedAsTheAnnexPrints() {
        assertThat(service.gradeMtmOverdueOver30DaysPercent(24.99)).isEqualTo("Good");
        assertThat(service.gradeMtmOverdueOver30DaysPercent(25)).isEqualTo("Satisfactory");
        assertThat(service.gradeMtmOverdueOver30DaysPercent(50)).isEqualTo("Satisfactory");
        assertThat(service.gradeMtmOverdueOver30DaysPercent(50.01)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("MTM overdue over 90 days and regularised: below 1% Good, 1 to 5% "
            + "Satisfactory, above 5% Below Par")
    void mtmOverdueOver90Days_areBandedAsTheAnnexPrints() {
        assertThat(service.gradeMtmOverdueOver90DaysPercent(0.99)).isEqualTo("Good");
        assertThat(service.gradeMtmOverdueOver90DaysPercent(1)).isEqualTo("Satisfactory");
        assertThat(service.gradeMtmOverdueOver90DaysPercent(5)).isEqualTo("Satisfactory");
        assertThat(service.gradeMtmOverdueOver90DaysPercent(5.01)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("contracts restructured: below 25% Good, 25 to 50% Satisfactory, "
            + "above 50% Below Par")
    void restructuredContracts_areBandedAsTheAnnexPrints() {
        assertThat(service.gradeRestructuredContractsPercent(24.99)).isEqualTo("Good");
        assertThat(service.gradeRestructuredContractsPercent(25)).isEqualTo("Satisfactory");
        assertThat(service.gradeRestructuredContractsPercent(50)).isEqualTo("Satisfactory");
        assertThat(service.gradeRestructuredContractsPercent(50.01)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("stock statement: timely Good, delay up to 15 days Satisfactory, "
            + "over 15 days Below Par")
    void stockStatementDelay_isBandedAsTheAnnexPrints() {
        assertThat(service.gradeStockStatementDelay(0)).isEqualTo("Good");
        assertThat(service.gradeStockStatementDelay(15)).isEqualTo("Satisfactory");
        assertThat(service.gradeStockStatementDelay(16)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("creation of charge: prompt Good, delay up to 2 months Satisfactory, "
            + "over 2 months Below Par")
    void creationOfChargeDelay_isBandedAsTheAnnexPrints() {
        assertThat(service.gradeCreationOfChargeDelay(0)).isEqualTo("Good");
        assertThat(service.gradeCreationOfChargeDelay(60)).isEqualTo("Satisfactory");
        assertThat(service.gradeCreationOfChargeDelay(61)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("a contract that is still NPA is shown as Below Par, whatever was computed")
    void npaOverride_forcesBelowPar_whenAContractIsStillNpa() {
        assertThat(service.applyNpaOverride("Good", true)).isEqualTo("Below Par");
        assertThat(service.applyNpaOverride("Satisfactory", true)).isEqualTo("Below Par");
        assertThat(service.applyNpaOverride("Below Par", true)).isEqualTo("Below Par");
    }

    @Test
    @DisplayName("without an active NPA the computed grade is kept as it is")
    void npaOverride_keepsTheComputedGrade_whenNoContractIsStillNpa() {
        assertThat(service.applyNpaOverride("Good", false)).isEqualTo("Good");
        assertThat(service.applyNpaOverride("Satisfactory", false)).isEqualTo("Satisfactory");
        assertThat(service.applyNpaOverride("Below Par", false)).isEqualTo("Below Par");
    }
}
