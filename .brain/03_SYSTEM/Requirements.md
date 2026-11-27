# SYSTEM REQUIREMENTS — Digital EoI Module

---

## 1. Regulatory Context
Under **Reserve Bank of India (RBI) Directions on Transfer and Distribution of Credit Risk (dated 28.11.2025, Paras 127 & 128)**, lending institutions engaged in **Multiple Banking Arrangements (MBA)** or **Consortium Arrangements** must periodically exchange credit information on corporate borrowers.

The exchange must occur **at least at quarterly intervals** using the standardized format enclosed as **Annex II: Format for Credit Information Exchange**.

---

## 2. Business Objectives (UCO Bank)
1. **Automate Data Extraction:** Reduce manual burden on branch credit officers by auto-pulling borrower data, limits, outstandings, and account conduct from Finacle CBS.
2. **Standardized RBI Annex II Generation:** Eliminate ad-hoc manual spreadsheets and format deviations.
3. **Maker-Checker Governance:** Ensure dual-control verification before any report is transmitted to consortium lenders.
4. **Monitoring & Compliance Tracking:** HO Credit Department and Zonal Offices must have a centralized dashboard to track pending, generated, and submitted quarterly reports across all corporate accounts.
5. **Regulatory Audit Readiness:** Maintain an unalterable audit log of every report generation, approval, and dispatch.

---

## 3. Functional Requirements (The 6 Parts of Annex II)

### Part I: Bio-Data of the Company
* Borrowing party name and registered/corporate address.
* Constitution (Public Ltd, Pvt Ltd, Partnership, Sole Proprietorship, LLP, etc.).
* Names of Directors / Partners.
* Business Activity (Main activity & Allied activities).
* Names of other financing banks in the consortium/MBA.
* Net worth of Directors / Partners.
* Group affiliation & Associate concerns banking with UCO Bank.
* Material changes in shareholding and management from previous quarter.

### Part II: Major Credit Quality Indicators
* **IRAC Classification:** Standard, SMA-0, SMA-1, SMA-2, Sub-Standard, Doubtful (D1/D2/D3), Loss / NPA.
* **Internal Credit Rating:** Bank's internal credit rating grade with descriptive narration.
* **External Credit Rating:** Agency name (CRISIL, ICRA, CARE, India Ratings, etc.), rating assigned, and validity date.
* **Annual Report Date:** Date of latest audited financial statements reviewed by the bank.

### Part III: Exposure Details (Non-Derivatives)
Facility-wise tabular details in **₹ in crore**:
* Credit Facility Type (Cash Credit, Working Capital Demand Loan, Term Loan, Short-Term Loan, Foreign Currency Loan, LC, BG, DPG, Bill Discounting).
* Purpose of loan & Sanction date.
* Sanctioned limit vs. Balance outstanding as on quarter end.
* Repayment terms & installment schedule.
* Security offered: Primary security, Collateral security, Personal/Corporate Guarantees, Cash flow escrow/control.
* Default history in term commitments, lease rentals, or dues.
* Adverse legal/statutory proceedings, court cases, or auditor qualifications.

### Part IV: Derivatives Exposure
Tabular tracking of derivative transactions:
* Plain Vanilla: Forex Forward Contracts, Interest Rate Swaps (IRS), Foreign Currency Options.
* Complex Derivatives: Zero-cost option structures, exotic combinations.
* Notional amount (₹ in crore).
* Weighted average maturity.
* Mark-to-Market (MTM) positive/negative value.
* Contracts classified as NPA or restructured.

### Part V: Unhedged Foreign Currency Exposures (UFCE)
Currency-wise position breakdown:
* Short-term (<1 year): Long positions, Short positions, Net short-term exposure.
* Long-term (≥1 year): Long positions, Short positions, Net long-term exposure.
* Overall Net Position per currency and cumulative across all currencies in ₹ crore.

### Part VI: Experience with the Borrower (Conduct Evaluation)
Evaluates performance as **Good**, **Satisfactory**, or **Below Par** across 6 dimensions:
1. **Conduct of Funded Facilities:** Overdrawings count, average days to adjust, % of limit overdrawn.
2. **Conduct of Contingent Facilities:** Default instances in LC/BG payments, days to regularize.
3. **Conduct of Derivatives:** Overdue MTM days, restructured contracts.
4. **Financial Covenants:** Timely submission of monthly stock statements and timely creation/perfection of security charges.
5. **Internal Systems & Procedures:** Adequacy of inventory, receivables, and resource allocation controls.
6. **Quality of Management:** Integrity, expertise, and operational track record.

