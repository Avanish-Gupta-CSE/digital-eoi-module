# CURRENT_STATE

Last updated: 2026-11-28 (security hardening pass, Decision 020)

## Project
UCO Bank — Digital EoI (Exchange of Information) Module.
Automated generation of credit information exchange under Consortium / Multiple Banking
Arrangements.

## Repository status
- **Application exists and is feature-complete for local H2 and Docker Oracle UAT.** Spring Boot 4.1.3 / Java 25, WAR packaging, Maven.
- **Data model:** 12 tables — the 11 Annex II tables (`eoi_borrower` master + `eoi_report` per
  borrower-quarter + 9 children) plus `eoi_audit_log` in `schema-audit.sql`. Ids from `eoi_seq.NEXTVAL`.
- **Dual DB profiles:**
  - Oracle UAT/Prod default (`application.properties` + `ojdbc11`), verified against Docker Oracle Free (`uco-oracle-free` on `localhost:1521/FREEPDB1`) via `scripts/dev-start.sh`, `db_migrations/01_CREATE_EOI_TABLES.sql`, and `db_migrations/02_SEED_EOI_DEMO.sql`.
  - H2 in-memory (`--spring.profiles.active=h2`) with synthetic rows in `h2-init.sql`.
- **Auth:** Session-based (`HttpSession` + `AuthConfig` interceptor): Bank SSO (`GET /entry?Value=...` AES-GCM), `HRMS_DBA` lookup (`HrmsLookupService`), and dev/UAT OTP login (`GET/POST /login`, test OTP `123456` gated by `DEV_LOGIN_ENABLED=true`).
- **Tests:** **62 passing** (`mvn -o clean test`) — `SecurityFiltersTest` 5, `LoginThrottleTest` 2, `ReportLockTest` 5, `AuthControllerTest` 7, `IndianMoneyTest` 4, `SchemaTest` 3, `ReportServiceTest` 2, `ConductScoringServiceTest` 12, `ReportChildControllerTest` 6, `WorkflowServiceTest` 9, `WorkflowControllerTest` 1, `ReportEditControllerTest` 4.
  CI runs `guardrails` + `mvn test` on every PR.
