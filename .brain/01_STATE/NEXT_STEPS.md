# NEXT_STEPS

Last updated: 2026-11-28

## Status

**All application slices (PRs #18–#29) are complete on `main`.** The application now covers:
- Dashboard + KPI cards + status filter chips (`Draft`, `Submitted`, `Approved`, `Returned`, `Due`, `Overdue`) + `Open` link
- Read-only Annex II Parts I–VI report view + print CSS + CSV export + Maker–Checker workflow & `eoi_audit_log`
- Full Maker entry/edit (`/reports/{id}/edit`) for Parts I–II + Parts I.III–I.V and III–VI child-row add/delete + Part VI `ConductScoringService` auto-grader
- Bank SSO (`GET /entry?Value=...` AES-GCM) + `HRMS_DBA` lookup + dev/UAT OTP login (`/login`, test OTP `123456`)
- Oracle UAT Docker support (`uco-oracle-free`, `db_migrations/`, `scripts/dev-start.sh`) and H2 laptop profile (`h2`)
- **57 tests pass** (`mvn -o clean test`) and `check-guardrails.sh` is green. What remains is bank-gated (live Finacle adapter, lender transmission channel, Annex I/III scope) — see below.

> **RBI Circular verified (2026-11-27):** `.brain/01_Business_Requirement/RBI_Circular.pdf` is the
> correct 57-page `RBI/DOR/2025-26/159` (*Commercial Banks – Transfer and Distribution of Credit Risk
> Directions, 2025*). It closes several BRD scan gaps (G17, G18, G21, G22, G24, G25, G26, most of G28)
> and confirms **Part IV has 8 columns** — `Amount of contracts classified as NPA` and
> `Notional Amount of outstanding contracts which have been restructured` are two separate columns.
> Delivered: `eoi_derivative` carries `npa_amount` + `restructured_notional_amount`.

| Question | Contained by | Unlocks when answered |
|---|---|---|
| #1 Finacle mechanism | `FinacleDataSource` interface + `MockFinacleDataSource` | the live adapter |
| #2 v1 scope | complete Annex II Parts I–VI built | Annex I / Annex III if in scope |
| #3 Submission channel | print/PDF-ready screen + CSV export | transmit to lenders |
| #4 Quarterly calendar | `eoi.current-quarter` property | the scheduler |
| #5 ₹5 crore threshold | `eoi.borrower-threshold-amount` | confirming the population rule |
| #6 Non-Finacle data | maker entry forms now exist | external sources if preferred |

## Next for the agent team
Dry-run the team with the new rules: ask `manager` for one small endpoint and confirm the `SECURITY` line, four negative tests and green gates appear (`opencode debug agents` first, to confirm the new guardrail path loads).

## Review gaps still open (found 2026-11-28, not fixed: they need an owner or bank decision)
1. **No row-level access control:** any signed-in officer sees and edits every borrower's report. UcoRealtyStaff scopes by SOL/zone; the EoI scoping rule (branch, zone, head office) is not defined.
2. **No re-open path** for an APPROVED report that has an error (it is now locked). Needs a bank-defined, audited "re-open" action and a rule for who may do it.
3. **No audit rows for edits or child add/delete:** only submit/approve/return/sync reach `eoi_audit_log`, but STANDING_DECISIONS asks for every edit. Add audit writes.
4. **Dev stand-in officer is always CHECKER** (`AuthController.devStaff`), and `DEV_LOGIN_ENABLED` plus test OTP `123456` must be removed before the audit build (secure-coding order, point 8).
5. **Numeric inputs** (Part VI rubric, derivatives) go through `Double.parseDouble` with no range check.
6. The nav shows a fake "Credit Officer / Maker" and a `DEV` chip when auth is off or on UAT; make the environment chip a property.
7. Login attempts and logout are not logged (UcoRealtyStaff logs both); no log-folder policy.

## Build sequence

| # | Phase | State |
|---|---|---|
| 0 | Skeleton, dashboard, money formatting, CI | **done** (#14, #17) |
| 1 | Annex II data model, Parts I–VI + audit table | **done** (#16, #18) |
| 2 | Maker entry form for Parts I/II (and all parts) | **done** (#23) |
| 3 | Maker–checker state machine + audit log | **done** (#23) |
| 4 | Part VI scoring engine (advisory, NPA override) | **done** (#20) |
| 5 | Report renderer + print + CSV export | **done** (#19, #22, #24) |
| 6 | Finacle adapter | **partly done** — interface + mock (#24); live adapter blocked on #1 |
| 7 | Quarterly scheduler + alerts | **not started** — blocked on #4 |

## Remaining work — all bank-gated

| Item | Blocked on | What it needs |
|---|---|---|
| Oracle 19c `uat` profile | bank environment | `ojdbc11` driver, `application-uat.properties`, an Oracle 19c schema run once (including the Part IV `ALTER` in Decision 017) |
| Bank SSO / HRMS login | bank IT | the `uco-realty-v3` authentication pattern and role mapping |
| Live `FinacleDataSource` adapter | question #1 | the actual mechanism: API, replica DB views, or file drop |
| Scheduler + quarterly alerts | question #4 | confirmed quarter-end rule |
| Lender transmission | question #3 | recipient system and channel |

Nothing in the code blocks these; each is a swap behind an interface or a property.

## The six questions, verbatim from the BRD

1. **Finacle integration mechanism.** API / web service, direct DB extract, or file drop?
   This single answer determines the whole architecture (sync adapter vs batch ETL).
2. **Scope v1.** Is it Annex II only (as the letter implies), or also Annex I capture and
   Annex III Diligence Report tracking as para 128 requires?
3. **Submission channel.** How does the generated EoI leave the bank — email with attachment,
   a lender portal, or an API to other banks? Nothing in the BRD defines a recipient system.
4. **Quarterly calendar.** Fixed as-on dates (30 Jun / 30 Sep / 31 Dec / 31 Mar) or
   rolling "at least quarterly" per borrower?
5. **Borrower population.** Is the ₹5 crore sanctioned-limit threshold (para 128(1)) the
   selection rule for which borrowers appear in the module?
6. **Data not in Finacle.** Shareholding changes, net worth of directors, integrity of
   management, litigation — manual entry screens + maker-checker, or external sources?

## Notes
- All work runs through the agent team in `.opencode/agents/` — talk to `manager`.
- Part V (currency positions) and Part IV (derivatives MTM) are the least likely data to exist
  cleanly in Finacle. Confirm with treasury before promising auto-population.
- Page 6 of the BRD scan OCR'd poorly; re-read the original if Part V precision matters.
