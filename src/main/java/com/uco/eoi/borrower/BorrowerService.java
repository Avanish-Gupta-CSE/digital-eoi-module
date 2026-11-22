package com.uco.eoi.borrower;

import com.uco.eoi.common.IndianMoney;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The one place where the dashboard rule lives.
 *
 * The borrower threshold is a property (eoi.borrower-threshold-amount) because the
 * bank has not yet confirmed that the Rs 5 crore limit selects the population
 * (open question, board issue #5). When they answer, this file does not change.
 */
@Service
public class BorrowerService {

    private final BorrowerRepository repository;
    private final BigDecimal threshold;

    public BorrowerService(BorrowerRepository repository,
                           @Value("${eoi.borrower-threshold-amount}") BigDecimal threshold) {
        this.repository = repository;
        this.threshold = threshold;
    }

    public DashboardView dashboard(String quarter) {
        List<BorrowerRow> rows = new ArrayList<>();
        int due = 0;
        int submitted = 0;
        int overdue = 0;

        for (Borrower borrower : repository.findByQuarter(quarter)) {
            if (borrower.sanctionedLimit().compareTo(threshold) < 0) {
                continue;
            }
            rows.add(toRow(borrower));
            if ("DUE".equals(borrower.status())) {
                due++;
            } else if ("SUBMITTED".equals(borrower.status())) {
                submitted++;
            } else if ("OVERDUE".equals(borrower.status())) {
                overdue++;
            }
        }
        return new DashboardView(quarter, rows, rows.size(), due, submitted, overdue);
    }

    private BorrowerRow toRow(Borrower borrower) {
        return new BorrowerRow(
                borrower.borrowerName(),
                borrower.consortiumName(),
                IndianMoney.format(borrower.sanctionedLimit()),
                IndianMoney.format(borrower.outstanding()),
                borrower.status(),
                statusClass(borrower.status()));
    }

    private String statusClass(String status) {
        return switch (status) {
            case "SUBMITTED" -> "badge-submitted";
            case "OVERDUE" -> "badge-overdue";
            default -> "badge-due";
        };
    }
}
