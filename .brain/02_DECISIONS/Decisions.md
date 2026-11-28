# DECISIONS LEDGER — Digital EoI Module

> **Full numbered history of all technical, architectural, and business decisions.**  
> Later decisions override earlier ones. For current rules in force, see [`STANDING_DECISIONS.md`](STANDING_DECISIONS.md).

---

### Decision 001: Project Scope & Core Purpose
* **Date:** 2026-11-20
* **Context:** HO Flagship Corporate Credit Department requested automated generation of Exchange of Information under Consortium/Multiple Banking arrangements as per RBI directions (Paras 127 & 128).
* **Decision:** Build a dedicated internal banking web application named **"Digital EoI Module"** to automate data capture from Finacle, provide a Maker-Checker workflow, calculate Part VI conduct ratings, and generate RBI Annex II reports.
* **Status:** In Force.

---

### Decision 002: Architectural Baseline from `uco-realty-v3` (`UcoRealtyStaff`)
* **Date:** 2026-11-20
* **Context:** A battle-tested UCO Bank internal intranet portal exists at `../uco-realty-v3` (`UcoRealtyStaff`).
* **Decision:** Adopt the proven architectural and engineering standards from `UcoRealtyStaff`:
  * Java / Spring Boot backend packaged as an executable WAR for external Tomcat 11.
  * Server-rendered JSP + JSTL with Bootstrap 5 frontend (no Node.js build pipeline).
  * Strict bank Content Security Policy (CSP): no inline scripts, no inline styles, event handlers in external `.js` files.
  * UCO Bank official theme, logo, and responsive styling.
  * Single intranet portal for bank staff (no external customer portal needed).
* **Status:** In Force.

---

### Decision 003: Database Strategy (Oracle 19c with H2 Local Fallback)
* **Date:** 2026-11-20
* **Context:** UCO Bank production runs on Oracle 19c. However, developers working locally on laptops without bank VPN need to run and test smoothly.
* **Decision:** 
  * Production/UAT profile uses Oracle 19c (`ojdbc11`) with native SQL queries.
  * Local development profile uses in-memory H2 database with schema initialized via `h2-init.sql`.
* **Status:** In Force.

---

### Decision 004: Authentication & Role-Based Access Control (RBAC)
* **Date:** 2026-11-20
* **Context:** Bank security policies prohibit local user credentials or plain password tables.
* **Decision:** 
  * Authenticate users via UCO Bank Single Sign-On (SSO: `/entry?Value=<encrypted token>`).
  * Resolve employee details and role from Bank HRMS tables at login based on SOL ID and Scale/Cadre:
    * **Maker:** Credit Officers / Branch Staff (Scale 1–3).
    * **Checker:** Branch Heads / Approving Authorities (Scale 4+ or designated sanctioning officers).
    * **Auditor / HO Viewer:** Read-only access to quarterly reports across all zones.
  * Provide standard dev test-login with mock OTP for local/UAT development.
* **Status:** In Force.

---

### Decision 005: Finacle Integration Abstraction Layer
* **Date:** 2026-11-20
* **Context:** Finacle data may initially be provided via read-only Oracle database views (ODS/replica) or later via Finacle Integrator (FI) REST/SOAP APIs.
* **Decision:** Define a clear Java interface (`FinacleIntegrationService`) to decouple core business logic from the exact transport layer. Provide a `FinacleDbViewService` implementation alongside a `MockFinacleService` for offline dev/testing.
* **Status:** In Force.

---

### Decision 006: RBI Part VI Conduct Scoring Rules Engine
* **Date:** 2026-11-20
* **Context:** The BRD specifies concrete rules for rating borrower conduct as Good, Satisfactory, or Below Par based on overdrawing frequency, adjustment time, LC/BG defaults, and covenant compliance.
* **Decision:** Implement these rules in an automated business service (`EoIConductScoringService`). The system computes the recommended rating automatically, while allowing the Maker to add mandatory qualitative remarks if overriding any computed parameter.
* **Status:** In Force.

---

