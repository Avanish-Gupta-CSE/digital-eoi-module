# GEMINI.md — Agent Behavioral & Engineering Guidelines

Behavioral guidelines to reduce common LLM coding mistakes, based on Andrej Karpathy's core principles, adapted for Antigravity agents in the **Digital EoI Module (UCO Bank)** workspace.

> **Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

---

## The 4 Core Karpathy Principles

### 1. Think Before Coding
**Don't assume. Don't hide confusion. Surface tradeoffs.**
- **Explicit Assumptions:** State your assumptions explicitly before touching code. If uncertain, ask.
- **Multiple Interpretations:** If multiple architectural or implementation interpretations exist, present them — don't pick silently.
- **Push Back on Complexity:** If a simpler, more robust approach exists, say so. Push back when warranted.
- **Stop When Confused:** If requirements or bank specifications are ambiguous, stop. Name what is confusing. Ask before executing.

### 2. Simplicity First
**Minimum code that solves the problem. Nothing speculative.**
- **No Scope Creep:** Do not write features or capabilities beyond what was asked.
- **No Premature Abstraction:** No helper functions or abstractions for single-use code.
- **No Speculative Flexibility:** No unrequested configurability, generic frameworks, or hooks.
- **No Paranoid Coding:** No error handling or defensive guards for impossible scenarios.
- **Conciseness Test:** If you write 200 lines and it could be 50, rewrite it.
- **Senior Engineer Check:** Ask yourself: *"Would a senior bank software architect say this is overcomplicated?"* If yes, simplify.

### 3. Surgical Changes
**Touch only what you must. Clean up only your own mess.**
- **No Orthogonal Edits:** When editing existing code, don't "improve," reformat, or refactor adjacent lines, comments, or classes that aren't broken.
- **Respect Established Style:** Match existing codebase conventions, even if you would personally write it differently.
- **No Unsolicited Cleanup:** If you notice unrelated dead code or legacy issues, mention them in notes — do not delete or modify them.
- **Orphan Cleanup:** Only remove imports, variables, or functions that *your own* edits made unused.
- **Diff Traceability:** Every single changed line must trace directly back to the user's explicit request.

### 4. Goal-Driven Execution
**Define success criteria. Loop until verified.**
- **Transform Vague Tasks:** Turn high-level instructions into concrete, verifiable outcomes:
  - *"Fix the scoring bug"* → *"Write a unit test reproducing the Part VI rating mismatch, verify failure, fix logic, verify pass."*
  - *"Add validation"* → *"Write tests for invalid inputs, verify rejection, then implement."*
  - *"Refactor X"* → *"Ensure build & tests pass before and after changes."*
- **Verifiable Plan for Multi-Step Tasks:** Always state a brief, measurable sequence:
  ```
  1. [Step] → verify: [check]
  2. [Step] → verify: [check]
  3. [Step] → verify: [check]
  ```
- **Strong vs. Weak Criteria:** Strong success criteria let you verify and iterate independently; weak criteria ("make it work") lead to confusion and hallucinations.

---

## Workspace & Bank Constraints (UCO Bank — Digital EoI Module)

Every agent operating in this workspace must adhere to the following mandatory constraints:

1. **Persistent Memory First (`.brain/`):**
   - Read `.brain/START_HERE.md` and `.brain/AGENT_RULES.md` before taking actions in new sessions or context resets.
   - **The code is the truth**: If documentation and code conflict, verify the code and update the documentation in the same turn.

2. **Bank CSP Compliance:**
   - **Zero Inline JS:** No `<script>` tags inside JSP views. All handlers must attach via element IDs in external files under `assets/js/`.
   - **Zero Inline CSS:** No `style="..."` attributes in JSP templates. Use Bootstrap 5 classes or `assets/css/style.css`.

3. **Dual Profile Pattern (from `uco-realty-v3`):**
   - `h2` profile: In-memory standalone development on developer laptops without bank VPN.
   - `uat` / `oracle` profile: Oracle 19c (`ojdbc11`) for bank deployment.

4. **Zero Production Secrets & No Mock Data in Business Code:**
   - Real business logic must use JPA/SQL repository patterns; mock data belongs strictly in `h2-init.sql`.
   - Never commit or log credentials, employee passwords, or bank tokens.

5. **Mandatory Write-Back Protocol:**
   - Before completing any turn that changes code, configuration, or learns project knowledge:
     - Update `.brain/01_STATE/CURRENT_STATE.md`
     - Update `.brain/01_STATE/NEXT_STEPS.md`
     - Append entry to `.brain/01_STATE/SESSION_LOG.md`
     - Update `.brain/02_DECISIONS/` if architectural/design choices were made.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, zero speculative abstractions, clarifying questions asked before writing code, and every change is verified against clear success criteria.

---

## Security non-negotiables (every agent, every tool)

Full rules and copyable patterns: `.opencode/guardrails/security-and-integrity.md`, `.opencode/patterns/`,
past mistakes in `.opencode/LESSONS.md`. In short: escape every printed `${}` with `<c:out>`; every
`<form>` carries the `_csrf` field; check role **and** status on the server (status inside the UPDATE);
child-row SQL also matches the parent id; new write URLs go into `ReportLockConfig`; state changes write
an audit row; no secrets or default passwords; write the negative tests. Run
`sh scripts/check-guardrails.sh` and `mvn -o -q test` before saying done.
