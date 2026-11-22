# Annex II — Field Inventory (design input)

Produced by **Krishna Chankapurai (PO)** on 2026-11-22 from `AnnexII_ocr.md` (BRD scan pp. 3–9) and
`RBI_PARA_127_128.md`. Verified and corrected by the Manager at 600 dpi OCR on the same day
(see the addendum at the end — it changes Part IV).

Authority: RBI/DOR/2025-26/159, Para 128(2), Annex II "Format for Credit Information Exchange",
direction pp. 46–52.

**Rule for anyone building from this:** a field marked GAP is not permission to guess. Store it in a
way that accepts the answer later (nullable column, or a reference table seeded later), never as an
invented enum value or a made-up band.

---

## Units

Parts III and IV are headed **(Rs. in crore)**. Part V states **(Rs. in crore)** for item IV only.
Parts I, II and VI print no unit.

## Part I — Bio Data of the Company

| # | Field (exact annex wording) | Type | Cardinality | Source | Notes |
|---|---|---|---|---|---|
| I | Borrowing party's name and address | text-long | one per borrower | p.3 I | Annex prints name + address as one item |
| II | Constitution | enum (values not enumerated) | one per borrower | p.3 II | G2 |
| III | Names of Directors / Partners | child-table | one per director / partner | p.3 III | |
| IV(a) | Business activity — Main | text | one per borrower | p.3 IV | |
| IV(b) | Business activity — Allied | child-table | one per allied activity | p.3 IV | G3 |
| V | Names of other financing Banks | child-table | one per other bank | p.3 V | also the distribution list |
| VI | Net worth of Directors / Partners | child-table | one per director / partner | p.3 VI | G4: no unit, no as-on date |
| VII | Group affiliation, if any | text | one per borrower | p.3 VII | nullable |
| VIII | Date on associate concerns, if banking with the same bank | text-long | one per borrower | p.3 VIII | G1: "Date" is probably "Data" |
| IX | Changes in shareholding and management from the previous report, if any | text-long | one per borrower per report | p.3 IX | period-to-period |

## Part II — Major credit quality indicators

| # | Field | Type | Cardinality | Source | Notes |
|---|---|---|---|---|---|
| I | IRAC Classification | enum (values not enumerated) | per borrower per report | p.3 II-I | G6 |
| II(a) | Internal Credit rating with narration — rating | text | per borrower per report | p.3 II-II | |
| II(b) | Internal Credit rating with narration — narration | text-long | per borrower per report | p.3 II-II | "narration" is required prose |
| III | External Credit rating, if any | text | per borrower per report | p.3 II-III | G7: no agency/date/validity |
| IV(a) | Latest available Annual Report of the borrower | text | per borrower per report | p.3 II-IV | |
| IV(b) | Latest available Annual Report — "As on -" | date | per borrower per report | p.3 II-IV | G8 |

## Part III — Exposure Details other than Derivatives (Rs. in crore)

Facility-wise. 9 items.

| # | Field | Type | Cardinality | Notes |
|---|---|---|---|---|
| I | Type of credit facilities (…, contingent facilities like LC, BG & DPG (I&F) etc., L/C bills discounting, project wise finance) | enum as printed | per facility | list not closed ("etc.") |
| II | Purpose of loan | text | per facility | |
| III | Date of loan facilities (including temporary facilities) | date | per facility | temporary facilities are rows, not excluded |
| IV | Amount sanctioned (facility wise) | amount (crore) | per facility | |
| V | Balance outstanding (facility wise) | amount (crore) | per facility | G9 minimal lost numeral; G11 no as-on date |
| VI | Repayment terms | text-long | per facility | |
| VII | Security offered | child-table | per security item per facility | G10 fourth bullet unresolved |
| VIII | Defaults in term commitments / lease rentals / others | text-long | per facility (assumed) | G12 |
| IX | Any other special information like court cases, statutory dues, major defaults, adverse audit observations | text-long | per borrower (assumed) | G13 |

## Part IV — Exposure Details, Derivatives Transactions (Rs. in crore)

**Corrected at 600 dpi: seven columns, not nine.** G15 and G16 are closed — "(Not due for
settlement)" is a qualifier of the positive MTM column, and "classified as NPA which have been
restructured" is one column.

