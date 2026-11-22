# FINACLE DATA GAP ANALYSIS — Digital EoI Module

> This document details which fields of the RBI Annex II format are auto-fetched from Finacle CBS, which are computed by the application logic, and which require manual input from the branch Maker.

---

## Part-by-Part Classification

### Part I: Bio-Data of the Company
| Field | Classification | Source / Strategy |
| :--- | :--- | :--- |
| Borrower Name | **[AUTO]** | Finacle Customer Master (`CMG.CUST_NAME`) |
| Registered / Corporate Address | **[AUTO]** | Finacle Address Table (`RET` / `CORP` via CIF) |
| Constitution | **[AUTO]** | CIF Master Constitution Code (`CMG.CONSTITUTION_CODE`) |
| Names of Directors / Partners | **[HYBRID]** | Auto-pulled from Finacle related parties if available; editable by Maker |
| Main Business Activity | **[AUTO]** | Finacle Occupation / Industry NIC Code (`CMG.OCCUPATION_CODE`) |
| Allied Activities | **[MANUAL]** | Entered by Maker |
| Other Financing Banks | **[MANUAL / PERSISTED]** | Recorded by Maker; persisted from previous quarter |
| Net Worth of Directors / Partners | **[MANUAL]** | From audited CA net-worth certificates |
| Group Affiliation | **[HYBRID]** | Finacle Group CIF; editable by Maker |
| Changes in Management / Shareholding | **[MANUAL]** | Qualitative notes entered by Maker |

---

### Part II: Major Credit Quality Indicators
| Field | Classification | Source / Strategy |
| :--- | :--- | :--- |
| IRAC Classification | **[AUTO]** | Finacle Asset Classification (`GAM.ASSET_CLASS_ID` / IRAC table) |
| Internal Credit Rating & Narration | **[HYBRID]** | Internal Rating model / Manual entry |
| External Credit Rating & Agency | **[MANUAL / PERSISTED]** | Agency (CRISIL, ICRA, etc.), rating symbol, and validity |
| Latest Annual Report Date | **[MANUAL]** | Date picker in JSP |

---

### Part III: Exposure Details (Non-Derivatives)
| Field | Classification | Source / Strategy |
| :--- | :--- | :--- |
| Facility Type (TL, CC, OD, LC, BG) | **[AUTO]** | Finacle Scheme Type (`GAM.SCHM_TYPE`, `SCHM_CODE`) |
| Loan Purpose & Sanction Date | **[AUTO]** | Finacle Loan Master (`LAM.SANCT_DATE`) |
| Amount Sanctioned | **[AUTO]** | Finacle Sanction Limit (`GAM.SANCT_LIM`) |
| Balance Outstanding | **[AUTO]** | Finacle Clear Balance (`GAM.CLR_BAL_AMT`) |
| Repayment Terms | **[HYBRID]** | Finacle Installment Details / Maker review |
| Primary & Collateral Security | **[HYBRID]** | Finacle Security Register (`SRM` tables); editable by Maker |
| Defaults in Commitments | **[COMPUTED]** | Finacle Overdue Interest/Principal Demands (`LALD`/`LAD`) |
| Special Information / Court Cases | **[MANUAL]** | Qualitative remarks text area |

---

### Part IV & V: Derivatives & UFCE
| Field | Classification | Source / Strategy |
| :--- | :--- | :--- |
| Plain Vanilla & Complex Derivatives | **[MANUAL / IMPORT]** | Tabular entry grid for Treasury contracts; MTM & NPA status |
| Unhedged Foreign Currency Exposure | **[MANUAL / PERSISTED]** | Form grid for Short-term (<1 yr) & Long-term (≥1 yr) net positions |

---

### Part VI: Conduct & Experience (The Rules Matrix)

The application evaluates Finacle daily balance and demand records to compute ratings:

```
┌───────────────────────────────────────┬──────────────┬────────────────┬──────────────┐
│ Metric / Parameter                    │ Good         │ Satisfactory   │ Below Par    │
├───────────────────────────────────────┼──────────────┼────────────────┼──────────────┤
│ Funded Overdrawings (Frequency)       │ ≤ 4 times    │ 5 to 6 times   │ > 6 times    │
│ Average Adjustment Period             │ ≤ 1 month    │ 1 to 2 months  │ > 2 months   │
│ Extent of Overdrawing (% of Limit)    │ ≤ 10%        │ 10% to 20%     │ > 20%        │
│ Contingent Defaults (LC/BG devolved)  │ ≤ 2 times    │ 3 to 4 times   │ > 4 times    │
│ Contingent Adjustment Period          │ ≤ 1 week     │ 1 to 2 weeks   │ > 2 weeks    │
│ Stock Statement Submission Delay      │ Prompt / Nil │ ≤ 15 days      │ > 15 days    │
│ Creation of Security Charge Delay     │ Prompt / Nil │ ≤ 2 months     │ > 2 months   │
│ Any Facility or Derivative is NPA     │ —            │ —              │ ALWAYS Below │
└───────────────────────────────────────┴──────────────┴────────────────┴──────────────┘
```
