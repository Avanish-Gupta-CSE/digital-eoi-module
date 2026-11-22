# Your team — who does what, and how work moves

Seven people, eight agent seats. Read this once; after that you only talk to Nagendra.

---

## 1. Who you talk to

**Nagendra Immadi** is the Manager and the default agent. You describe the goal; he plans it,
assigns it, chases it, and reports back. You never need to address anyone else directly.

You can still address people by name — Nagendra translates:

```
"Ask Krishna to write the story for the quarterly dashboard."
"Get Shaik to review yesterday's changes."
"Have Vishal fix the Due badge spacing."
"Nagendra, where are we?"
```

If you want to bypass him, `Tab` switches you to any agent directly. Useful for a one-line fix
(Avanish), or a review-only pass (Shaik as `tl`).

---

## 2. The org chart

```
                        YOU (owner: decisions, approvals)
                                    │
                          Nagendra Immadi — Manager
                          plans, routes, reports, writes .brain/
                                    │
        ┌───────────────┬───────────┴───────────┬────────────────┐
        │               │                       │                │
   Krishna          Shaik Arif              Uday Kiran      Rajeshwari
   Chankapurai      ┌──────────┐            Sayaboyina     Narella
   PO               │ TL hat   │            SSE backend     SSE view
   requirements     │ review   │                 │               │
   wording          └──────────┘                 │               │
                    ┌──────────┐                 │               │
                    │ SSE2 hat │─────────────────┴───────────────┘
                    │ builds   │      Vishal Singh — SE
                    └──────────┘      Avanish Gupta — ASE
```

Shaik Arif sits in two seats on purpose:

| Seat | Agent | Can edit code? | Job |
|---|---|---|---|
| `tl` | Tech Lead | **no** | design the smallest change, review every diff, run the build |
| `sse2` | Implementation lead | yes | build the hardest part, set the pattern others copy |

Two seats means a review can never quietly become a self-review. When Shaik reviews his own build, he
must open with `SELF-REVIEW`, and Nagendra asks Uday or Rajeshwari for a second opinion on anything
touching money, scoring or approval.

---

## 3. Who owns what

| Person | Agent | Owns | Never does |
|---|---|---|---|
| Nagendra Immadi | `manager` | the plan, the routing, the report, `.brain/` | writes application code |
| Krishna Chankapurai | `po` | user stories, acceptance criteria, exact screen labels | decides tech, tables, endpoints |
| Shaik Arif | `tl` | design, file list, review verdicts, build result | writes feature code |
| Shaik Arif | `sse2` | the hard part, shared page shell, new patterns | trivial edits |
| Uday Kiran Sayaboyina | `sse` | controller, service, repository, query, tests | the JSP when Rajeshwari has it |
| Rajeshwari Narella | `sse-b` | JSP, Bootstrap, JS, validation, print view | backend, queries, endpoints |
| Vishal Singh | `se` | one specified change across 1–3 files | new features, design decisions |
| Avanish Gupta | `ase` | a label, a class, a typo, one field, a test row | anything needing judgement |

Cost note: Nagendra and Shaik (`tl`) run on the OpenCode Go allowance; Uday, Rajeshwari, Vishal and
Shaik (`sse2`) run on your MiniMax plan; Krishna and Avanish run on free models. Three separate
quotas, so one running dry never stops the team.

---

## 4. The workflow, with a real example

You say:

> "Get the quarterly EoI dashboard built. Read the requirement first."

**Nagendra (Manager)** — reads `.brain/`, restates the goal in one sentence, and writes the task list:

```
1. Write the story + acceptance criteria ......... Krishna (po)
2. Design: files, data, flow ..................... Shaik (tl)
3. Build the page shell + one real card .......... Shaik (sse2)
4. Backend: query + controller ................... Uday (sse)
5. View: JSP + filters + empty state ............. Rajeshwari (sse-b)
6. Review + run the build ........................ Shaik (tl)
7. Record state + decisions ...................... Nagendra (manager)
```

**Krishna (PO)** — opens the RBI file and the BRD, writes `REQ-xxx` in
`.brain/03_SYSTEM/Requirements.md`: the why, the who, the story, five checkable criteria, the exact
label table, the empty state wording, and any open question for the bank. He flags anything the
source does not state as `ASSUMPTION — needs bank confirmation`.

**Shaik (TL)** — returns a 12-line design: exact files, request → service → repository → view, new
columns or none, the risk (H2 vs Oracle), and the one test that proves it works. He refuses any design
that needs a new dependency or a fourth layer.

**Shaik (SSE2)** — builds the first screen end to end so the pattern exists: page shell, breadcrumb,
one card, one empty state. Reports the pattern in three lines so everyone else copies it.

**Uday (SSE)** — takes the backend half: the query that runs on both H2 and Oracle, the service rule,
the controller method, one test named as a sentence.

**Rajeshwari (SSE-2)** — takes the view half in parallel: the JSP, the filter bar, the status badges,
the empty state, the print stylesheet. She never edits Uday's files and vice versa.

**Shaik (TL) again** — reviews both diffs against the guardrails, orders findings BLOCKER / MAJOR /
MINOR / NIT, and runs `mvn -q test`. He does not fix anything; he reports.

**Nagendra** — sends blockers back to the owner of the file, then reports to you:

```
Done — Rajeshwari built the view, Uday built the query, Shaik (TL) reviewed, build green.
Files: DashboardController.java, DashboardService.java, EoiRepository.java, dashboard.jsp
Checked: mvn -q test (14 passed), empty state verified with zero rows
Next: maker–checker screen, needs your call on who approves at Zonal Office
Risk: none in H2; Oracle index needed before UAT
```

