# RESUME HERE — read this before touching code

Last updated: 2026-11-22. If anything here disagrees with the code, **the code wins** — fix this file.

---

## 1. What works right now

A Spring Boot 4.1.3 / Java 25 WAR with the full Annex II maker-checker flow, SSO/HRMS login and a security layer (CSRF, CSP, report lock). **Sections 4 and 5 below are partly historic: `CURRENT_STATE.md` and `NEXT_STEPS.md` are the live truth.** Every new form must carry the `_csrf` hidden field.

```sh
# run it (the second command is not optional: spring-boot:run does not recompile)
mvn -q compile
mvn -q spring-boot:run -Dspring-boot.run.profiles=h2
# open http://localhost:8080/dashboard

# verify
mvn -q test                    # 57 tests
sh scripts/check-guardrails.sh # no inline script/style in JSPs
```

Proven, not assumed: `/dashboard` returns 200 with 4 borrowers; `?quarter=Q1FY2526` renders the empty
state; all CSS/JS load from `/assets/` with no external request; the ₹4.5 crore sample row is filtered
out by `eoi.borrower-threshold-amount`.

## 2. Where things are

| Layer | Path |
|---|---|
| Entry point | `src/main/java/com/uco/eoi/DigitalEoiApplication.java` |
| Controller | `src/main/java/com/uco/eoi/web/DashboardController.java` |
| Business rule | `src/main/java/com/uco/eoi/borrower/BorrowerService.java` |
| SQL | `src/main/java/com/uco/eoi/borrower/BorrowerRepository.java` |
| Schema + mock data | `src/main/resources/db/schema.sql`, `db/h2-init.sql` |
| Views | `src/main/webapp/WEB-INF/jsp/` (`dashboard.jsp`, `fragments/*.jspf`) |
| Assets | `src/main/resources/static/assets/` (Bootstrap vendored, no CDN) |
| Config | `src/main/resources/application.properties`, `application-h2.properties` |

Full screen→file table: [`03_SYSTEM/Change_Map.md`](../03_SYSTEM/Change_Map.md).

## 3. The one design rule that matters

Every unanswered bank question is **configuration or an interface**, never a guess:

| Open question (issue) | How it is contained today |
|---|---|
| Finacle mechanism (#1) | not built yet — adapter goes behind `BorrowerRepository` |
| v1 scope (#2) | only the dashboard exists; Annex II tables not built |
| Submission channel (#3) | no export yet |
| Quarterly calendar (#4) | `eoi.current-quarter` property |
| ₹5 crore threshold (#5) | `eoi.borrower-threshold-amount` property |
| Non-Finacle data (#6) | no manual-entry screens yet |

**If an answer arrives, change one property or add one class.** If you find yourself hardcoding a
threshold, a quarter, or a recipient, stop.

## 4. What to do next (in this order)

1. **Annex II data model.** Read `.brain/01_Business_Requirement/RBI_PARA_127_128.md` and BRD pages
   3–9. Create tables for Parts I–VI in `schema.sql` (portable SQL only), seed `h2-init.sql`.
   Verify: every Annex II cell maps to a column, and the schema runs on H2.
2. **Maker entry form.** One JSP + one controller + validation. Verify: bad input shows an error
   under the field, good input persists to H2.
3. **Maker–checker state machine + audit table.** Verify: a maker cannot approve their own record.
4. **Part VI scoring** as pure functions with unit tests (rubric is BRD pages 50–52).
5. **Report renderer** (print CSS first, PDF later).

Before starting any of these, the Manager creates a board card (`scripts/board.sh add ...`).

## 5. Traps discovered the hard way

- `${view.empty}` fails — `empty` is reserved in EL. Use `${empty view.rows}`.
- Record accessors work in EL, but **extra** methods on a record do not reliably. Keep view objects
  as plain records with components only.
- `mvn spring-boot:run` alone uses stale classes. Always `mvn -q compile` first.
- JSPs are recompiled by Jasper; if a JSP change seems ignored, delete `target/work`.
- H2 runs with `MODE=Oracle`; still keep SQL portable — no `LIMIT`, no `NVL2`.
- `CHANGED`, `CHECKED`, `OPEN` report discipline is enforced by the agent prompts, and the PR
  template asks for real command output, not "should work".

## 6. State

- Branch: `main`. Run `git log --oneline -3` for the exact head.
- Board: https://github.com/users/Avanish-Gupta-CSE/projects/2 — six blocked business questions.
- Decisions: `02_DECISIONS/Decisions.md`, newest at the bottom.
