# Annex II — Schema Design (accepted)

Designed by **Shaik Arif (TL)** on 2026-11-22 from `AnnexII_Field_Inventory.md`.
**Accepted by the Manager the same day**, with both open decision points signed off:

- **(a) Borrower/report split — accepted.** Part I is "one per borrower"; Parts II–VI are "per
  borrower per report"; Part I.IX ("from the previous report") only makes sense across reports.
- **(b) One shared sequence `eoi_seq` — accepted.** IDs need only be unique within their table.
  Eleven sequences would be complexity without benefit.

---

## 1. Anchor decision

`eoi_borrower` = borrower master (static Part I identity). `eoi_report` = one quarterly return
(Parts II–VI + workflow state). The existing `eoi_borrower` mixed both and is re-cut.

## 2. Tables (11)

| # | Table | Purpose | Part |
|---|---|---|---|
| 1 | `eoi_borrower` | borrower master + static bio-data | I |
| 2 | `eoi_report` | one return per borrower per quarter; Part II + workflow | II, I.IX, III.IX |
| 3 | `eoi_borrower_director` | directors/partners and their net worth | I.III, I.VI |
| 4 | `eoi_borrower_allied` | allied business activities | I.IV(b) |
| 5 | `eoi_borrower_bank` | other financing banks | I.V |
| 6 | `eoi_facility` | one credit facility per report | III |
| 7 | `eoi_facility_security` | security per facility | III.VII |
| 8 | `eoi_derivative` | one derivative contract per report | IV |
| 9 | `eoi_currency_position` | one currency per report, long/short only | V |
| 10 | `eoi_report_experience` | six graded items per report | VI.A |
| 11 | `eoi_report_rubric` | rubric observations per report | VI.B |

*Associate concerns* stays a **column** on `eoi_borrower` (G1 unresolved; a table would need invented columns).

## 3. Columns

Types are portable: `NUMERIC(p,s)`, `VARCHAR(n)`, `DATE`. No `VARCHAR2`, no `BIGINT`, no `CLOB`,
no `DEFAULT` for id generation (see §4).

**eoi_borrower** — id PK NUMERIC(18,0) · borrower_name VARCHAR(150) N (I.I) · address VARCHAR(500) N
(I.I) · constitution VARCHAR(50) N (I.II, G2) · business_activity_main VARCHAR(200) N (I.IVa) ·
group_affiliation VARCHAR(200) Y (I.VII) · associate_concerns VARCHAR(500) Y (I.VIII, G1) ·
consortium_name VARCHAR(150) N (module)

**eoi_report** — id PK · borrower_id FK N · report_quarter VARCHAR(8) N (G5) · status VARCHAR(20) N ·
reported_on DATE Y (G5) · irac_classification VARCHAR(20) Y (II.I, G6) · internal_rating VARCHAR(20) Y
(II.IIa) · internal_rating_narration VARCHAR(500) Y (II.IIb) · external_rating VARCHAR(100) Y
(II.III, G7) · annual_report_latest VARCHAR(100) Y (II.IVa) · annual_report_as_on DATE Y (II.IVb, G8) ·
changes_shareholding VARCHAR(1000) Y (I.IX) · special_information VARCHAR(1000) Y (III.IX, G13)

**eoi_borrower_director** — id PK · borrower_id FK · person_name VARCHAR(150) N · designation
VARCHAR(50) Y · net_worth NUMERIC(18,2) Y (G4)

**eoi_borrower_allied** — id PK · borrower_id FK · activity VARCHAR(200) N (G3)

**eoi_borrower_bank** — id PK · borrower_id FK · bank_name VARCHAR(150) N

**eoi_facility** — id PK · report_id FK · facility_type VARCHAR(100) N (III.I) · purpose VARCHAR(200) Y ·
facility_date DATE Y · sanctioned_amount NUMERIC(18,2) Y · outstanding_amount NUMERIC(18,2) Y
(G9, G11) · repayment_terms VARCHAR(500) Y · defaults_term_commitments VARCHAR(500) Y (G12)

**eoi_facility_security** — id PK · facility_id FK · security_description VARCHAR(500) N (G10)

