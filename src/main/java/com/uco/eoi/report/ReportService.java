package com.uco.eoi.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Assembles the Annex II report of one borrower and quarter.
 *
 * The service owns exactly one rule: the Part V nets (Annex II Part V, Decision 018).
 * Everything else is reading the tables and putting them in the order the annex prints them.
 */
@Service
public class ReportService {

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    /** The whole report, or null when no report carries that id. */
    public ReportView report(long reportId) {
        ReportHeader header = repository.findHeader(reportId).orElse(null);
        if (header == null) {
            return null;
        }
        return new ReportView(
                header.reportId(), header.quarter(), header.status(),
                header.borrower(), header.report(),
                repository.findDirectors(header.borrowerId()),
                repository.findAlliedActivities(header.borrowerId()),
                repository.findOtherBanks(header.borrowerId()),
                repository.findFacilities(header.reportId()),
                repository.findDerivatives(header.reportId()),
                netExposures(repository.findCurrencyPositions(header.reportId())),
                repository.findExperience(header.reportId()),
                repository.findRubric(header.reportId()));
    }

    /**
     * Part V arithmetic, exactly as Annex II prints it (Decision 018): short net = short long
     * minus short short, long net = long long minus long short, and the printed
     * "Overall Net Position (I - II)" = short net minus long net.
     *
     * There is no total across currencies: without FX rates it would be a wrong figure, so it
     * is not printed at all. The amounts are not rounded, so the annex figures stay exact.
     */
    static List<CurrencyRow> netExposures(List<RawCurrency> raw) {
        List<CurrencyRow> rows = new ArrayList<>();
        for (RawCurrency position : raw) {
            BigDecimal shortNet = position.shortLong().subtract(position.shortShort());
            BigDecimal longNet = position.longLong().subtract(position.longShort());
            rows.add(new CurrencyRow(position.currency(),
                    position.shortLong(), position.shortShort(), shortNet,
                    position.longLong(), position.longShort(), longNet,
                    shortNet.subtract(longNet)));
        }
        return rows;
    }
}
