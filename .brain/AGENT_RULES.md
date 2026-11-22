# AGENT RULES — Read Before You Act, Write Back Before You Finish

These rules apply to **every agent** working in this repository. They ensure continuity across context resets and new sessions: **the state of the project lives in `.brain/`, not in anyone's temporary memory**.

---

## 0. Core Behavioral Philosophy (Karpathy Principles)
All agents in this workspace must adhere strictly to Andrej Karpathy's 4 engineering principles (`CLAUDE.md` / `AGENTS.md`):
1. **Think Before Coding (No Silent Assumptions):** State assumptions explicitly. If requirements are ambiguous, stop and ask questions instead of guessing. Surface tradeoffs upfront.
2. **Simplicity First (Minimum Code):** Write the absolute minimum code required to solve the task. Nothing speculative, no premature abstractions, no unasked-for "flexibility".
3. **Surgical Changes (Clean Up Only Your Own Mess):** Touch only what you must. Never refactor or reformat unrelated adjacent code. Clean up only orphaned imports/variables caused by your own edits.
4. **Goal-Driven Execution (Explicit Verification):** Define measurable success criteria upfront (`[Step] → verify: [check]`). Loop and verify independently until criteria pass.

---

## 1. Read First (at start of each session or after context reset)
1. [`START_HERE.md`](START_HERE.md)
2. This file (`AGENT_RULES.md`)
3. [`01_STATE/CURRENT_STATE.md`](01_STATE/CURRENT_STATE.md) + [`01_STATE/NEXT_STEPS.md`](01_STATE/NEXT_STEPS.md)
4. Last 3 entries of [`01_STATE/SESSION_LOG.md`](01_STATE/SESSION_LOG.md)
5. [`02_DECISIONS/STANDING_DECISIONS.md`](02_DECISIONS/STANDING_DECISIONS.md)
6. Relevant system docs in `03_SYSTEM/`

*Rule:* **The code is the truth.** If documentation and actual source code disagree, verify the code and update the documentation in the same turn.

---

## 2. Working Rules & Constraints
* **Owner Profile:** Avanish Gupta (IT Officer, SWDC Cell, DIT Head Office). Explain in clear words, state file paths clearly, and provide explicit context.
* **No Mock Data in Code:** Business code must use real SQL / repository patterns. Mock/test data belongs strictly to SQL init scripts (`h2-init.sql`).
* **Bank CSP Rules:**
  * Strict Content Security Policy: **NO inline JavaScript** (`<script>...</script>`), **NO inline CSS** (`style="..."`).
  * All script handlers must use element IDs and add listeners in external JS files located in `assets/js/`.
* **Zero Production Secrets:** Never hardcode passwords, encryption keys, or real employee credentials in `.brain/` or git repositories. Use environment variables or bank config management.
* **Dual Profile Pattern (from uco-realty-v3):**
  * `local` / `h2` mode: in-memory H2 DB runs out-of-the-box on Mac laptop without external VPN.
  * `uat` / `oracle` mode: connects to Oracle 19c (`ojdbc11`) with native queries.

---

## 3. Write Back Protocol (Mandatory Before Ending Any Turn)
Before writing your closing response to the user whenever you changed code, configuration, or gathered durable project knowledge:

| Step | Target File | Action |
| :--- | :--- | :--- |
| **1** | `01_STATE/CURRENT_STATE.md` | Edit in-place to reflect the exact current truth, status, and known gaps. |
| **2** | `01_STATE/NEXT_STEPS.md` | Check off completed items; add new tasks; specify the exact immediate next task. |
| **3** | `01_STATE/SESSION_LOG.md` | **Append** a new dated session log entry (User request, actions taken, files modified, next steps). |
| **4** | `02_DECISIONS/Decisions.md` | If an architectural, technical, or business decision was made, append a new numbered entry. |
| **5** | `02_DECISIONS/STANDING_DECISIONS.md` | Update if the new decision alters active standing rules. |
| **6** | `03_SYSTEM/*` | Update corresponding system specs (Requirements, Architecture, Gap Analysis, Schema). |

---

## 4. Decision Template for `02_DECISIONS/Decisions.md`
```markdown
### Decision <Number>: <Title>
* **Date:** YYYY-MM-DD
* **Context:** Why was this decision needed?
* **Decision:** What was chosen?
* **Consequences / Rationale:** Pros, cons, impact on existing components.
* **Status:** In Force / Superseded by Decision <X>
```