**eoi_derivative** — id PK · report_id FK · serial_no NUMERIC(9,0) Y (G19) · nature VARCHAR(100) N
(G17, G18) · notional_amount NUMERIC(18,2) Y · weighted_avg_maturity NUMERIC(18,4) Y (G20) ·
positive_mtm NUMERIC(18,2) Y · npa_amount NUMERIC(18,2) Y · restructured_notional_amount NUMERIC(18,2) Y · restructuring_reasons
VARCHAR(500) Y

**eoi_currency_position** — id PK · report_id FK · currency VARCHAR(10) N (G23) · short_long
NUMERIC(18,2) Y · short_short NUMERIC(18,2) Y · long_long NUMERIC(18,2) Y · long_short NUMERIC(18,2) Y
(G21, G22)

**eoi_report_experience** — id PK · report_id FK · item_code VARCHAR(4) N (I–VI) · grade VARCHAR(20) N
(Good / Satisfactory / Below Par)

**eoi_report_rubric** — id PK · report_id FK · item_code VARCHAR(4) N · param_no NUMERIC(3,0) N (1–17) ·
observed_value VARCHAR(100) Y · grade VARCHAR(20) N

## 4. Keys, indexes, sequence

- **`eoi_seq`** — one shared sequence. Insert with `eoi_seq.NEXTVAL`. **Never** `NEXT VALUE FOR`
  (H2-only, hard-fails Oracle); never a `DEFAULT`.
- Unique: `eoi_report (borrower_id, report_quarter)`, `eoi_currency_position (report_id, currency)`,
  `eoi_report_experience (report_id, item_code)`, `eoi_report_rubric (report_id, param_no)`.
- **Index every FK explicitly** — Oracle does not auto-index FKs.
- `eoi_borrower_bank` also needs `(borrower_id, bank_name)` unique.

## 5. Not stored (computed instead)

- Part V nets (short net, long net, per-currency overall, across-all): computed in Java.
- Dashboard sanctioned/outstanding: `SUM(facility.sanctioned_amount)` / `SUM(facility.outstanding_amount)`.
- Part VI rubric bands: **no band table this phase** (G26/G28 unknown). Observed value + grade only.
- VI.A item grades: officer-entered, not derived (G29).

## 6. Files

| Action | File |
|---|---|
| edit | `src/main/resources/db/schema.sql` — borrower (re-cut), report, director, allied, bank, `eoi_seq` |
| add | `src/main/resources/db/schema-annexii-exposure.sql` — facility, facility_security, derivative, currency_position |
| add | `src/main/resources/db/schema-annexii-experience.sql` — report_experience, report_rubric |
| edit | `src/main/resources/application-h2.properties` — ordered `schema-locations` list |
| edit | `src/main/resources/db/h2-init.sql` — reseed split tables |
| add | `src/test/java/com/uco/eoi/db/SchemaTest.java` |
| edit | `src/main/java/com/uco/eoi/borrower/*` + `web/DashboardController` + `dashboard.jsp` — dashboard migration |
| edit | `.brain/03_SYSTEM/Change_Map.md` |

## 7. Risks

- `eoi_seq.NEXTVAL` only; `NEXT VALUE FOR` breaks Oracle.
- Oracle `VARCHAR` limit is 4000 **bytes**; keep long text ≤ 1000, never silently bump to CLOB.
- No column named `DATE`, `LONG`, `LEVEL`, `NUMBER`, `ORDER`.
- Portability proven only on H2-Oracle-mode here; real Oracle 19c is the UAT check.
- **This re-cut breaks the current dashboard** — `Borrower`, `BorrowerRepository`, `BorrowerService`
  (threshold now applies to `SUM(sanctioned_amount)`), `DashboardController`, `dashboard.jsp` must
  migrate in the same PR or `main` stops running.

## 8. Deferred

G1 (associate concerns as table), G2/G6/G23 (value lists from the bank), G4 (net-worth unit),
G5 (reporting date), G7 (rating validity), G8/G11 (as-on dates), G9/G10/G12/G13, G17–G20,
G21/G22 (Part V units), G24–G28 (rubric bands), G29 (grading discipline).
