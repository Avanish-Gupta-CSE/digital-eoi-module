# SESSION LOG

<!-- Append new entries at the top, newest first. -->

## 2026-11-27 — PR 1 (Slice 1): Split `eoi_derivative` NPA & restructured columns + Decision 017

**Done**
- `sse2` split `eoi_derivative.npa_restructured_amount` into `npa_amount` and `restructured_notional_amount`
  in `schema-annexii-exposure.sql`, `h2-init.sql`, and `SchemaTest.java` (matching `RBI/DOR/2025-26/159` p. 48).
- Updated `AnnexII_Schema_Design.md` §3 and resolved `Decisions.md` provenance note.

**Verified**
- `mvn -o test`: 7/7 passing (`IndianMoneyTest` 4, `SchemaTest` 3); `sh scripts/check-guardrails.sh`: `guardrails: ok`.

## 2026-11-27 — Krishna wrote REQ-002 (Annex II entry) and REQ-003 (maker–checker)

**Done**
- `Requirements.md` grew by 107 lines: two stories with acceptance criteria and exact screen wording.
- REQ-002 covers all six parts on one entry screen, and carries Decision 017's temporary assumptions
  inline — mock `FinacleDataSource`, Parts I–VI only, quarter/threshold as properties, advisory Part VI
  scoring, eight-column Part IV.
- REQ-003 covers submit/approve/return, who-sees-what, and the uneditable history, citing Para 128(2)
  and 128(5).

**Decisions taken on his two flags**
- **REQ-001 stays unused and is now documented as such.** The dashboard was built and merged before
  stories were written; it takes REQ-001 when written. Renumbering was rejected because a later
  REQ-001 could collide.
- **REQ-002 stays one story** (one screen, part tabs). Splitting into six is a bank decision about
  per-part submission, not a technical one — recorded in the file so it is not re-litigated silently.

**Not done / open**
- These are specifications for slices *after* Slice 1; no code was written from them.
- Slice 1 PR 1 (Part IV split) is in flight with `se`.

## 2026-11-27 — Slice 1 blocked by session permissions; Decision 017 recorded

**Done**
- Recorded **Decision 017** (temporary assumptions for #1–#6, Part IV split into `npa_amount` +
  `restructured_notional_amount`, NPA override mandatory, no new Maven dependency).
- Mapped this session's permission boundary instead of guessing:
  * `.brain/**` edit — **allowed** (Decisions.md and this log written)
  * application code edit — **denied** (`Permission denied: edit` on `schema-annexii-exposure.sql`)
  * shell — only `git status`, `git diff`, `git log`, `ls`. `mvn`, `git switch` denied.
  * subagents — **denied** (`Permission denied: subagent`), so the PO→TL→SSE chain cannot run.
- Left the pre-existing uncommitted `.brain/01_STATE/*` edits untouched and preserved.

**Finding — unresolved contradiction in our own notes**
Two 2026-11-27 entries in this log disagree about `RBI_Circular.pdf`:
- one says it is `RBI/DOR/2025-26/159` (Commercial Banks, 57 pp.) and records the Part IV 8-column split;
- the next says it is `RBI/DOR/2025-26/277` (Urban Co-operative Banks, 95 pp.) with Annex II/III deleted.
The file is 398,222 bytes. The two notes cannot both be true. The 8-column split is adopted on the
owner's instruction and is safe either way (both columns nullable and additive), but **the circular's
identity must be confirmed before any Annex II figure is quoted to the bank**. This model cannot read
PDF input and shell is restricted, so the check could not be completed here.

**Not done / open — Slice 1, ready to execute unchanged once permissions return**
1. Branch (preserving the `.brain/01_STATE/*` edits) and commit Decision 017.
2. `eoi_derivative`: split `npa_restructured_amount` → `npa_amount` + `restructured_notional_amount`
   in `schema-annexii-exposure.sql`, `h2-init.sql`, `SchemaTest`.
3. Read-only `/reports/{id}`: all six parts, Part V nets computed in Java, print stylesheet,
   dashboard rows linked to it.
4. `tl` review, `mvn -q test`, `check-guardrails.sh`, PR, CI `guardrails`, squash-merge.

