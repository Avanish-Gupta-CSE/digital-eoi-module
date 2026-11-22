# Your dev team (OpenCode)

Seven agents that behave like a small bank delivery team, plus two guardrail files that every agent
must obey.

## How to use it

You talk to **Manager**. It is already the default agent in this project.

| You want | Say |
|---|---|
| A feature built | "Get the quarterly EoI dashboard built. Keep it simple." |
| A requirement written | "Turn Para 128 into user stories with acceptance criteria." |
| A design before code | "Design the maker–checker flow, no code yet." |
| A small fix | "The Due badge says 'Due ' with a space. Get it fixed." |
| A review | "Review what changed yesterday against the guardrails." |

Manager picks the teammates and reports back. You can also switch to any agent directly with
`Tab` (or `/agents`) when you want to bypass the manager.

## The team

Full org chart, workflow and a worked example: **`.opencode/TEAM.md`**. Short version:

| Person | Agent | Job |
|---|---|---|
| Nagendra Immadi | `manager` | plans, routes, reports — the one you talk to |
| Krishna Chankapurai | `po` | requirements, acceptance criteria, screen wording |
| Shaik Arif | `tl` | design, review, build check (cannot edit code) |
| Shaik Arif | `sse2` | builds the hardest part, sets the pattern |
| Uday Kiran Sayaboyina | `sse` | backend: controller, service, repository, tests |
| Rajeshwari Narella | `sse-b` | view: JSP, Bootstrap, JS, validation, print |
| Vishal Singh | `se` | one specified change, 1–3 files |
| Avanish Gupta | `ase` | tiny exact edits: label, class, typo, one field |

Shaik holds two seats so that a review can never quietly become a self-review.

Work flows up for review, never sideways: builder → `tl` → if BLOCKER, back to the file's owner.

Deliverables are signed by the person, e.g. *"Done — Rajeshwari built the view, Uday built the
query, Shaik (TL) reviewed, build green."*

## Guardrails

- `.opencode/guardrails/minimal-code.md` — size limits, banned constructs, dependency policy,
  offline build requirement, definition of done.
- `.opencode/guardrails/ui-ux.md` — page shell, forms, tables, feedback, language, accessibility,
  print, offline assets.

Both are loaded automatically into every agent (`instructions` in `.opencode/opencode.json`). You do
not need to paste them into prompts.

## Models — output quality per dollar on the $10 Go plan

Go gives one shared allowance across paid models (5-hour cap = 20%, weekly = 50%, monthly resets
with the billing period). **Free models keep working after the allowance runs out**, so the team
degrades instead of stopping.

| Agent | Model | Marginal cost | Why this one |
|---|---|---|---|
| Manager | `opencode-go/deepseek-v4-pro#high` | 0.66 / 1.98 (cache 0.022) | the orchestrator must never stall; Go is its own allowance |
| TL | `opencode-go/deepseek-v4-pro#high` | 0.66 / 1.98 (0.022) | the quality gate — not the place to economize |
| PO | `opencode/deepseek-v4.1-flash-free#high` | **free** | wording is bounded by the RBI source; also keeps a non-MiniMax path |
| SSE2 | `minimax-coding-plan/MiniMax-M3.1-Flash` | **$0** (M Plan quota) | frontier coding model, 1M ctx, tool use verified |
| SSE | `minimax-coding-plan/MiniMax-M3.1-Flash` | **$0** (M Plan quota) | same, full slice work |
| SE | `minimax-coding-plan/MiniMax-M3.1-Flash` | **$0** (M Plan quota) | writes real application code; quality matters per token |
| ASE | `opencode/deepseek-v4.1-flash-free#low` | **free** | tiny edits; frees MiniMax quota for real work |

### Why MiniMax carries the writers

The starter plan's rate limits are **200 RPM and 10,000,000 TPM**, with no concurrency cap.

| Team peak | Limit | Headroom |
|---|---|---|
| 4 agents in flight, ~160k tokens in a burst minute | 10M TPM | ~60× |
| 20–40 requests/min in a revision loop | 200 RPM | ~5–10× |

A whole feature is ~60–120 requests and ~3–5M tokens. Throughput is therefore not a constraint, and
parallel subagents are unrestricted. The scarce resource is the **5-hour / weekly quota**, not the
rate — which is why output-heavy roles sit here and the orchestrator does not.