- **Git:** `main` at `029faf0` (PRs up to #33 merged). The security pass of 2026-11-28 is uncommitted until the owner reviews it. Public repo `Avanish-Gupta-CSE/digital-eoi-module`, `main` protected. See `CONTRIBUTING.md`.

## Security controls in force (2026-11-28)
CSRF token on every POST (`web/CsrfFilter`); CSP and hardening headers (`web/SecurityHeadersFilter`); all JSP output escaped; approve/return CHECKER-only with status-guarded SQL; frozen reports (`report/ReportLockInterceptor`); report-scoped child deletes; login throttle and session-id change; no default DB password; hardened session cookie. Plain filters and interceptors, not Spring Security (Decisions 019, 020).

## Look and feel (2026-11-28)
UcoRealtyStaff shell: UCO Bank logo header, blue nav bar, UCO footer. Assets in `static/assets/images/`. Bump `?v=` on `style.css` in `fragments/head.jspf` after any CSS change.

## Working screens (all on `main`)

| Route | What it does | Controller |
|---|---|---|
| `/dashboard` | quarterly borrower list, KPI cards, search + status filters | `web/DashboardController` |
| `/reports/{id}` | read-only Annex II Parts I–VI, workflow panel, print stylesheet | `report/ReportController` |
| `/reports/{id}/edit` | maker entry form (GET) and save (POST), field-level validation | `report/ReportEditController` |
| `/reports/{id}/prefill` | copy the previous quarter's report into this one (POST) | `report/ReportEditController` |
| `/reports/{id}/export.csv` | zero-dependency CSV export | `report/ReportEditController` |
| `/reports/{id}/submit` · `/approve` · `/return` | maker–checker state machine (POST) | `report/WorkflowController` |
| `/reports/{id}/sync-finacle` | pulls CBS-owned values from the mock adapter (POST) | `report/WorkflowController` |

**Services of note:** `ConductScoringService` (advisory Part VI scoring; an NPA that is still NPA on
the exchange date is forced Below Par and cannot be overridden), `WorkflowService` (state transitions
written to `eoi_audit_log`), `FinacleDataSource` interface with `MockFinacleDataSource` as its only
implementation today.

## How to run it
```sh
mvn -q compile
mvn -q spring-boot:run -Dspring-boot.run.profiles=h2   # http://localhost:8080/dashboard
mvn -q test
sh scripts/check-guardrails.sh
```

## Known artefacts
| Path | What it is |
|---|---|
| `.brain/01_Business_Requirement/BRD.pdf` | 9-page scanned PDF (no text layer). Two UCO Bank internal letters + Annex II of the RBI direction. Git-ignored on purpose. |
| `.brain/01_Business_Requirement/RBI_Circular.pdf` | **Correct governing direction (updated 2026-11-27):** `RBI/DOR/2025-26/159` (*Commercial Banks – Transfer and Distribution of Credit Risk Directions, 2025*, 57 pages). Resolves BRD scan/OCR gaps on Annex II pp. 46–52 (and reveals Part IV has **8 columns**, not 7). Git-ignored. |
| `.brain/01_Business_Requirement/RBI_PARA_127_128.md` | Verbatim regulatory text of paras 127–129 from `RBI/DOR/2025-26/159`, source metadata, and gap analysis. |
| `.brain/01_STATE/RESUME_HERE.md` | The handoff file: what works, where things are, what to do next, traps. |
| `.brain/03_SYSTEM/Change_Map.md` | Screen → controller → service → repository → view table. |

## Regulatory basis (confirmed)
RBI (Commercial Banks – Transfer and Distribution of Credit Risk) Directions, 2025.
RBI/DOR/2025-26/159, DOR.STR.REC.No.78/21.04.048/2025-26, dated 28-11-2025, updated 01-10-2026.
- Para 127 — enabling guidance, no software obligation.
- Para 128 — the actual mandate: Annex I declaration (₹5 cr threshold), Annex II exchange
  at least quarterly, Annex III professional certification, CIC report usage, confidentiality clauses.
- Para 129 — lead bank monitors unhedged FX exposure.
- Applies to UCO Bank (corresponding new bank).

## Business ask (from letter dated 21-08-2026)
1. Auto-fetch borrower & facility-wise data from Finacle.
2. Auto-capture sanctioned limits, outstanding, defaults, account conduct per para 127/128.
3. Auto-generate prescribed EoI format (Annex II) with Maker–Checker workflow.
4. Dashboard/alerts for quarterly generation, submission, audit trail.

## Tech constraints (from AGENTS.md)
- No inline JS in JSP; no inline CSS.
- Dual profile: `h2` (in-memory dev) and `uat`/`oracle` (Oracle 19c, ojdbc11).
- No secrets in code; no mock data outside `h2-init.sql`.
- Reference pattern: `uco-realty-v3`.

## Tooling — agent delivery team (added 2026-11-21, named 2026-11-22)
| Path | What it is |
|---|---|
| `.opencode/agents/*.md` | Eight agents: `manager` (primary/default), `po`, `tl`, `sse2`, `sse`, `sse-b`, `se`, `ase` |
| `.opencode/guardrails/minimal-code.md` | Binding simplicity, dependency, offline-build, testing, change-map rules |
| `.opencode/guardrails/ui-ux.md` | Binding UI/UX, bank vocabulary, accessibility, print, local-asset rules |
| `.opencode/guardrails/security-and-integrity.md` | Binding security rules (Decision 021), with `.opencode/patterns/` and `.opencode/LESSONS.md`; checked by `check-guardrails.sh` and `GuardrailsTest` |
| `.opencode/opencode.json` | `default_agent: manager`; both guardrails in `instructions` (absolute paths) |
| `.opencode/TEAM.md` | Org chart, routing table, worked example, definition of done |
| `.opencode/README.md` | Quickstart, model tiering, failure modes |
| `.brain/01_STATE/RESUME_HERE.md` | Start here after a context reset |

Team: Nagendra Immadi (`manager`) · Krishna Chankapurai (`po`) · Shaik Arif (`tl` design/review and
`sse2` build) · Uday Kiran Sayaboyina (`sse` backend) · Rajeshwari Narella (`sse-b` view) ·
Vishal Singh (`se`) · Avanish Gupta (`ase`). Manager and TL do not write feature code; only Manager
writes `.brain/`.

Workflow: owner → Manager → (PO | TL) → SSE2/SSE/SSE-2/SE/ASE → TL review. Work flows up for review,
never sideways. Deliverables are signed by the person who did the work.

## Unknowns blocking design
Six business questions are open as GitHub issues #1–#6 on the project board. What can still be built
without them is listed in `.brain/01_STATE/NEXT_STEPS.md`.