| # | Field | Type | Notes |
|---|---|---|---|
| 1 | Sr. No. | number | implies contract-level rows — G19 still open |
| 2 | Nature of the derivatives Transactions | enum as printed | A.1 Forex Forward contracts; A.2 Interest rate Swaps; A.3 Foreign Currency Options; A.4 Any other contracts (please specify); B.1 Contracts involving only interest rate derivatives; B.2 *unreadable* (G17); B.3 Other contracts involving foreign currency derivatives; B.4 Any other contracts (please specify) |
| 3 | Notional Amount of contracts outstanding | amount (crore) | |
| 4 | Weighted average maturity of contracts | number | G20 unit unresolved |
| 5 | Amount of positive MTM for the bank (Not due for settlement) | amount (crore) | one column |
| 6 | Amount of contracts classified as NPA which have been restructured | amount (crore) | one column |
| 7 | Major reasons for restructuring (in brief) | text-long | |

Sections as printed: **A. Plain Vanilla Contracts**, **B. Complex derivatives including various types
of option combinations designed as cost reduction/zero cost structures**.

## Part V — Un-hedged foreign currency exposures, currency-wise

| # | Field | Type | Cardinality | Notes |
|---|---|---|---|---|
| 1 | Currency | text | per currency | G23 no list |
| 2 | Short term (<1 year) — Long positions | number | per currency | G21 numerals lost; G22 unit unknown |
| 3 | Short term (<1 year) — Short positions | number | per currency | |
| 4 | Short term — Net short-term exposure (a-b) | **derived** | per currency | do not store |
| 5 | Long term (1 year and beyond) — Long positions | number | per currency | |
| 6 | Long term — Short positions | number | per currency | |
| 7 | Long term — Net long-term exposure (a-b) | **derived** | per currency | do not store |
| 8 | Overall Net Position (I - II) for each currency | **derived** | per currency | do not store |
| 9 | Overall Net Position across all currencies | amount (crore) | per borrower per report | only item with an explicit unit |

## Part VI — Experience with the borrower

Two levels, both stored: **item grade** (6 rows) and **rubric parameter** (17 recoverable rows).
The annex states the constraint itself: *"(The above to be rated as good, satisfactory or below par
only)"*.

### VI.A — six graded items (one per borrower per report)

I Conduct of funded facilities (cash management / tendency to overdraw) ·
II Conduct of contingent facilities (payment history) · III Compliance with financial covenants ·
IV Company's internal systems & procedures · V Quality of management · VI Overall Assessment.
Each is an enum: Good / Satisfactory / Below Par.

### VI.B — rubric parameters

| # | Item | Parameter | Good | Satisfactory | Below Par |
|---|---|---|---|---|---|
| 1 | 1 | Over-drawings (No. of times) | Upto 4 times | 5 to 6 times | **Above 6 times** (G24, inferred from orphan digit) |
| 2 | 1 | Average period of adjustment | Within 1 month | Within 2 months | Beyond 2 months |
| 3 | 1 | Extent of overdrawings (% of limit) | Upto 10% | 10 to 20% | Above 20% |
| 4 | 2 | No. of Defaults | Upto 2 times | 3 to 4 times | **Above 4 times** (G24, inferred) |
| 5 | 2 | Average period of adjustment | Within 1 week | Within 2 weeks | Beyond 2 weeks |
| 6 | 3 | No. of contracts where positive MTM due to the bank remained overdue > 30 days | <25% of total contracts | 25–50% | >50% |
| 7 | 3 | … overdue > 90 days and the … the date of information | <1% | 1–5% | >5% (G25 clause truncated) |
| 8 | 3 | No. of contracts restructured during the relevant period | <25% | 25–50% | >50% |
| 9 | 4 | Stock statement / Financial data | Timely | Delay upto 15 days | Delay over 15 days |
| 10 | 4 | Creation of charge | Prompt | Delay upto 2 months | Delay over 2 months |
| 11 | 5 | Inventory Management | Adequate systems in place | …but not adhered | **G26** |
| 12 | 5 | Receivables Management | Adequate systems in place | …but not adhered | **G26** |
| 13 | 5 | Resource Allocation | **G26** | **G26** | **G26** |
| 14 | 5 | Control over Information | **G26** | **G26** | **G26** |
| 15 | 6 | Integrity | Reliable | Nothing adverse | Cannot be categorized in previous columns |
| 16 | 6 | Expertise | **G28** | **G28** | **G28** |
| 17 | 6 | Tract Record [Track Record] | **G28** | **G28** | **G28** |

