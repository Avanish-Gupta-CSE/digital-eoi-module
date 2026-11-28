# STANDING DECISIONS — In Force Today

> This file contains the active decisions governing the **Digital EoI Module**.  
> Derived from [`Decisions.md`](Decisions.md).

---

## 1. Product & Workflow
* **Application Scope:** Internal intranet portal for UCO Bank branches, Zonal Offices, and Head Office Credit Department. No public-facing portal.
* **Maker–Checker Mandate:** Every quarterly EoI report must be generated/verified by a Maker (Credit Officer) and independently approved by a Checker (Branch Head / Approver). Self-approval is strictly forbidden.
* **Audit Trail:** Every status change, edit, review remark, submission, and approval must be written to an immutable `EOI_AUDIT_LOG` table with employee ID, SOL ID, timestamp, and action description.

## 2. Technology Stack & Engineering
* **Backend:** Java 25, Spring Boot 4.1.3, `JdbcTemplate` repositories (no JPA, **no Spring Security**: Decisions 019 and 020).
* **Packaging:** Executable WAR deployable to Tomcat 11.
* **Frontend:** Server-rendered JSP + JSTL, Bootstrap 5.
* **Security & CSP:** Strict Bank CSP compliance (zero inline scripts, zero inline CSS).
* **Database:** Oracle 19c (`ojdbc11`) for UAT & Production; in-memory H2 for local standalone runs.
* **Reporting:** zero-dependency CSV export and a print stylesheet today. Apache POI and PDF are **not built** (blocked on the submission channel, question #3).

## 3. Finacle & Data Rules
* **Interface Abstraction:** Use `FinacleIntegrationService` interface to support either direct Oracle replica DB views or Finacle Integrator APIs.
* **Prefill from Previous Quarter:** To avoid repetitive manual typing, non-changing corporate bio-data and facilities can be cloned from the last approved quarter's report.
* **Automated Part VI Scoring:** Overdrawing frequency, adjustment delays, and default counts are calculated by the rules engine to determine "Good", "Satisfactory", or "Below Par". If an account or derivative is NPA, the rating is automatically "Below Par".

## 3a. Security (Decision 020)
* Every `<form>` carries `<input type="hidden" name="_csrf" ...>`; every user-sourced `${}` in a JSP goes through `<c:out>`.
* Approve and return are CHECKER-only; a report is editable only in DRAFT, DUE, OVERDUE or RETURNED.
* No committed default database password; the CSP and hardening headers come from `SecurityHeadersFilter`.

## 4. Agent Behavioral Guidelines (Karpathy Contract)
* **Think Before Coding:** Explicit assumptions, surface tradeoffs, stop and ask when requirements are ambiguous.
* **Simplicity First:** Minimum code to solve the problem, zero speculative abstractions or unasked flexibility.
* **Surgical Changes:** Touch only what is strictly required, zero orthogonal refactoring, match existing style, clean up only your own orphans.
* **Goal-Driven Execution:** Explicit verification criteria (`[Step] → verify: [check]`), loop until provably verified.
* **Artifacts in Workspace:** Maintained in `CLAUDE.md`, `AGENTS.md`, `GEMINI.md`, `.agents/rules/karpathy.md`, and `.brain/AGENT_RULES.md`.
* **Seven-Agent Delivery Team (2026-11-21):** Work is planned and executed by the agents in
  `.opencode/agents/` — `manager` (primary) → `po` / `tl` → `sse2` / `sse` / `se` / `ase`.
  Manager and TL are structurally denied application-code edits; only Manager writes `.brain/`.
  Two guardrail files (`.opencode/guardrails/minimal-code.md`, `ui-ux.md`) are loaded into every
  agent via `instructions` and are binding: size limits, banned constructs, no new dependency,
  no CDN or remote asset, empty/error/loading states required, bank vocabulary preserved.
