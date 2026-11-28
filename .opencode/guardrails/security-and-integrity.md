# Guardrail 3 — Security and integrity (binding, machine-checked)

Binding for every agent. The first build of this app broke every rule below (stored XSS, no CSRF,
no CSP, any maker could approve, approved reports editable, rows deletable through another
report's URL). A reviewer found them; this file is so nobody has to. Rules 1-4 and 9 are also
checked by `sh scripts/check-guardrails.sh`; rules 3-5 by `GuardrailsTest`. A red gate is a red PR.

**Copy the pattern, do not invent one.** Each rule names a real file. Open it, copy its shape.
More examples: `.opencode/patterns/`. Past mistakes: `.opencode/LESSONS.md`.

## The ten rules

| # | Rule | Copy this |
|---|---|---|
| 1 | **Escape output.** Every `${...}` that prints text in a JSP goes through `<c:out value="${...}"/>`, also inside attributes. Only `contextPath`, numeric ids and `c:` tag attributes may be bare. | `fragments/report-part1-3.jspf` |
| 2 | **CSRF.** Every `<form>` (also `<form:form>`) carries `<input type="hidden" name="_csrf" value="<c:out value='${sessionScope._csrf}'/>">`. GET never changes data. | `fragments/nav.jspf` (logout form) |
| 3 | **Who may do it (role).** Check the role on the server, not by hiding a button. | `WorkflowController.notChecker` |
| 4 | **From which status.** The UPDATE itself carries `AND status IN (...)`; zero rows updated = refuse with a message. Never read-then-write. | `WorkflowService.SET_APPROVED` |
| 5 | **Whose row.** Any `/reports/{id}/x/{rowId}` SQL matches the parent too: `WHERE id = ? AND report_id = ?`. | `ReportExposureRepository.deleteCurrency` |
| 6 | **Frozen reports.** A new write URL under `/reports/{id}/...` must be added to `ReportLockConfig`, or editing an approved report stays possible. Workflow actions are the only exceptions. | `ReportLockConfig` |
| 7 | **Audit.** A state change writes an `eoi_audit_log` row in the same `@Transactional` method. | `WorkflowService.writeAudit` |
| 8 | **Errors.** Never show exception text, SQL or stack traces. Catch, flash a plain message, redirect. | `ReportChildExceptionHandler` |
| 9 | **No secrets.** No password, key, token or default credential in code, properties or SQL seeds except `h2-init.sql`. Values come from environment variables. | `application.properties` |
| 10 | **Exports.** A CSV/Excel cell starting with `= + - @` is prefixed with `'`. | `ReportEditController.csvCell` |

SQL is always a bound `?` parameter. The only `+` allowed in a SQL string joins two literals, or the
configured HRMS schema name.

## The four negative tests (required for every new endpoint)

Passing the happy path proves nothing. Each new POST endpoint gets a test for each of these, in the
style of `ReportLockTest` and `SecurityFiltersTest`:

1. **Wrong role** — a maker calls a checker action → refused, nothing written.
2. **Wrong status** — the report is APPROVED/SUBMITTED (or DRAFT for a checker action) → refused.
3. **Wrong owner** — row id belongs to another report → nothing changes.
4. **No token** — POST without `_csrf` → refused (covered globally by `CsrfFilter`; add a case only if you add a new filter path).

If a case truly does not apply, write `n/a because ...` in the report. Silence is a failure.

## Before you start any new area

Read `.brain/03_SYSTEM/Architecture.md`, then the matching reference in the bank's sister project
`/Users/avanish/Documents/Projects/uco-realty-v3/UcoRealtyStaff` (config, controllers) and its
`.brain/03_SYSTEM/Secure_Coding_Compliance.md` (the bank's secure-coding office order, point by point).
The bank's order wins over convenience.

## Scope of a change

Touch only the lines the task needs. Do not rewrite, rename or "tidy" working code next to yours (a
dry run rewrote the existing return-form JS to share a helper: that is a separate task, and a risk
to a security-relevant screen). Add alongside; if sharing code really is needed, ask the Manager.

## Report format (adds one line)

`CHANGED`, `TEST`, `RUN`, `SECURITY`, `OPEN`. `SECURITY` lists rules 1-10 as `yes` / `n/a because ...`
and the four negative tests as `done` / `n/a because ...`.

## Stop and ask

New dependency, Spring Security, weakening a header, a default password "just for dev", turning the
lock or the CSRF filter off for a URL, or any rule above that cannot be met.