**Binding rule printed inside the rubric (p.8), not a field:** *"All cases where any of the contracts
has been classified as NPA and continues to be NPA as on the date of the exchange of information
should be shown as Below Par."* — a validation rule that overrides the bands.

## Child tables implied by the annex

Directors/Partners · Allied business activities · Other financing banks · Associate concerns ·
Credit facilities · Security offered (child of facility) · Derivatives contracts · Un-hedged currency
positions · Experience assessment items (exactly 6 per report) · Experience rubric parameters
(17 rows per report).

## Gaps

| # | Page | Why it matters |
|---|---|---|
| G1 | 3 | "Date on associate concerns" meaning unknown |
| G2 | 3 | Constitution values not enumerated |
| G3 | 3 | "Allied" list vs single value |
| G4 | 3 | Director net worth: unit, as-on date, per person vs aggregate |
| G5 | 3–4 | no reporting date printed anywhere |
| G6 | 3 | IRAC values not enumerated |
| G7 | 3 | external rating: no agency/date/validity |
| G8 | 3 | "As on -" blank |
| G9 | 4 | lost numeral V |
| G10 | 4 | unlabelled security bullet / cash-flow control |
| G11 | 4 | no as-on date for sanctioned/outstanding |
| G12 | 4 | item VIII facility vs borrower |
| G13 | 4 | item IX facility vs borrower |
| ~~G14~~ | 5 | **closed** — column set verified at 600 dpi |
| ~~G15~~ | 5 | **closed** — "(Not due for settlement)" qualifies positive MTM |
| ~~G16~~ | 5 | **closed** — NPA + restructured is one column |
| G17 | 6 | section B row 2 text lost |
| G18 | 6 | B.4 vs unnumbered row |
| G19 | 5 | contract-level vs type-level rows |
| G20 | 5 | maturity unit |
| G21 | 6 | Part V numerals lost |
| G22 | 6 | Part V units |
| G23 | 6 | no currency list |
| G24 | 7 | two Below-Par band numbers lost (6 and 4 inferred from orphan digits) |
| G25 | 8 | >90-day clause truncated |
| G26 | 8 | `- do -` in a three-column rubric; 4 of 12 cells unknown |
| G27 | 8 | **closed** — Timely = stock statement Good, Prompt = creation of charge Good |
| G28 | 9 | **the scan is physically truncated**, not an OCR failure: Track Record bands and Expertise wording are absent from the enclosure |
| G29 | 7 | whether officer grades parameters, items, or both |

## Open questions for the bank

1. **Part IV granularity** — one row per contract or per transaction type? (Part VI counts contracts, so contract-level is the only computable basis.)
2. **Part VI rubric** — the enclosure page is cut off; who supplies the full Annex II rubric page?
3. **Value lists** — Constitution and IRAC: from the bank's masters, or fixed in this module?
4. **Reporting date** — the annex prints no as-on date; is every report tied to quarter-end, and is outstanding as on that date or as on generation?
5. **Part V units** — original currency, USD equivalent, or ₹ crore?
6. **Grading discipline** — advisory or binding? Does the module refuse a grade that contradicts the bands?
7. **Storage unit** — store rupees and display crore, or store crore as the annex does? This affects every amount column.

---

## Addendum — Manager verification, 2026-11-22

Re-OCR'd pages 5, 8 and 9 at 600 dpi with Apple Vision. Findings folded into the tables above:

- **Part IV has 7 columns, not 9.** Confirmed header row:
  `Notional Amount of contracts outstanding | Weighted average maturity of contracts | Amount of
  positive MTM for the bank (Not due for settlement) | Amount of contracts classified as NPA which
  have been restructured | Major reasons for restructuring (in brief)`. G14/G15/G16 closed.
- **Page 8 band mapping resolved.** Stock statement / Financial data: Timely → Delay upto 15 days →
  Delay over 15 days. Creation of charge: Prompt → Delay upto 2 months → Delay over 2 months. G27 closed.
- **Page 9 is truncated in the source scan**, not by OCR. The enclosure physically ends mid-rubric.
  G28 cannot be closed from this document — it needs a clean copy from the bank.
- The NPA override note on p.8 was read cleanly and is quoted verbatim above.