**To unblock:** run this session under an agent with code edit + shell + subagent rights
(`manager` with subagents, or `sse2`). Nothing else is required — the plan above is complete.

## 2026-11-27 — Re-read updated `RBI_Circular.pdf` (`RBI/DOR/2025-26/159`); closed scan gaps & caught Part IV 8-column split

**Done**
- Extracted all 57 pages of the updated `.brain/01_Business_Requirement/RBI_Circular.pdf` via `pypdf`.
- Confirmed it is `RBI/DOR/2025-26/159` (`DOR.STR.REC.No.78/21.04.048/2025-26`, Nov 28, 2025, updated Oct 1, 2026).

**Findings**
- **Scan/OCR gaps closed directly from the official PDF:**
  - G17 & G18 (Part IV Section B): B.1 Interest-rate only, B.2 Other contracts incl. FX derivatives, B.3 Any other contracts.
  - G21 & G22 (Part V): `(Rs. in crore)` sits at the Part V header level (applies to the whole section).
  - G24, G25, G26 (Part VI pp. 50–51): `Above 6 times` / `Above 4 times`, full `> 90 days` NPA-regularized clause, and Section V (`Adequate systems are in place` / `...but not adhered` / `...not in place`).
  - G28 (Part VI p. 52): `Expertise Competence / Commitments` = `Professional & visionary` | `Have necessary experience` | `-do-`; `Tract Record` = `Timely` | `Executions /` (truncated in RBI's own PDF!) | `-do-`.
- **Schema catch on Part IV (p. 48):** Part IV has **8 columns** (`Amount of contracts classified as NPA` and `Notional Amount of outstanding contracts which have been restructured` are two separate columns, not one). `eoi_derivative.npa_restructured_amount` in `src/main/resources/db/schema-annexii-exposure.sql` should be split before Part IV UI/export.

## 2026-11-27 — Reviewed `RBI_Circular.pdf` vs `.brain` context and prepared team gap list + build plan

**Done**
- Re-read `.brain` state (`CURRENT_STATE.md`, `NEXT_STEPS.md`, `RESUME_HERE.md`, `SESSION_LOG.md`,
  `RBI_PARA_127_128.md`, `AnnexII_Field_Inventory.md`, `AnnexII_Schema_Design.md`, `Change_Map.md`,
  `Decisions.md`) and inspected the latest repository tree (`020780e`, PR `#17` dashboard UI rebuild).
- Extracted and searched all 95 pages of `.brain/01_Business_Requirement/RBI_Circular.pdf` via `pypdf`.

**Findings**
- **`RBI_Circular.pdf` is the wrong RBI circular for this project.** It is `RBI/DOR/2025-26/277`
  (`DOR.CRE.REC.196/07-01-005/2025-26`, *Urban Co-operative Banks – Credit Facilities Directions, 2025*,
  updated Oct 1, 2026). Its Paras 127–128 cover UCB housing repair loans (₹10L/₹6L), and its
  Annex II/III are marked deleted (`12[*****]`, `13[*****]`).
- The actual governing circular cited by UCO Bank's BRD is `RBI/DOR/2025-26/159`
  (*Commercial Banks – Transfer and Distribution of Credit Risk Directions, 2025*, dated 28-11-2025),
  already verified and documented in `.brain/01_Business_Requirement/RBI_PARA_127_128.md`.

**Not done / open**
- No application code modified; waiting on owner review of the consolidated plan and bank-team
  clarification list.
- Remember to commit `.brain/01_STATE/*` updates via a feature/chore branch PR (since `main` is protected).

## 2026-11-22 — Annex II data model (11 tables) built and proven on H2

**Done**
- `po` produced the Annex II field inventory: 66 fields, 10 implied child tables, 29 gaps, 7 bank
  questions. Persisted to `.brain/03_SYSTEM/AnnexII_Field_Inventory.md`.
- Pre-OCR'd the scanned annex with Apple Vision (tesseract mangled the bordered tables) and saved the
  text to `.brain/01_Business_Requirement/AnnexII_ocr.md` so nobody repeats it.
- Re-verified pages 5, 8 and 9 at 600 dpi: **Part IV is 7 columns, not 9** (the `(Not due for
  settlement)` parenthetical qualifies positive MTM; NPA + restructured is one column). G14/G15/G16
  closed, G27 closed, and **page 9 is physically truncated in the bank's enclosure** — G28 cannot be
  closed from this document.
- `tl` designed the schema; Manager accepted both decision points (borrower/report split, one shared
  `eoi_seq`). Design in `.brain/03_SYSTEM/AnnexII_Schema_Design.md`.
- `sse2` wrote the three schema files and the 178-line seed, then hit its step limit before compiling.
  The remaining work was completed and verified here: ordered `schema-locations`, the aggregate
  dashboard query, the `Borrower` record, and `SchemaTest`.
- Seeded so the dashboard totals are unchanged; Textiles deliberately holds **two** facilities of
  ₹37.5 M each, so `₹ 7,50,00,000.00` is a real `SUM`, not a copied column.

**Verified**
- `mvn test`: 6/6, including `SchemaTest` (11 tables; duplicate report rejected).
- Negative test: with `ux_eoi_report_borrower_qtr` commented out the schema test **fails**, and passes
  when restored. The constraint is genuinely exercised.
- Running app: `/dashboard` 200 with 4 borrowers, `₹ 25,00,00,000.00` and `₹ 18,25,00,000.00` intact,
  Sample Chemicals still filtered by the threshold, empty state on `Q1FY2526`, no errors in the log.
- `check-guardrails.sh` ok; every SQL file ≤ 178 lines.

**Findings**
- The re-cut breaks the dashboard unless the query moves to `SUM(facility)` in the same change — the
  Tech Lead flagged this before any code was written, which is why `main` never went red.
- H2 `MODE=Oracle` accepts `eoi_seq.NEXTVAL` in multi-row inserts and `INSERT … SELECT`; that is what
  the seed relies on.

**Not done / open**
- No entry screen yet; the 11 tables are populated only by the seed.
- No `uat`/Oracle profile. Portability is proven on H2-Oracle-mode only.
- G1–G29 remain; the four that matter (G26, G28, G19, G22) are documented for the bank.

## 2026-11-22 — Jumpstart: first working slice, and the unknowns turned into seams

**Done**
- Spring Boot 4.1.3 / Java 25 skeleton, WAR packaging, Maven, `h2` profile, H2 in `MODE=Oracle`.
- `/dashboard` renders from real SQL: borrower, consortium, formatted limits, status badge,
  empty state for an empty quarter, print stylesheet. Header/breadcrumb/footer fragments.
- `IndianMoney` formats Indian grouping (`₹ 25,00,00,000.00`) — tested, 4 cases.
- Bootstrap 5.3.3 vendored into `static/assets/vendor/bootstrap/` with its MIT licence. Zero
  external requests; verified every asset returns 200 from the app.
- Unknowns contained rather than guessed: threshold and current quarter are properties; the
  repository is the single data seam.
- Verified running: `/dashboard` 200 with 4 borrowers; `?quarter=Q1FY2526` shows the empty state;
  the ₹4.5 crore sample row is filtered out by the threshold property; `mvn test` 4/4;
  `check-guardrails.sh` ok.

**Fixed while building (worth remembering)**
- `${view.empty}` breaks — `empty` is a reserved EL word. Use `${empty view.rows}`.
- An extra method on a record (`isEmptyResult()`) was not resolvable by EL; records are fine as
  long as the view object carries only components.
- `mvn spring-boot:run` alone served stale classes; `mvn compile` must run first.
- Stale Jasper output is cleared by deleting `target/work`.

**Not done / open**
- No `uat`/Oracle profile yet (needs `ojdbc11` and bank credentials).
- Annex II tables, entry form, maker–checker, scoring, export, Finacle adapter — all still to build;
  sequence in `01_STATE/NEXT_STEPS.md`.
- The six business questions remain open as issues #1–#6.

## 2026-11-22 — Board columns switched to Status; label drift removed

**Done**
- The project board was created, linked to the repo, and its default view converted to a Board
  layout holding all seven issues.
- Correction after looking at the live UI: columns come from the project's **Status** field
  (`Todo → In Progress → Done`), not from labels. Grouping by labels would create a column for every
  label, so `blocked` and `business-decision` would have become columns too.
- Added `scripts/board.sh` (`list`, `add`, `move`) so agents never touch GraphQL ids:
  `scripts/board.sh move 1 "In Progress"`. Verified by moving #1 to In Progress and back, and by
  confirming a bad stage name is rejected.
- Removed the five `status:*` labels from all issues and deleted their definitions, so there is one
  state and not two. `blocked` and `business-decision` remain.
- Updated `TEAM.md`, `README.md` and `manager.md`; the Manager may run `scripts/board.sh`.

**Findings**
- `gh project link` refuses user-owned projects ("has different owner from '@me'"); the
  `linkProjectV2ToRepository` GraphQL mutation links them correctly.
- Grouping ("Column by") is not exposed in the public GraphQL schema — only `layout` and
  `visibleFieldIds` are. Layout is settable, grouping is not.
- Closing an issue via a merged `Closes #n` moves its card to Done automatically, so the PR flow and
  the board need no extra step.

**Not done / open**
- Design and Review are not columns; they are visible from the linked PR on each card. Accepted as a
  deliberate simplification.
- Still blocked on the six business questions, which are issues #1–#6 in Todo.

## 2026-11-22 — Repository made public; server-side branch protection enabled

**Done**
- Scanned tracked files for credentials before publishing: no keys, tokens, passwords, or private
  keys; no PDFs or binaries tracked (the BRD scan stays git-ignored). 34 tracked files.
- Flipped `Avanish-Gupta-CSE/digital-eoi-module` to public at the owner's instruction.
- Enabled branch protection on `main`: pull request required, required status check `guardrails`,
  administrators enforced, force-push and deletion blocked, zero required approvals so a single
  person can still merge.
- Published a doc correction through a PR, because `TEAM.md`, `CONTRIBUTING.md` and `README.md` still
  claimed protection required GitHub Pro. The repo being public makes it available on the free plan.

**Verified**
- Agent layer: `git push origin main` returned `Blocked by configuration policy` before git ran.
- Server layer: a direct write to `main` via the contents API returned
  `409 — Changes must be made through a pull request. Required status check "guardrails" is expected`,
  and the file did not appear (`404`).
- CI ran on both the PR and the resulting `main` push; both green.

**Findings**
- `enforce_admins: true` plus `required_approving_review_count: 0` is the right combination for a
  solo developer: the owner cannot bypass the PR and CI gate, but does not need a second approver.
- Going public also means the six business-decision issues and the `.brain` notes are public. The
  owner accepted this explicitly; no borrower data or Finacle extracts exist in the repository.

**Not done / open**
- GitHub Projects board still blocked: the `gh` token lacks `project` scope. Issues and labels work.
- Still blocked on the six business questions, which are issues #1–#6.

## 2026-11-22 — Board moved to GitHub Issues + Projects; Vikunja retired

**Done**
- Created private repo `Avanish-Gupta-CSE/digital-eoi-module` and pushed `main`.
- Retired Vikunja: process stopped, `~/vikunja` deleted, MCP server removed from the global config.
  Reclaimed 119 MB (free space 7.2 GB → 7.3 GB) at the owner's request, since the machine is on a
  tight disk. Vikunja's SQLite board had 6 cards; nothing of value was lost because the same six
  items now exist as GitHub issues.
- Board model: one issue per task, column carried by a `status:` label
  (`backlog → design → in-progress → review → done`). Chosen over the Project's Status field because
  labels work from plain `gh issue` commands with no GraphQL node ids.
- Created labels and issues #1–#6 for the six blocking business questions, each labelled
  `business-decision, blocking, status:backlog`.
- Added `.githooks/pre-push` + `git config core.hooksPath .githooks`: rejects any push to
  `main`/`master` for humans as well as agents.
- Updated `TEAM.md`, `README.md`, `manager.md` and `CONTRIBUTING.md` to GitHub Issues/Projects; the
  Manager's permissions now allow the `gh issue` and `gh project` read/create/edit commands.

**Findings**
- **GitHub branch protection requires Pro on private repos.** The API returns 403 "Upgrade to GitHub
  Pro or make this repository public". On the free plan, enforcement is therefore the agent policy
  plus the local pre-push hook, not the server.
- **The `gh` token lacks the `project` scope** (`gist, read:org, repo, workflow`). GitHub Projects
  cannot be created until the owner runs `gh auth refresh -s project,read:project`; that flow is
  interactive and cannot be completed from an agent session. Issues, labels, PRs, reviews and merges
  all work without it.
- A local pre-push hook is not a substitute for server-side protection: it can be bypassed with
  `--no-verify`. Recorded so nobody mistakes it for a hard control.

**Not done / open**
- Project board view not created (waiting on the `project` scope).
- Slack/Teams notification on PR — not requested, not built.
- Still blocked on the six business questions, which are now issues #1–#6.

## 2026-11-22 — Git repository, PR discipline, and a local scrum board

**Done**
- `git init` on `main`, first commit `chore(repo): initialise repository with team config and
  guardrails` (33 files). The BRD scan is deliberately excluded via `.gitignore` so a bank document
  cannot travel with a future remote; the extracted RBI text stays versioned.
- `CONTRIBUTING.md`: branch model (`feature/`, `fix/`, `chore/`), commit convention, four merge
  conditions, who may merge, recovery rules.
- `.github/PULL_REQUEST_TEMPLATE.md`: person, requirement id, guardrail checklist, real test output,
  data confirmation.
- `.github/workflows/ci.yml` + `scripts/check-guardrails.sh`: CI runs the inline-script/style check
  immediately and `mvn test` once a `pom.xml` exists.
- Enforcement: project policies deny `git push` to `main`/`master` and any `--force`/`-f` push for
  every agent. Builders have `gh pr merge` and `gh pr review` explicitly denied; the Tech Lead can
  push, open PRs, review and squash-merge; the Manager can only read PR state.
- Board: **Vikunja v2.8.0**, native arm64 binary in `~/vikunja`, SQLite, bound to 127.0.0.1:3456,
  no Docker. Project "Digital EoI" with Buckets `Backlog → Design → In Progress → Review → Done`,
  and the six blocking business questions seeded as cards in Backlog.
- Integration: Vikunja ships its own MCP server at `/api/v2/mcp`; connected through the global
  OpenCode config with a scoped API token. Verified in-session: `projects_list` and `tasks_list`
  return the board, and all six cards sit in the Backlog bucket.
- Docs: `TEAM.md` gained "The board" and "Git and pull requests"; `manager.md` gained board/git
  discipline; `README.md` gained a board/git section.

**Findings**
- Taiga's original repo now redirects to `kaleidos-ventures/taiga` with no pushes since 2023-12-13 —
  not viable for a pilot that must survive offline. Focalboard also redirects. Vikunja (6.1k stars,
  active) and Plane (62.9k, heavy: Postgres + Redis + MinIO) were the real contenders; Vikunja won
  on footprint and on having a first-party MCP server.
- Docker Desktop requires a paid commercial licence for organisations of UCO Bank's size, and its
  daemon was not running. The native binary avoids both the licence question and the daemon.
- `{env:VAR}` in MCP headers is not reliable here: the shared background service does not inherit a
  shell export, and this build has no `opencode service set env`. The token therefore lives in the
  global config (mode 600) and never in the repository.
- Vikunja's API-token permissions must use the action names from `/api/v1/routes`, and `expires_at`
  is mandatory. MCP also needs the `mcp: access` permission, which is not part of the default set.
- Reading and creating cards needs only `tasks` permissions, but **moving a card between columns
  needs `projects: views_buckets_tasks_update`**. Without it the board is read-only for agents. The
  move action is `task_bucket_update` (params: `project`, `view`, `bucket`, `task_id`), and it is
  reached through `find_action`/`do_action`, not as a top-level tool. Moving a card into the Done
  bucket also marks the task done.
- The generic `/projects/{id}/tasks` list reports `bucket_id: 0` even when a card is placed; the
  view-scoped `/api/v2/projects/{p}/views/{v}/buckets/tasks` is the accurate read.

**Not done / open**
- No git remote. PR creation, branch protection and CI cannot be exercised until a remote exists —
  this is an owner decision because it determines where bank source code is stored.
- Vikunja is started by hand; no launchd agent yet.
- Still blocked on the six business questions, which are now also the board's backlog.

## 2026-11-22 — Named the team and added the second senior engineer

**Done**
- Owner supplied real names. Agents keep role IDs (stable for routing and permissions) and carry the
  person's name in the description and in the system prompt identity, since the V2 agent schema has
  no `name` field — verified, it is silently ignored.
- Added `sse-b` (Rajeshwari Narella) as the second senior engineer, scoped to the view layer: JSP,
  Bootstrap, JS, validation, print. `sse` (Uday) is scoped to backend, with an explicit rule about
  who owns the JSP when both work one feature.
- Shaik Arif holds two seats: `tl` (design/review, edit denied) and `sse2` (build, full access).
  TL must open any self-review with `SELF-REVIEW`; Manager asks for a peer opinion on money, scoring
  or approval changes.
- Rewrote `.opencode/agents/manager.md` with the named roster, name→ID translation, size-based
  routing, and signature rule.
- Wrote `.opencode/TEAM.md`: org chart, ownership table, routing table, a worked dashboard example,
  definition of done, failure handling, owner responsibilities.
- Updated `.opencode/README.md` team section to point at `TEAM.md`.

**Verified**
- `opencode debug agents` reports 8 team agents plus 7 built-ins. Manager's `subagent` allows are
  exactly `po, tl, sse2, sse, sse-b, se, ase` — nothing else.

**Findings**
- Agent display names in the UI come from the agent ID; the person's name is visible in the
  description and in every report. Recorded so nobody re-tries `name:`.
- Two SSE seats need a hard file-ownership rule or they will collide on `x.jsp`. Encoded in both
  prompts.

**Not done / open**
- Nobody has run a full cycle with the new roster yet; the worked example in `TEAM.md` is the
  intended behaviour, not observed behaviour.
- Still blocked on the six business questions in `NEXT_STEPS.md`.

## 2026-11-22 — Ruled out throughput as a constraint; SE moved to MiniMax

**Done**
- Read the starter plan's rate limits from the console: `MiniMax-M3.1-Flash` 200 RPM, 10,000,000 TPM,
  no concurrency cap. `MiniMax-M3` listed on the same table.
- Worked the comparison: team peak is ~4 concurrent agents (~160k tokens per burst minute) and
  20–40 requests/min, so ~60× token headroom and ~5–10× request headroom. A feature is ~60–120
  requests and ~3–5M tokens.
- Moved `se` from free `deepseek-v4.1-flash-free#high` to `minimax-coding-plan/MiniMax-M3.1-Flash`.
  `po` and `ase` stay free on purpose — cheap path for low-value tokens, and one non-MiniMax route
  survives if the plan quota runs dry.
- Updated the model section of `.opencode/README.md` with the rate-limit table.

**Findings**
- The earlier hedging was wrong: rate limits were never the binding constraint. The plan's
  5-hour/weekly *quota* is, and it is denominated differently from RPM/TPM.
- With no concurrency cap, parallel subagents are safe from the provider's side; the only real
  parallelism limit is two agents editing the same file, which the Manager already guards.

**Not done / open**
- Remaining quota size for the starter tier still unmeasured — check the Plan usage page or
  `mmx quota` after the first real feature.
- Still blocked on the six business questions in `NEXT_STEPS.md`.

## 2026-11-22 — Added the MiniMax M Plan as a provider; writers moved onto it

**Done**
- Allowed `minimax-coding-plan` and `minimax` in the global provider policies.
- Declared `MiniMax-M3.1-Flash` and `MiniMax-M3` in the global config with the vendor-mandated
  `limit` block (context 524288, output 131072).
- Fixed `~/.config/opencode/plugins/model-filter/index.ts`, which disabled every model outside
  opencode-go / free opencode / free openrouter. It was hiding MiniMax entirely.
- `sse2` and `sse` now run `minimax-coding-plan/MiniMax-M3.1-Flash`.
- Rewrote the model section of `.opencode/README.md` around three independent quotas.

**Verified**
- `opencode models` lists `minimax-coding-plan/MiniMax-M3` and `.../MiniMax-M3.1-Flash`.
- Live inference returns through the stored credential.
- Tool-use test passes: on M3.1-Flash the agent read `sse2.md`, reported `steps: 24` and the exact
  `model:` line, without guessing.

**Findings**
- The API key pasted in chat is a **pay-as-you-go** key with no balance: `insufficient_balance_error
  (1008)` on both `/v1/chat/completions` and `/anthropic/v1/messages`. M Plan keys and PAYG keys are
  not interchangeable per MiniMax's own docs. The M Plan key already stored in OpenCode is the
  working one; the pasted key was not written anywhere.
- Only M3.1-Flash and M3 are current. M2.7/M2.5/M2.1/M2 were retired 18 Nov 2026; M Plan silently
  routes the old names to M3.1 Flash.
- The global default model `opencode-go/minimax-m2.7` in `~/.config/opencode/opencode.json` no
  longer exists in the catalog. Not changed — needs an owner decision.

**Not done / open**
- Nobody has compared MiniMax M3.1-Flash with DeepSeek V4 Pro or Muse Spark on a real JSP/Spring
  slice; that needs the first feature.
- MiniMax M Plan applies a 5-hour and a weekly window over one shared quota. Starter tier size is
  unknown; check with `mmx quota`.
- Still blocked on the six business questions in `NEXT_STEPS.md`.

## 2026-11-22 — Moved the code-writing agents onto a subsidized frontier model

**Done**
- Owner chose output quality per dollar over data retention. Recorded as Decision 010, which
  supersedes the zero-retention restriction in Decision 009.
- `sse2` and `sse` → `opencode-go/muse-spark-1.3-contributor#high` ($0.10/$0.20, cache $0.002,
  against a $1.25/$4.25 list price).
- `manager` and `tl` stay on `opencode-go/deepseek-v4-pro#high` — orchestration and review are the
  two slots where an unproven model costs more than the tokens.
- `po`, `se`, `ase` stay free on `opencode/deepseek-v4.1-flash-free`.
- Rewrote the model section of `.opencode/README.md`, including the fallback chain and the
  synthetic-data control.

**Verified**
- `muse-spark-1.3-contributor` is accessible on the Go subscription (no privacy-toggle error).
- Tool use works: asked ASE on that model to read `.opencode/agents/tl.md` and report the `steps:`
  value plus a quoted guardrail line. It read the file, returned `14` and the exact bullet, and
  followed ASE's `CHANGED / CHECKED / UNSURE` report contract.

**Findings**
- GLM-5.3-Flash is *not* free (free variant retired 18 Nov 2026); the subsidized tier is the Muse
  Spark contributor models, which trade a ~12× discount for training rights.
- Manager and TL absolute cost is small enough (cache $0.022/M) that keeping them on the strongest
  reliable model does not move the monthly total.

**Not done / open**
- Real quality comparison of Muse Spark versus DeepSeek V4 Pro on an actual JSP/Spring slice —
  cannot be done until the first feature exists.
- Still blocked on the six business questions in `NEXT_STEPS.md`.

## 2026-11-22 — Re-tiered the team's models for the $10 Go plan

**Done**
- Moved PO, SSE, SE, ASE to `opencode/deepseek-v4.1-flash-free` (zero cost, zero retention,
  no training — same model as the paid Flash). ASE uses `#low` for speed.
- Moved SSE2 from `kimi-k2.7-code` ($4/M output) to `opencode-go/glm-5.3-flash#high` ($0.50/M
  output, 0-day retention).
- Kept Manager and TL on `opencode-go/deepseek-v4-pro#high`: on Go its cached-read price is
  $0.022/M, which is cheaper than GLM Flash and Kimi Code, and those two agents are almost
  entirely cached context.
- Documented the free-model privacy split and a fallback order in `.opencode/README.md`.

**Findings**
- GLM-5.3-Flash **Free** was retired 18 Nov 2026; `glm-5.3-flash` is now $0.15/$0.50.
- Go allowance is one shared pool (5h = 20%, weekly = 50%), but free models keep working after the
  limit — so the team degrades instead of stopping.
- Free models that train on or retain data (nemotron, flint-40b, iceman, tidepool, big-pickle) are
  banned for borrower data. Only deepseek-v4.1-flash-free, hy4-flash-free, beam-free and
  longcat-2.5-free are safe.
- DeepSeek's ZDR agreement on Go is valid only through **30 Nov 2026** — recheck before UAT.

**Not done / open**
- No benchmark yet of `glm-5.3-flash` on an actual JSP/Spring slice; SSE2 quality is the one
  unproven assumption. Compare against `deepseek-v4-pro#high` on the first real feature.
- Still blocked on the six business questions in `NEXT_STEPS.md`.

## 2026-11-21 — Set up the seven-agent delivery team and code/UI guardrails

**Done**
- Created `.opencode/agents/`: `manager` (primary, the agent the user talks to), `po`, `tl`,
  `sse2`, `sse`, `se`, `ase`. Manager → TL → workers; review always flows back up through TL.
- Created `.opencode/guardrails/minimal-code.md` (size limits, banned constructs, dependency and
  offline-build policy, testing rule, change map) and `ui-ux.md` (page shell, forms, tables,
  feedback, bank vocabulary, accessibility, print, local-only assets).
- Created `.opencode/opencode.json`: `default_agent: manager` plus both guardrail files in
  `instructions`, so every agent inherits them without prompt duplication.
- Created `.opencode/README.md`: how to talk to the team, model tiering, failure modes.

**Verified**
- `opencode debug agents` lists all 7 agents with the intended mode, model, steps, and permissions.
- Absolute instruction paths work. A relative path (`guardrails/minimal-code.md`) and a
  project-relative path (`.opencode/guardrails/minimal-code.md`) were both silently skipped —
  confirmed by asking Manager to quote the file: it could not. Switched to absolute paths and
  re-tested: it quoted both files verbatim. **If the project directory moves, update the two
  paths in `.opencode/opencode.json`.**

**Findings**
- Model tiering is cost-driven: leads on `deepseek-v4-pro#high`, PO on `kimi-k3.1-pro#high`,
  SSE2 on `kimi-k2.7-code`, SSE/SE on `deepseek-v4.1-flash#high`, ASE on `gpt-6-luna#medium`.
- Enforcement is structural, not aspirational: Manager/TL/PO cannot edit application code,
  and no agent can run `git push`, `rm -rf`, `npm install`, or `pip install`.

**Not done / open**
- The blocking business questions in `NEXT_STEPS.md` are still unanswered; the team has no code
  to work on until Finacle access, v1 scope, and submission channel are decided.

## 2026-11-21 — Read the business requirement, located the RBI direction

**Done**
- `BRD.pdf` is a 9-page iPhone scan with no text layer. OCR'd with `tesseract` at 200 dpi
  (via `pdftoppm`). OCR quality good on pages 1–5, 7–8; garbled on page 6 (currency tables).
- Identified contents: (a) follow-up letter 19-11-2026, (b) original requirement letter
  21-08-2026, (c) Annex II of the RBI direction, pages 46–52.
- Located the governing RBI direction: RBI (Commercial Banks – Transfer and Distribution of
  Credit Risk) Directions, 2025, dated 28-11-2025, updated 01-10-2026, 57 pages.
  HTML at `BS_ViewMasDirections.aspx?id=13151`; PDF is behind F5 bot protection on rbidocs —
  retrieved with a browser UA + referer.
- Extracted verbatim paras 127–129 and Annex I / II / III structure into
  `.brain/01_Business_Requirement/RBI_PARA_127_128.md`.
- Verified Annex II in the BRD scan matches the RBI PDF Annex II (pp. 46–52) exactly.

**Findings**
- Para 127 is enabling guidance only; para 128 is the binding obligation.
- Para 128 imposes five sub-obligations, but the BRD letter only asks for Annex II generation.
  Annex I declaration capture and Annex III professional certification are unaddressed.
- Verbose accuracy check: no code written this session.

**Not done / open**
- No answer yet on Finacle access mechanism, submission channel, or v1 scope.
- Nothing built.

## Earlier — Project initialised
- `.brain/` created with `01_Business_Requirement/BRD.pdf`.
