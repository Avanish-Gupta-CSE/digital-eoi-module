# CHANGE MAP — where to edit what

Keep this table current. A beginner must be able to find the file for a label change in 30 seconds.

## Screens

| Screen | URL | Controller | Service | Repository | View | JS |
|---|---|---|---|---|---|---|
| Quarterly dashboard | `/dashboard` | `web/DashboardController#dashboard` | `borrower/BorrowerService#dashboard` | `borrower/BorrowerRepository#findByQuarter` | `WEB-INF/jsp/dashboard.jsp` | `assets/js/dashboard.js` |
| Annex II report (read-only) | `/reports/{id}` | `report/ReportController#report` | `report/ReportService#report` | `report/ReportRepository` | `WEB-INF/jsp/report.jsp` + `fragments/report-part1-3.jspf`, `report-part4-6.jspf` | `assets/js/dashboard.js` (print binding only) |

## Shared pieces

| What | File |
|---|---|
| Page shell (head / header / footer) | `WEB-INF/jsp/fragments/head.jspf`, `nav.jspf`, `foot.jspf` |
| Colours, badges, print styles | `static/assets/css/style.css` |
| Money formatting (₹, lakh/crore) | `java/com/uco/eoi/common/IndianMoney.java` |
| Table definitions — Part I + report header | `resources/db/schema.sql` |
| Table definitions — Parts III & IV (facilities, derivatives) | `resources/db/schema-annexii-exposure.sql` |
| Table definitions — Part VI (experience) | `resources/db/schema-annexii-experience.sql` |
| Mock rows for every table | `resources/db/h2-init.sql` |
| Schema provenance test | `src/test/java/com/uco/eoi/db/SchemaTest.java` |
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

## Data model (Annex II, 11 tables)

Anchor: `eoi_borrower` (master) + `eoi_report` (one per borrower per quarter). Children:
`eoi_borrower_director`, `eoi_borrower_allied`, `eoi_borrower_bank`, `eoi_facility`,
`eoi_facility_security`, `eoi_derivative`, `eoi_currency_position`, `eoi_report_experience`,
`eoi_report_rubric`. Ids come from the shared `eoi_seq` — always `eoi_seq.NEXTVAL`, never
`NEXT VALUE FOR` (H2-only) and never a DDL `DEFAULT`.

Design and gap analysis: `03_SYSTEM/AnnexII_Schema_Design.md` and `AnnexII_Field_Inventory.md`.
