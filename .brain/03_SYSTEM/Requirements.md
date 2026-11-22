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