That's one full cycle. A small change is the same loop with two people instead of six.

---

## 5. Routing — where your request goes

| You say | Nagendra sends it to |
|---|---|
| "What does Para 128 actually require?" | Krishna (`po`) |
| "Write the acceptance criteria for X" | Krishna (`po`) |
| "Design this before coding" | Shaik (`tl`) |
| "Review what changed" | Shaik (`tl`) |
| "Build this screen" | Shaik (`sse2`) for the pattern, then Uday + Rajeshwari |
| "Fix the badge spacing" | Avanish (`ase`) |
| "Change the label to 'Sanctioned Limit'" | Avanish (`ase`) |
| "Add the exposure column to the report" | Vishal (`se`) |
| "This whole module needs a date filter" | Uday (`sse`) + Rajeshwari (`sse-b`) |
| "Where are we?" | Nagendra himself |

When in doubt, say what outcome you want, not who should do it. Nagendra picks the person.

---

## 6. The board

Local Vikunja at **http://127.0.0.1:3456** → project **Digital EoI**. AGPL, single binary, SQLite,
no Docker, no licence. The agents reach it through its built-in MCP server, so they create and move
cards natively.

Columns and what each one means:

| Column | Means | Who moves it here |
|---|---|---|
| **Backlog** | decided, not started, or blocked on a decision | Nagendra |
| **Design** | Shaik is producing the file plan | Nagendra, on handoff |
| **In Progress** | code is being written on a branch | the builder |
| **Review** | PR open, CI running, TL reading the diff | the builder |
| **Done** | merged, verified, `.brain` updated | Shaik (TL) only |

Rules:

- Anything bigger than a one-line fix gets a card. Typo-level work does not — it rides with the PR
  that contains it.
- The card is the *state*; `.brain` is the *record*. Do not repeat the same description in both.
- Every card in Review carries its branch name and PR link in the description.
- A card may only be moved to Done by the Tech Lead, after CI is green and the review has no BLOCKER.
- Blocked cards stay in Backlog with the blocking question in the title — that is why the six
  business questions are the first cards on the board.

Tools the agents actually use (project `2`, kanban view `12`):

| Need | Tool |
|---|---|
| create a card | `vikunja_tasks_create` |
| read the board flat | `vikunja_tasks_list` |
| read columns with their cards | `do_action` → `project_view_buckets_tasks_list` |
| list column ids | `do_action` → `buckets_list` |
| move a card | `do_action` → `task_bucket_update` |
| comment or edit a card | `vikunja_tasks_update` |

Moving a card into the view's **Done** bucket also marks the task done — one action, not two.

Setup, if the machine restarts:

```sh
cd ~/vikunja && nohup ./vikunja web --config ~/vikunja/config.yml > vikunja.log 2>&1 &
open http://127.0.0.1:3456
```

Login is in `~/vikunja/credentials.txt` (mode 600). The agent API token is in
`~/vikunja/api-token.txt` and is referenced only from the global OpenCode config — never from this
repository.

## 7. Git and pull requests

The repository is a normal git repo on `main`. Agents work exactly like a disciplined team:

```
1. git switch main && git pull
2. git switch -c feature/<slug>
3. edit, test, commit            feat(dashboard): add quarterly due card
4. git push -u origin feature/<slug>
5. gh pr create --fill            ← PR opens, CI starts
6. card moves to Review
7. TL reviews the diff, CI must be green
8. gh pr merge --squash           ← TL or you
9. card moves to Done, .brain updated
```

Enforced, not just agreed:

| Rule | Enforced by |
|---|---|
| Nobody pushes to `main` or `master` | policy in `.opencode/opencode.json` — fails with "Blocked by configuration policy" |
| Nobody force-pushes | same policy |
| Only TL merges | builders have `gh pr merge` explicitly denied |
| No review of your own work without saying so | TL prompt: verdict must open with `SELF-REVIEW` |
| No inline script/style in a JSP | CI job `guardrails`, same script you can run locally |
| No merge with failing tests | CI job `build and test`, once a `pom.xml` exists |

Full detail: `CONTRIBUTING.md`. The PR template asks for the person's name, the requirement id, the
guardrail checklist and the real test output.

What is **not** set up yet: there is no remote. Until a GitHub or Gitea remote exists, steps 4–8
cannot run — everything before that (branching, committing, the board, the review discipline)
already works locally.

## 8. What "done" means

A task is done when all four are true:

1. `tl` reviewed the diff and no BLOCKER remains.
2. The build or test result is known — never "should work".
3. The screen has its empty, error and loading states, and its labels match Krishna's wording table.
4. `.brain/` reflects the change (state, next steps, session log, decisions if any).

Not done: "the agent said it finished". Never accept that from anyone, including Nagendra.

---

## 9. When something goes wrong

| Symptom | What Nagendra does |
|---|---|
| MiniMax quota runs out mid-task | swaps the writer to `opencode-go/muse-spark-1.3-contributor` and continues |
| Go allowance runs out | switches the session to a free model with `/models` |
| Review returns a BLOCKER | sends it back to the file's owner with the exact finding |
| A requirement is missing a label | asks Krishna, who cites the RBI text or marks an assumption |
| A fix needs a new library | stops and asks you — that is your decision, not the team's |
| Two agents want the same file | serialises them: one owner per file, always |

---

## 10. Your side of the deal

The team handles code, tests, wording and review. These stay with you:

- **Business decisions** — thresholds, who approves, which borrowers are in scope.
- **Bank approval** — anything that touches real Finacle data or goes to UAT.
- **Dependencies** — adding a library is always your call.
- **Accepting work** — you read the "Done" report, not the code.

Everything else, ask Nagendra.
