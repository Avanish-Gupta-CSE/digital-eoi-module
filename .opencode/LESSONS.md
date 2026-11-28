# Lessons — defects the first build shipped, and what catches them now

Append a row whenever a reviewer finds something new. The Manager folds repeated rows into
`guardrails/security-and-integrity.md`. Found by review on 2026-11-28 (PR #34).

| # | Defect as it looked in the code | Why it hurts | Caught by |
|---|---|---|---|
| 1 | `<td>${r.name}</td>` for typed data | stored XSS: a `<script>` in a director name runs for the checker | `check-guardrails.sh` (rule 1) |
| 2 | `?quarter=` printed as `${view.quarter}` in text and in an attribute | reflected XSS | `check-guardrails.sh` |
| 3 | `<form method="post">` with no token | any website can submit or approve as the officer | `check-guardrails.sh` (rule 2) |
| 4 | No CSP header sent, though "bank CSP" was a stated rule | the rule was never enforced | `SecurityFiltersTest` |
| 5 | `approve()` with no role check | a maker approved reports | negative test "wrong role" |
| 6 | `UPDATE ... SET status='APPROVED' WHERE id=?` | approve a draft, approve twice, resubmit an approved report | negative test "wrong status"; rule 4 |
| 7 | Edit/child endpoints open in every status | numbers changed after the checker saw them | `GuardrailsTest` (rule 6) |
| 8 | `DELETE FROM eoi_x WHERE id = ?` | row of report B deleted through report A's URL | `GuardrailsTest` (rule 5) |
| 9 | Session id kept at login; OTP compared with `equals`, unlimited tries | session fixation, guessing | `AuthControllerTest`, `LoginThrottleTest` |
| 10 | `password=${EOI_DB_PASSWORD:uco_eoi_pwd}` | a default credential committed | `check-guardrails.sh` (rule 9) |
| 11 | CSV cell `=cmd...` exported as-is | formula runs in Excel | rule 10 test |
| 12 | Docs said "Spring Security, JPA, POI"; code had none | agents trusted the docs | the code is the truth; update docs in the same PR |
| 14 | Dry run 2026-11-28: manager's final report had no `SECURITY` block | the checklist existed but the report did not have to carry it | manager.md "Reporting to the owner" |
| 15 | Dry run: a builder rewrote the working return-form JS while adding a sibling feature | a security-relevant screen changed for no requested reason | guardrail "Scope of a change"; TL checks the diff for untouched-code edits |
| 13 | A change to a `.jspf` not showing in a running app | looked like a bug, was a stale compile | restart the app after editing a fragment |