---

## REQ-002 — Annex II maker entry and edit

**Why:** Para 128(2) requires information on the conduct of borrowers' accounts to be exchanged with other lenders **in the format given in Annex II at least at quarterly intervals**. Parts I to VI hold content that Finacle cannot supply (business activity, security, group affiliation, Part VI comments), so a maker must be able to complete and correct the whole form for one borrower and one quarter.
**Who:** Branch officer (maker, Scale 1–3 per Decision 004). Entry is branch-scoped: a maker sees and edits only borrowers mapped to their own branch.
**Story:** As a maker, I can complete all six parts of Annex II for one borrower and one quarter on one screen, so that the return is ready for checking without re-keying anything.

**Temporary assumptions in force (Decision 017, 27-11-2026 — binding until the bank answers)**
- Finacle is reached through the `FinacleDataSource` interface; the mock implementation is the only one built, and "Sync from Finacle" must work end to end against it.
- Scope is Annex II Parts I–VI only. Annex I and Annex III are not built.
- Quarter-end and the ₹5 crore borrower threshold come from `eoi.current-quarter` and `eoi.borrower-threshold-amount`. Neither is hardcoded.
- Part VI scoring is **advisory**: the officer may override a suggested grade with a remark, **except** the NPA rule, which is mandatory.
- Part IV carries eight columns, with "Amount of contracts classified as NPA" and "Notional Amount of outstanding contracts which have been restructured" as two separate amounts.

**Acceptance criteria**
- [ ] Given a borrower and a quarter are selected, when the entry screen loads, then all six parts are reachable and each part shows whether it is complete or still to be filled.
- [ ] Given I press **Sync from Finacle**, when the sync finishes, then sanctioned limit, balance outstanding, IRAC classification and overdrawing counts are filled and each synced value is marked as taken from Finacle on dd-mm-yyyy, while anything I typed myself is left unchanged.
- [ ] Given a previous quarter report exists for the borrower, when I press **Prefill from Previous Quarter**, then the form opens with the previous quarter's values and I can change any of them before saving.
- [ ] Given a derivative contract is classified NPA and is still NPA as on the exchange date, when Part VI is graded, then that grade is Below Par and the officer cannot override it.
- [ ] Given I have unsaved changes, when I try to leave the screen, then I am warned before the changes are lost.

**Screen wording (use these exact labels)**
| Element | Wording |
|---|---|
| Page title | Annex II Entry |
| Part tab 1 | Part I — Bio Data of the Company |
| Part tab 2 | Part II — Major credit quality indicators |
| Part tab 3 | Part III — Exposure Details other than Derivatives |
| Part tab 4 | Part IV — Exposure Details - Derivatives Transactions |
| Part tab 5 | Part V — Un-hedged foreign currency exposures |
| Part tab 6 | Part VI — Experience with the borrower |
| Button | Sync from Finacle |
| Button | Prefill from Previous Quarter |
| Button | Save Draft |
| Button | Submit for Checking |
| Status badge | Draft |
| Success message (sync) | Finacle data synced as on dd-mm-yyyy. 12 values updated. |
| Success message (prefill) | Details copied from the report for Q2 FY2026-27. Check every value before saving. |
| Success message (save) | Annex II for Q3 FY2026-27 saved. Reference: EOI-2026-0042. |
| Error message (sync failed) | Finacle could not be reached. Enter the values manually, or try Sync from Finacle again. |
| Empty state (no previous quarter) | No previous quarter report exists for this borrower. Enter the details for this quarter. |
| Field label | Currency |
| Field label | Sanctioned limit |
| Field label | Balance outstanding |
| Field label | Amount sanctioned (facility wise) |
| Field label | Purpose of loan |
| Field label | Repayment terms |
| Field label | Security offered |
| Field label | Purpose of changing the suggested grade * |
| Rule message (Part VI) | A contract classified NPA and still NPA as on the exchange date is graded Below Par. This grade cannot be changed. |
| Amount format | ₹ 12,45,00,000 |
| Date format | 21-11-2026 |

