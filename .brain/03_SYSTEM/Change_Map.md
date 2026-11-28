# CHANGE MAP — where to edit what

Keep this table current. A beginner must be able to find the file for a label change in 30 seconds.

## Screens

| Screen | URL | Controller | Service | Repository | View | JS |
|---|---|---|---|---|---|---|
| Quarterly dashboard | `/dashboard` | `web/DashboardController#dashboard` | `borrower/BorrowerService#dashboard` | `borrower/BorrowerRepository#findByQuarter` | `WEB-INF/jsp/dashboard.jsp` | `assets/js/dashboard.js` |
| Annex II report (read-only) | `/reports/{id}` | `report/ReportController#report` | `report/ReportService#report`; `WorkflowService` for the panel state | `report/ReportRepository` | `report.jsp` + `fragments/report-part1-3.jspf`, `report-part4-6.jspf`, `report-status.jspf`, `report-workflow.jspf` | `assets/js/dashboard.js` (print binding) |
| Maker entry | `/reports/{id}/edit` (GET form, POST save) | `report/ReportEditController#edit`, `#save` | `report/ReportService#report`; `ReportWriteRepository` for writes | `report/ReportWriteRepository` | `WEB-INF/jsp/report-edit.jsp` | `assets/js/report-edit.js` |
| Prefill from previous quarter | POST `/reports/{id}/prefill` | `report/ReportEditController#prefill` | `report/ReportWriteRepository` | `report/ReportWriteRepository` | — | `assets/js/report-edit.js` |
| Annex II CSV export | `GET /reports/{id}/export.csv` | `report/ReportEditController#export` | `report/ReportService#report` | `report/ReportRepository` | — | — |
| Maker–checker actions | POST `/reports/{id}/submit`, `/approve`, `/return` | `report/WorkflowController` | `report/WorkflowService` | `resources/db/schema-audit.sql` (`eoi_audit_log`) | `fragments/report-workflow.jspf` | `assets/js/workflow.js` |
| Sync from Finacle | POST `/reports/{id}/sync-finacle` | `report/WorkflowController` | `report/WorkflowService` + `FinacleDataSource` → `MockFinacleDataSource` | `report/ReportWriteRepository` | `fragments/report-workflow.jspf` | `assets/js/workflow.js` |

## Shared pieces

| What | File |
|---|---|
| Page shell (head / header / footer) | `WEB-INF/jsp/fragments/head.jspf`, `nav.jspf`, `foot.jspf` |
| Colours, badges, print styles | `static/assets/css/style.css` |
| Money formatting (₹, lakh/crore) | `java/com/uco/eoi/common/IndianMoney.java` |
| Table definitions — Part I + report header | `resources/db/schema.sql` |
| Table definitions — Parts III & IV (facilities, derivatives) | `resources/db/schema-annexii-exposure.sql` |
| Table definitions — Part VI (experience) | `resources/db/schema-annexii-experience.sql` |
| Table definition — audit trail | `resources/db/schema-audit.sql` (`eoi_audit_log`) |
| Part VI scoring rules (advisory, NPA override) | `java/com/uco/eoi/report/ConductScoringService.java` |
| Finacle seam — swap the live system in here | `java/com/uco/eoi/report/FinacleDataSource.java` (interface), `MockFinacleDataSource.java` (today) |
| Maker–checker state machine + audit writes | `java/com/uco/eoi/report/WorkflowService.java` |
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

## Built — do not rebuild

Annex II report view · maker entry form · prefill from previous quarter · CSV export ·
maker–checker with audit log · Part VI advisory scoring with mandatory NPA override ·
`FinacleDataSource` interface with `MockFinacleDataSource`.

## Bank-gated, not built

Oracle 19c `uat` profile (`ojdbc11`, `application-uat.properties`, one schema run including the
Part IV `ALTER` from Decision 017) · bank SSO/HRMS login following `uco-realty-v3` · the live
`FinacleDataSource` adapter · quarterly scheduler and alerts · transmission to lenders.
Each is a swap behind an interface or a property, not a rewrite.

## Data model (Annex II, 11 tables)

Anchor: `eoi_borrower` (master) + `eoi_report` (one per borrower per quarter). Children:
`eoi_borrower_director`, `eoi_borrower_allied`, `eoi_borrower_bank`, `eoi_facility`,
`eoi_facility_security`, `eoi_derivative`, `eoi_currency_position`, `eoi_report_experience`,
`eoi_report_rubric`. Ids come from the shared `eoi_seq` — always `eoi_seq.NEXTVAL`, never
`NEXT VALUE FOR` (H2-only) and never a DDL `DEFAULT`.

Design and gap analysis: `03_SYSTEM/AnnexII_Schema_Design.md` and `AnnexII_Field_Inventory.md`.