### Decision 007: Adoption of Karpathy Behavioral Guidelines for AI Agents
* **Date:** 2026-11-20
* **Context:** AI agents frequently suffer from common failure modes: silent assumptions, premature over-engineering, orthogonal sweeping refactoring, and lack of explicit goal verification.
* **Decision:** Adopt and enforce Andrej Karpathy's 4 core behavioral guidelines across all agents working in this repository:
  1. *Think Before Coding* (explicit assumptions, surface tradeoffs, ask when ambiguous).
  2. *Simplicity First* (minimum code, zero speculative abstractions or flexibility).
  3. *Surgical Changes* (touch only what's needed, zero orthogonal refactoring, diff traceability).
  4. *Goal-Driven Execution* (explicit verification criteria, `[Step] → verify: [check]` loops).
  Implemented natively via `CLAUDE.md`, `AGENTS.md`, `GEMINI.md`, `.agents/rules/karpathy.md`, and integrated into `.brain/AGENT_RULES.md`.
* **Consequences / Rationale:** Prevents code bloat, keeps Git diffs minimal and reviewable, eliminates hallucinations, and ensures bank-grade discipline.
* **Status:** In Force.

---

### Decision 008: Seven-Agent Delivery Team with Binding Code/UI Guardrails
* **Date:** 2026-11-21
* **Context:** The module must be maintained later by a small team, possibly without internet access, and by developers who are not senior. Prompt-level reminders alone were not enough: agents under time pressure drift toward clever code, new dependencies, and CDN assets.
* **Decision:** Model the working team as OpenCode agents under `.opencode/agents/`, with the hierarchy and constraints enforced by configuration rather than by instruction:
  * `manager` is the only primary agent and the only agent that writes `.brain/`. It plans, delegates, and reports; it cannot edit application code.
  * `po` owns requirements and screen wording, docs only. `tl` owns design and review, no feature code.
  * `sse2` sets the pattern, `sse` builds one slice, `se` makes one specified change, `ase` handles tiny exact edits.
  * Two guardrail files (`minimal-code.md`, `ui-ux.md`) load into every agent via `instructions`. Hard size limits, a banned-construct table, a no-new-dependency policy, the `mvn -o` offline-build requirement, and mandatory empty/loading/error states.
  * Structural enforcement: Manager/TL/PO have `edit` denied outside `.brain/`; all agents are denied `git push`, `rm -rf`, `npm install`, and `pip install`.
* **Consequences / Rationale:** Simplicity and UI quality become review gates instead of opinions; the hierarchy stays flat (no agent launches agents except Manager), so cost and blast radius stay predictable. Cost is tiered by model: leads on `deepseek-v4-pro`, workers on `deepseek-v4.1-flash`, ASE on `gpt-6-luna`.
* **Known constraint:** `instructions` entries must be absolute paths — relative paths were silently skipped and verified as not loading. Moving the project requires updating `.opencode/opencode.json`.
* **Status:** In Force.

---

### Decision 009: Model economics for the $10 OpenCode Go plan
* **Date:** 2026-11-22
* **Context:** The Go subscription provides one shared monthly allowance (5-hour cap = 20%, weekly = 50%). Premium output tokens are the expensive part: Kimi K3.1 Pro bills $10/M output and Kimi K2.7 Code $4/M, while the actual work those agents do is deliberate and simple. Free coding models are now available, and the project currently has no application code and will use synthetic data only (`h2-init.sql`).
* **Decision:** Match model price to the marginal value of output tokens:
  * Manager and TL keep `opencode-go/deepseek-v4-pro#high`. They carry the largest cached contexts, and Go's cached-read price ($0.022/M) makes them cheaper in practice than GLM Flash or Kimi Code.
  * PO, SSE, SE, ASE move to `opencode/deepseek-v4.1-flash-free` (zero cost, zero retention, no training — the same model as the paid Flash). ASE runs at `#low`.
  * SSE2 moves to `opencode-go/glm-5.3-flash#high`, an 8× reduction in output price versus Kimi K2.7 Code.
  * Free models that train on or retain data (Nemotron free, Flint 40B free, Iceman free, Tidepool, Big Pickle) are banned for borrower data. Allowed free models: `deepseek-v4.1-flash-free`, `hy4-flash-free`, `beam-free`, `longcat-2.5-free`.
* **Consequences / Rationale:** Four of seven agents cost nothing; the highest-output agent dropped 8×. The team degrades to free models when the Go allowance is exhausted rather than stopping. Risk accepted: free tiers are "limited time" and may disappear, so each agent has a documented paid fallback in `.opencode/README.md`; and bank data in UAT must move back to ZDR paid endpoints, especially as DeepSeek's ZDR agreement on Go runs only through 30 Nov 2026.
* **Status:** In Force — SSE2 choice unproven until the first real feature slice is compared.

---

### Decision 010: Output quality over data-residency — subsidized models approved for the pilot
* **Date:** 2026-11-22
* **Context:** Decision 009 restricted the team to zero-retention models, which excluded the cheapest high-capability tier on Go. The owner has stated that data retention and training on prompts are acceptable, and that output quality per dollar is the deciding factor. The project has no application code and the pilot runs entirely on synthetic data in `h2-init.sql`.
* **Decision:** Prioritise output quality per dollar and permit models that retain or train on request data:
  * `sse2` and `sse` move to `opencode-go/muse-spark-1.3-contributor#high`. This is a subsidized frontier model at **$0.10 in / $0.20 out** (cache $0.002) against a $1.25 / $4.25 list price — a ~12× subsidy that makes its output 10× cheaper than DeepSeek V4 Pro while being a much larger model.
  * `manager` and `tl` stay on `opencode-go/deepseek-v4-pro#high`: orchestration reliability and review judgement are the two places where an unproven model would hurt most, and their absolute cost is pennies per session.
  * `po`, `se`, `ase` stay on the free `deepseek-v4.1-flash-free`.
  * Free models that retain or train on data (Nemotron free, Flint 40B free, Iceman free, Tidepool, Big Pickle) are now **permitted** under this decision.
* **Verification before adoption:** `muse-spark-1.3-contributor` was tested on 2026-11-22 and confirmed to answer, to call tools correctly (read a file and quote exact content), and to follow an agent's required report format.
* **Compensating control:** the pilot only ever sees synthetic data from `h2-init.sql` — no borrower names, facility numbers, or Finacle extracts. Before UAT ingests real data, move the contributors back to `deepseek-v4-pro` (0-day retention) and re-open this decision.
* **Consequences / Rationale:** The expensive output tokens now sit on the cheapest capable model, so the $10 allowance covers far more work per month. Risk accepted knowingly: prompts from the pilot are usable for model training, and a contributor endpoint can be withdrawn by the provider at any time — the documented fallback chain is `free flash → muse contributor → deepseek-v4-pro#high`.
* **Status:** In Force. Supersedes the zero-retention restriction in Decision 009 and its SSE2 model choice.

---

### Decision 011: MiniMax M Plan adopted for the code writers
* **Date:** 2026-11-22
* **Context:** The owner holds a monthly MiniMax M Plan subscription, whose quota is separate from the $10 OpenCode Go allowance. MiniMax offers `MiniMax-M3.1-Flash` and `MiniMax-M3`, both frontier coding models with a 1M context window. The project was previously routing its two highest-output agents (SSE2, SSE) through OpenCode Go.
* **Decision:** `sse2` and `sse` use `minimax-coding-plan/MiniMax-M3.1-Flash`. Manager and TL stay on `opencode-go/deepseek-v4-pro#high`; PO, SE and ASE stay on free `deepseek-v4.1-flash-free`.
  * Rationale for not moving the leads: the team is deliberately spread across three independent quotas — MiniMax M Plan (writers), Go (leads), free models (support roles). A writer running dry is a two-line model swap; an orchestrator running dry stalls the whole session.
  * Models declared with `context: 524288` / `output: 131072` (vendor-recommended 512K client window; the model supports 1M). Both limits are mandatory — OpenCode refuses to start if either is missing.
* **Infrastructure fix:** the global `model-filter` plugin (`~/.config/opencode/plugins/model-filter/index.ts`) disables every model outside `opencode-go`, free `opencode` models, and free `openrouter` models. It was silently hiding all MiniMax models even after the provider was allowed by policy and declared in config. `minimax-coding-plan` was added to the keep list. The global `policies` allowlist also gained `minimax-coding-plan` and `minimax`.
* **Credential finding:** a MiniMax *pay-as-you-go* key and a MiniMax *M Plan* key are not interchangeable. The key supplied in chat returned `insufficient_balance_error (1008)` on both the OpenAI-compatible and Anthropic-compatible endpoints. The M Plan key already stored in OpenCode works, and is the one in use. Nothing was written to disk from the supplied key, since it is a credential shared in plain text.
* **Verified:** `opencode models` lists both MiniMax models; a live inference call returns; a tool-use test (read a file, quote exact content) passes; the provider resolves through the integration's Anthropic-compatible base URL `https://api.minimax.io/anthropic/v1`.
* **Consequences / Rationale:** The two agents that produce most output tokens now cost nothing marginal within the plan quota. MiniMax applies a 5-hour and a weekly window over one shared quota, so a heavy day can exhaust it; the documented fallback is `opencode-go/muse-spark-1.3-contributor`.
* **Observed rate limits (starter plan, 2026-11-22):** `MiniMax-M3.1-Flash` — 200 RPM, 10,000,000 TPM, no concurrency cap. Team peak is ~4 concurrent agents (~160k tokens per burst minute) and 20–40 requests/min, i.e. ~60× token headroom and ~5–10× request headroom. Throughput is therefore not a constraint and parallel subagents need no throttling; the binding limit is the 5-hour/weekly quota, not RPM.
* **Updated 2026-11-22:** with throughput ruled out as a constraint, `se` also moved to `MiniMax-M3.1-Flash` (it writes real application code). `po` and `ase` deliberately stay on free models to preserve a non-MiniMax path and to stop trivial work consuming plan quota. Assignment rule: put output-heavy roles where tokens are flat-rate, keep the orchestrator where failure cannot stall the session.
* **Status:** In Force.

---

### Decision 012: Named roster, eight seats, two-hat lead
* **Date:** 2026-11-22
* **Context:** The owner named the team and expects to address people by name. The roster has seven people but eight seats, because the tech lead and the implementation lead are the same person (Shaik Arif), and there are two senior engineers.
* **Decision:**
  * Agent IDs stay role-based (`manager`, `po`, `tl`, `sse2`, `sse`, `sse-b`, `se`, `ase`) because routing, permissions and prompts reference roles. Each person's name is carried in the agent `description` and in the system-prompt identity line, and every deliverable is signed by the person who did the work.
  * `name:` is not a valid V2 agent field — it is silently ignored, confirmed against `opencode debug agents`. Do not reintroduce it.
  * `sse` (Uday Kiran Sayaboyina) owns the backend half; `sse-b` (Rajeshwari Narella) owns the view half. When both work one feature, the JSP belongs to `sse-b` and the controller/service/repository to `sse`. Never the same file.
  * Shaik Arif keeps two seats: `tl` (design and review, `edit` denied) and `sse2` (build, full access). Two seats keep review from becoming self-review. A verdict on his own build must open with `SELF-REVIEW`, and the Manager requests a peer opinion from `sse`/`sse-b` for anything touching money, scoring, or approval.
* **Consequences / Rationale:** The owner can say "ask Krishna" or "get Shaik to review" and routing still works through the Manager. The two-SSE split doubles throughput on a feature without file collisions. Cost of the split: two prompts to keep in sync, and an explicit file-ownership rule that must be enforced by the Manager.
* **Status:** In Force. Intent documented in `.opencode/TEAM.md`; not yet exercised on a real feature.

---

### Decision 013: Local Vikunja board and a PR-gated git workflow
* **Date:** 2026-11-22
* **Context:** The owner asked for an open-source scrum board integrated with the agent team and with git, so the agents behave like an enterprise team: branch, PR, review, merge.
* **Decision:**
  * Repository initialised on `main` with `CONTRIBUTING.md` defining the branch model, commit convention and merge conditions. CI (`guardrails` + `mvn test` once a `pom.xml` exists) gates every PR.
  * Enforcement is structural: project policies deny every agent `git push` to `main`/`master` and any force push; builders are denied `gh pr merge` and `gh pr review`; only the Tech Lead may push branches, open PRs, review and squash-merge; the Manager may only read PR state.
  * Board: **Vikunja v2.8.0** (AGPL), native arm64 binary, SQLite, bound to `127.0.0.1:3456`, no Docker. Project "Digital EoI" with `Backlog → Design → In Progress → Review → Done`. Integration uses Vikunja's first-party MCP server at `/api/v2/mcp`, so no custom integration code exists.
  * Rejected: Taiga (upstream dormant since 2023), Focalboard (redirects), Plane (Postgres + Redis + MinIO; too heavy for one laptop), Kanboard (kanban only, JSON-RPC, no first-party MCP). Rejected Docker Desktop on licence grounds.
  * Secrets: the agent API token lives in the global OpenCode config (mode 600) and `~/vikunja/api-token.txt` (mode 600). It is never written into this repository. The BRD scan is git-ignored so a bank document cannot travel with a remote.
* **Consequences / Rationale:** The board is state, `.brain` is the record. Cards and PRs give the owner visibility without reading code, and the merge rules are enforced by configuration rather than convention. The MCP path avoids maintaining a bespoke CLI.
* **Open:** no git remote, so PR and merge steps are untested end to end; hosting bank source code is an owner decision.
* **Status:** In Force — board verified working; PR flow verified only up to local commit.

---

### Decision 014: GitHub Issues + Projects as the board; Vikunja retired
* **Date:** 2026-11-22
* **Context:** The owner confirmed that bank code on GitHub is acceptable and asked for GitHub Projects. The machine has 7.2 GB free on a 228 GB disk, so local services are a real cost (Vikunja was 119 MB). GitHub's branch protection is unavailable on private repos without Pro.
* **Decision:**
  * Private repository `Avanish-Gupta-CSE/digital-eoi-module` is the remote. `main` is the release branch.
  * The board is GitHub Issues; the column is a `status:` label (`backlog`, `design`, `in-progress`, `review`, `done`). A Project board view groups by those labels.
  * Labels were chosen over the Project's native Status field because they work from plain `gh issue` commands without GraphQL node ids, and survive the Project being re-created.
  * Vikunja, its SQLite database and its MCP server are removed. The six blockers are re-created as issues #1–#6.
  * Enforcement on the free plan: agent policy in `.opencode/opencode.json` plus a committed `.githooks/pre-push` that rejects pushes to `main`/`master`. Recorded explicitly as bypassable with `--no-verify` — it is not server-side protection.
* **Consequences / Rationale:** Zero local footprint, native issue↔PR linking (`Closes #n`), and the board lives where the code lives. Cost: the board requires internet, and server-side protection needs Pro.
* **Open:** the `gh` token needs `project,read:project` scope before the Project board view can be created; that is an interactive owner action.
* **Status:** In Force.

---

### Decision 015: Public repository with server-side branch protection
* **Date:** 2026-11-22
* **Context:** Decision 014 recorded that branch protection needs GitHub Pro on private repos, leaving only the agent policy and a local hook. The owner instructed that the repository be made public, which makes protection available on the free plan.
* **Decision:**
  * `Avanish-Gupta-CSE/digital-eoi-module` is **public**. Publishing was gated on a secret scan of tracked files: no keys, tokens, passwords, private keys, PDFs or binaries. The BRD scan remains git-ignored.
  * Branch protection on `main`: pull request required, required status check `guardrails`, `enforce_admins: true`, force-push and deletion blocked, `required_approving_review_count: 0`.
  * Zero required approvals is deliberate: a solo developer must not be locked out, while admins are still bound by the PR and CI gate.
  * The local `.githooks/pre-push` and the agent policy in `.opencode/opencode.json` remain as fast-fail layers, not as the primary control.
* **Consequences / Rationale:** Direct writes to `main` are now impossible for everyone, including the owner and every agent. Verified by a 409 from the contents API and by a policy block on `git push`. Cost: the repository, its issues, and `.brain` notes are public; the owner accepted this and no borrower data or Finacle extracts exist in the repo.
* **Status:** In Force.

---

### Decision 016: First slice — Spring Boot 4.1.3 + Java 25, plain JDBC, unknowns as config
* **Date:** 2026-11-22
* **Context:** The six business questions block the *content* of the module but not its shape. Java 25 is the installed JDK; Spring Boot 3.x does not officially support it. The first screen is read-only and reads one table.
* **Decision:**
  * Spring Boot **4.1.3** on Java 25, WAR packaging, Maven. WAR so the same artifact runs with `java -jar` and deploys to Tomcat 11.
  * `spring-boot-starter-jdbc` with `JdbcTemplate` for this slice, **not** JPA. One read-only query, no entities, no lazy-loading surprises, and SQL a beginner can read. This departs from the JPA line in STANDING_DECISIONS §2 and is deliberately narrow: revisit when Annex II brings many related tables.
  * Every open question becomes configuration or a seam: `eoi.borrower-threshold-amount`, `eoi.current-quarter`, and `BorrowerRepository` as the only data source. No threshold, quarter or recipient is hardcoded.
  * Bootstrap 5.3.3 vendored under `static/assets/vendor/` with its MIT licence. No CDN, no npm, no asset build step.
* **Consequences / Rationale:** The team can build against a real, running screen today and swap any business answer in with a property change. Plain JDBC keeps the guardrail "a beginner can read the SQL" true. Cost: if Annex II grows into a dozen related tables with maker–checker state, hand-written SQL gets repetitive — that is the recorded trigger to revisit JPA.
* **Verified:** `/dashboard` 200 with 4 borrowers, empty state on an empty quarter, all assets local, `mvn test` 4/4, guardrails script passes.
* **Status:** In Force.

---

### Decision 017: Finish the application on recorded temporary assumptions
* **Date:** 2026-11-27
* **Context:** The six business questions (#1–#6) are still unanswered, but the owner has directed that the application be completed now on explicit temporary assumptions, each contained behind a property or an interface so a real bank answer is a small swap rather than a rewrite. This decision supersedes the "stay blocked" posture of Decision 013/014-era notes; it does not guess silently — every assumption is written down here.
* **Temporary assumptions (binding until the bank answers):**
  1. **Finacle (#1).** A clean `FinacleDataSource` interface, with an H2/mock implementation returning simulated CIF, IRAC classification, facilities, limits, balances, overdrawing counts and stock-statement dates. "Sync from Finacle" must work end to end on the mock. No real Finacle client is written or implied.
  2. **v1 scope (#2).** Build the complete Annex II, Parts I–VI, per the annex (RBI/DOR/2025-26/159, pp. 46–52). Annex I and Annex III remain out of scope.
  3. **Submission (#3).** A print/PDF-ready Annex II screen plus a zero-dependency CSV export. **No new Maven dependency** may be added to achieve this without the owner's explicit approval.
  4. **Calendar (#4) and threshold (#5).** Fixed quarter-ends driven by `eoi.current-quarter` and `eoi.borrower-threshold-amount`. Both stay properties; neither is hardcoded.
  5. **Non-Finacle data (#6) and Part VI.** Full maker entry forms, "prefill from previous quarter", and **advisory** auto-scoring in Part VI (an officer may override with remarks). The NPA rule is mandatory and not overridable: where a contract is classified NPA and still NPA as on the exchange date, the grade is Below Par.
* **Schema consequence (Part IV, 8 columns).** `eoi_derivative.npa_restructured_amount` is split into **`npa_amount`** and **`restructured_notional_amount`**, matching the annex's two separate columns ("Amount of contracts classified as NPA" and "Notional Amount of outstanding contracts which have been restructured", p. 48). Applies to `schema-annexii-exposure.sql`, `h2-init.sql` and `SchemaTest`. **Delivered in PR #18, merge `dc6e789`.**
* **Oracle migration (required before UAT).** PR #18 changes the H2 path, which recreates the table; CI therefore does not prove Oracle. UAT must run:
  ```sql
  ALTER TABLE eoi_derivative ADD (npa_amount NUMERIC(18,2), restructured_notional_amount NUMERIC(18,2));
  ALTER TABLE eoi_derivative DROP COLUMN npa_restructured_amount;
  ```
  Recorded here because the merged PR body does not carry this risk section.
* **Provenance note (resolved 2026-11-27):** `.brain/01_Business_Requirement/RBI_Circular.pdf` was replaced by the owner at 11:38 IST with the official 57-page `RBI/DOR/2025-26/159` (*Commercial Banks – Transfer and Distribution of Credit Risk Directions, 2025*). The earlier 11:28 entry referred to an older file before replacement. Page 48 of `159` confirms Part IV has 8 columns (`Amount of contracts classified as NPA` and `Notional Amount of outstanding contracts which have been restructured` are two separate columns).
* **Consequences / Rationale:** Work proceeds without inventing bank rules: every assumption is visible, reversible and confined. Cost: some built behaviour will be replaced once the bank answers (#1 especially), and Part VI scoring is advisory until the grading discipline question is settled. This is accepted deliberately to finish the product.
* **Status:** In Force, temporary by construction — each numbered assumption is retired by the bank answer that replaces it.

---

### Decision 018: Pareto model tiering & step-budget tuning across the 8-seat team
* **Date:** 2026-11-28
* **Context:** To maximize throughput and first-pass accuracy with minimal cost across the OpenCode Go ($10), MiniMax M Plan, and Free buckets (and avoid mid-task step-limit pauses observed in PRs #18–#24).
* **Decision:**
  * `tl`: stays on `opencode-go/deepseek-v4-pro#high` (`steps: 18`, up from 14).
  * `manager`: moved to `opencode/deepseek-v4.1-flash-free#high` ($0, preserves Go allowance).
  * `sse2`: upgraded to `minimax-coding-plan/MiniMax-M3` ($0 marginal on M Plan, stronger pattern setter).
  * `sse` & `sse-b`: keep `minimax-coding-plan/MiniMax-M3.1-Flash` ($0 marginal, `steps: 22`, up from 18).
  * `se`: moved to `opencode-go/muse-spark-1.3-contributor#high` ($0.10/$0.20, `steps: 18`, up from 12).
  * `po` & `ase`: keep `opencode/deepseek-v4.1-flash-free#high` and `#low` ($0).
* **Verified:** All chosen model/variant strings tested live via `opencode run --model ...` (`OK`).
* **Status:** In Force.

---

### Decision 018: Part V arithmetic shown literally; across-currency total withheld until FX exists
* **Date:** 2026-11-27
* **Context:** The Annex II report view must show Part V (un-hedged foreign currency exposure) with its net positions. The Tech Lead asked for two sign-offs before coding: the sign convention, and whether an across-all-currencies total is shown.
* **Decision:**
  * **Arithmetic follows the annex literally.** Per currency: short-term net = (a) long − (b) short; long-term net = (a) long − (b) short; overall per currency = I − II, i.e. short-term net − long-term net. That is the printed formula `Overall Net Position (I − ll)`, not an economic interpretation of it. It is implemented once, in `ReportService#netExposures`, with the annex reference in a comment.
  * **The across-all-currencies total is not displayed.** It is a raw sum of different currencies and is meaningless without FX rates, which are out of scope (Decision 017). The screen shows per-currency rows and a short note that the consolidated figure needs FX rates; the column is not printed blank without explanation.
  * **Amounts stay in absolute units** and are not run through the Indian rupee formatter for per-currency positions, because a position in USD is not rupees. Part III and IV amounts remain rupees, formatted with `IndianMoney`.
* **Consequences / Rationale:** The report cannot print a wrong regulatory figure, and the missing consolidated value is explained rather than silently omitted. Cost: the form is not visually complete until FX rates are in scope — accepted, and recorded here so it is a decision rather than an oversight.
* **Status:** In Force.
