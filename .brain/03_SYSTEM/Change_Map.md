# CHANGE MAP — where to edit what

Keep this table current. A beginner must be able to find the file for a label change in 30 seconds.

## Screens

| Screen | URL | Controller | Service | Repository | View | JS |
|---|---|---|---|---|---|---|
| Quarterly dashboard | `/dashboard` | `web/DashboardController#dashboard` | `borrower/BorrowerService#dashboard` | `borrower/BorrowerRepository#findByQuarter` | `WEB-INF/jsp/dashboard.jsp` | `assets/js/dashboard.js` |

## Shared pieces

| What | File |
|---|---|
| Page shell (head / header / footer) | `WEB-INF/jsp/fragments/head.jspf`, `nav.jspf`, `foot.jspf` |
| Colours, badges, print styles | `static/assets/css/style.css` |
| Money formatting (₹, lakh/crore) | `java/com/uco/eoi/common/IndianMoney.java` |
| Table definition and mock rows | `resources/db/schema.sql`, `resources/db/h2-init.sql` |
| Threshold, current quarter | `resources/application.properties` |
| Local database settings | `resources/application-h2.properties` |

## Common changes

| You want to | Edit |
|---|---|
| change a screen label | the matching `WEB-INF/jsp/*.jsp` |
| change an amount's format | `common/IndianMoney.java` (not the JSP) |
| change which borrowers appear | `eoi.borrower-threshold-amount` in `application.properties` |
| change the default quarter | `eoi.current-quarter` in `application.properties` |
| add a column to the dashboard | `Borrower` → `BorrowerRow` → `BorrowerService.toRow` → `dashboard.jsp` |
| add a new screen | new `@Controller` + service method + `*.jsp` + an entry in this table |
| change the database for UAT | `application-uat.properties` (Oracle, created with the uat profile) |

## Not built yet

Annex II entry form · maker–checker · audit log · Part VI scoring · report export · Finacle adapter.
Add each to this table in the PR that introduces it.