**Out of scope:** Annex I borrower declaration; Annex III professional certification (Decision 017, assumption 2); live Finacle connectivity; approval of the report (REQ-003); PDF and CSV generation.
**Open questions for the bank:** Is the Part VI grade advisory or binding for all parameters, not only NPA? Are amounts held in ₹ crore as the annex prints them, or in rupees and displayed in crore? Is a Part IV row one contract or one line per nature of transaction?

---

## REQ-003 — Maker–checker workflow and audit trail

**Why:** Para 128(2) makes the exchange a statement to other lenders about a borrower's conduct, and Para 128(5) requires confidentiality clauses covering that exchange. The bank must therefore be able to show, for every report, which officer prepared it, which officer approved it, and what changed. A single officer must not be able to both prepare and approve.
**Who:** Maker (branch officer, Scale 1–3) prepares and submits. Checker (Branch Head or designated approving authority, Scale 4+, per Decision 004) approves or returns. Auditor / HO viewer sees all zones read-only.
**Story:** As a checker, I can approve or return a submitted report with my name recorded against the decision, so that nothing reaches another lender without dual control.

**Who can see what**
- Maker: own branch borrowers only, and only while the report is Draft or Returned. A Submitted or Approved report is read-only to the maker.
- Checker: reports from their own branch or zone in Submitted state, and the full history of any report they have acted on.
- Auditor / HO viewer: every report in every zone, read-only, including history. No approve, return or edit action.

**What happens on rejection:** the report returns to the maker as **Returned**, the reason is shown to the maker on the report and in the history, and the maker may edit and submit again. The earlier submission and the return both remain in the history and are never overwritten.

**Acceptance criteria**
- [ ] Given a Draft report, when the maker presses **Submit for Checking**, then the status becomes Submitted, the maker's name and date-time are shown on the report, and the maker can no longer edit it.
- [ ] Given a Submitted report, when the checker opens it, then every part is read-only with the maker's name and submission date-time visible, and the checker is offered **Approve** and **Return to Maker**.
- [ ] Given the checker presses **Return to Maker**, when the remark is shorter than 10 characters, then the return is refused with the message under the field; when a valid remark is entered, then the status becomes Returned and the maker sees that remark on the report.
- [ ] Given the checker is the same officer who submitted the report, when that officer opens it, then **Approve** and **Return to Maker** are not available and the screen states that a different officer must check it.
- [ ] Given any report, when its history is opened, then every Draft, Submitted, Approved and Returned event appears with officer name, role, date-time and remark, and no entry can be edited or deleted.

**Screen wording (use these exact labels)**
| Element | Wording |
|---|---|
| Page title | Report for Checking |
| Status badge | Draft |
| Status badge | Submitted |
| Status badge | Approved |
| Status badge | Returned |
| Button | Submit for Checking |
| Button | Approve |
| Button | Return to Maker |
| Button | View History |
| Field label | Remarks * |
| Confirmation (submit) | Submit this report for checking? You will not be able to edit it until it is returned. |
| Confirmation (approve) | Approve this report for Q3 FY2026-27? |
| Error (empty remark) | Enter a reason for returning the report, at least 10 characters. |
| Error (self-approval) | You prepared this report. A different officer must check it. |
| Error (already acted) | This report has already been approved by <name>. Reload the page to see the current status. |
| Success message (approved) | Report approved by <name> on dd-mm-yyyy at hh:mm. |
| Success message (returned) | Report returned to <maker name>. Reason: <remark>. |
| Read-only banner | You can view this report but cannot change it. |
| History heading | History |
| History empty state | No history yet. History appears after the report is submitted for checking. |
| Legend | * Required |

**Out of scope:** transmission or dispatch of the approved report to other lenders; digital signature; Annex I and Annex III workflow; deletion of a report.
**Open questions for the bank:** May a returned report be edited by any maker in the branch, or only by the officer who prepared it? Can approval be delegated when the checker is on leave, and if so who records the delegation? How long must the history be retained — is it the bank's record-retention period or the RBI period for credit information?

---

## Requirement numbering

**REQ-001 is deliberately unused.** The first delivered slice (the dashboard, PRs #14 and #17) was
built and merged before requirements were written up as stories. When that story is written, it takes
REQ-001. REQ-002 (Annex II entry) and REQ-003 (maker–checker + history) are the next two slices.

REQ-002 intentionally covers all six parts on one entry screen, with part tabs. If the bank wants each
part to save and submit independently, it becomes six stories and the acceptance criteria must be
re-cut. That is a bank decision, not a technical one.
