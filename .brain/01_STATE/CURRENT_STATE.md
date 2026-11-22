# CURRENT_STATE

Last updated: 2026-11-21

## Project
UCO Bank — Digital EoI (Exchange of Information) Module.
Automated generation of credit information exchange under Consortium / Multiple Banking
Arrangements.

## Repository status
- No application code yet. No build system, no pom.xml/package.json.
- Only `.brain/` knowledge base exists.
- Not a git repo.

## Known artefacts
| Path | What it is |
|---|---|
| `.brain/01_Business_Requirement/BRD.pdf` | 9-page scanned PDF (no text layer). Two UCO Bank internal letters + Annex II of the RBI direction. |
| `.brain/01_Business_Requirement/RBI_PARA_127_128.md` | Verbatim regulatory text of paras 127–129, source metadata, and gap analysis. |

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
| `.opencode/agents/*.md` | Eight agents: `manager` (primary/default), `po`, `tl`, `sse2`, `sse`, `sse-b`, `se`, `ase` — see naming table below |
| `.opencode/guardrails/minimal-code.md` | Binding simplicity, dependency, offline-build, testing, change-map rules |
| `.opencode/guardrails/ui-ux.md` | Binding UI/UX, bank vocabulary, accessibility, print, local-asset rules |
| `.opencode/opencode.json` | `default_agent: manager`; both guardrails in `instructions` (absolute paths) |
| `.opencode/TEAM.md` | Org chart, routing table, worked example, definition of done |
| `.opencode/README.md` | Quickstart, model tiering, failure modes |

Team: Nagendra Immadi (`manager`) · Krishna Chankapurai (`po`) · Shaik Arif (`tl` design/review and
`sse2` build) · Uday Kiran Sayaboyina (`sse` backend) · Rajeshwari Narella (`sse-b` view) ·
Vishal Singh (`se`) · Avanish Gupta (`ase`). Manager and TL do not write feature code; only Manager
writes `.brain/`.

Workflow: owner → Manager → (PO | TL) → SSE2/SSE/SSE-2/SE/ASE → TL review. Work flows up for review,
never sideways. Deliverables are signed by the person who did the work.

## Unknowns blocking design
See `.brain/01_STATE/NEXT_STEPS.md`.