### Three independent quotas, by design

| Allowance | Used by | When it runs out |
|---|---|---|
| MiniMax M Plan (5-hour + weekly window) | SSE2, SSE, SE | swap to `opencode-go/muse-spark-1.3-contributor` |
| OpenCode Go ($10 shared pool) | Manager, TL | session `/models` → free `deepseek-v4.1-flash-free` |
| Free models (no quota) | PO, ASE | always available |

Manager and TL stay on Go deliberately: a writer running dry is a two-line model swap, an
*orchestrator* running dry stalls the entire session.

### MiniMax specifics

`MiniMax-M3.1-Flash` and `MiniMax-M3` are the only two current MiniMax models — M2.7 and older were
retired on 18 Nov 2026 (M Plan silently routes old names to M3.1 Flash). Both are declared with
`context: 524288` / `output: 131072`; the model supports 1M, but 512K is the vendor's recommended
client window and keeps cost and latency sane.

Thinking is always on for M3.1-Flash and cannot be turned off.

Your M Plan key is already stored in OpenCode (`opencode auth list` → *MiniMax Token Plan
(minimax.io)*). A **pay-as-you-go** MiniMax key is a different credential and will fail with
`insufficient_balance_error (1008)` — the plan key comes from the M Plan console page.

### The subsidized tier is the other trick

`muse-spark-1.3-contributor` stays the fallback for the writers: **$0.10 in / $0.20 out** because the
provider trades that discount for training rights, against a $1.25 / $4.25 list price.

### Data policy for this pilot

Per Decision 010, data retention and training are **accepted** for the pilot. The compensating
control is that the pilot only ever sees **synthetic data from `h2-init.sql`** — no borrower names,
no real facility numbers, no Finacle extracts.

Revisit before UAT ingests real Finacle data.

### Watching the spend

```sh
opencode stats      # tokens and cost per session
mmx quota           # MiniMax M Plan remaining quota (MiniMax CLI)
```

The Go console shows allowance used per model.

| Symptom | Fix |
|---|---|
| Manager launches a wrong teammate | tighten the "Your team" table in `manager.md` |

## Rules of engagement

- Manager never edits application code. TL never writes feature code. If you see either doing it,
  that is a bug in the prompt, not a feature.
- Only Manager writes `.brain/`. Subagents hand back content; Manager persists it.
- No agent may add a dependency, a CDN link, or an internet font/image. That is a human decision.
- Work is done when TL reviewed it and the build or test result is known — not when a subagent says so.

## Board and git

**Board:** GitHub Issues on `Avanish-Gupta-CSE/digital-eoi-module`. The column is a label:
`status:backlog → status:design → status:in-progress → status:review → status:done`. A GitHub Project
board groups by those labels; the issues and labels work with or without it.

**Git:** `main` is protected by agent policy plus a local `.githooks/pre-push` hook. Branch → commit
→ push → PR → green CI → TL merges. Full rules in `CONTRIBUTING.md`; PR checklist in
`.github/PULL_REQUEST_TEMPLATE.md`; CI in `.github/workflows/ci.yml`.

Install the hook once per clone:

```sh
git config core.hooksPath .githooks
```

GitHub's own branch protection needs Pro on private repos, so on the free plan the hook and the agent
policy are the enforcement.

## Two things to know

**Paths are absolute.** `.opencode/opencode.json` points at the two guardrail files by full path,
because relative entries were silently skipped (verified). If you move or rename this project, edit
those two lines. Everything else moves with the folder.

**Check the team loads** after any edit:

```sh
opencode debug agents    # should list manager, po, tl, sse2, sse, se, ase
```

## When something goes wrong

| Symptom | Fix |
|---|---|
| Manager launches a wrong teammate | tighten the "Your team" table in `manager.md` |
| Work is over-engineered | add the specific rule to `minimal-code.md` — rules beat reminders |
| UI looks developer-made | add the concrete pattern to `ui-ux.md` |
| Too slow / too expensive | lower that agent's `steps`, or move it to a cheaper model |
| Agent drifts from bank wording | point PO at the RBI file, and add the term to `ui-ux.md` |

## Offline note

OpenCode itself needs internet. The **application** does not, and must not: all assets local,
`mvn -o` build from a pre-filled repository. The guardrails enforce this on every screen.
