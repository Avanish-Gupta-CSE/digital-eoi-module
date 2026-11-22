# SESSION LOG

<!-- Append new entries at the top, newest first. -->

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
