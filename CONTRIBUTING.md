# Contributing — branch, review and merge rules

These rules apply to humans and agents equally. The Manager enforces them; the Tech Lead is the
merge authority.

## Branch model

`main` is always releasable. Nobody commits to it directly — not you, not an agent.

| Branch | For | Merged by |
|---|---|---|
| `main` | released, verified state | — (protected) |
| `feature/<slug>` | a new screen, report or business rule | Tech Lead, after review |
| `fix/<slug>` | a defect, a wrong label, a wrong number | Tech Lead, after review |
| `chore/<slug>` | build, config, docs, guardrails | Tech Lead, after review |

Slug is lowercase and hyphenated: `feature/quarterly-dashboard`, `fix/due-badge-spacing`.

## Before you start

```sh
git switch main && git pull
git switch -c feature/<slug>
```

One branch per task. If the task grows a second concern, stop and let the Manager re-cut it.

## Commits

`<type>(<scope>): <subject>` — imperative, lowercase, under 72 characters.

```
feat(dashboard): add quarterly due status card
fix(eoi): correct sanctioned limit formatting in lakh
chore(guardrails): tighten inline script rule
test(scoring): cover npa overrides good conduct
```

Types: `feat`, `fix`, `test`, `docs`, `chore`, `refactor`. No "wip", no "changes", no ticket-only
subjects. One idea per commit; if you cannot describe it in one line, it is two commits.

## Pull requests

Open the PR as soon as the first commit is pushed — not when everything is finished. Use the
template in `.github/PULL_REQUEST_TEMPLATE.md`.

A PR is mergeable only when **all four** are true:

1. CI is green (`mvn -q test` plus the guardrail check).
2. The Tech Lead reviewed it and no BLOCKER remains.
3. The screen has its empty, loading and error states, with labels from the requirement.
4. No new dependency, no CDN link, no inline JS or CSS — or the change has written owner approval.

## Merging

Squash merge only. The PR title becomes the commit on `main`, so it must read like a commit message.

Who may merge:

| Role | Merge |
|---|---|
| Owner | yes |
| Tech Lead (`tl`) | yes, after CI green and review complete |
| Manager (`manager`) | no |
| Everyone else | no |

Never force-push to `main`. Never merge your own PR without a review recorded on it. These are
blocked by policy, not just convention.

## What agents may do

| Action | Manager | Tech Lead | Builders | You |
|---|---|---|---|---|
| Read diffs, `git status` | yes | yes | yes | yes |
| Create a branch, commit, push it | no | yes | yes | yes |
| Open a PR | no | yes | yes | yes |
| Review a PR | no | yes | no | yes |
| Merge to `main` | no | yes | no | yes |
| Push to `main` directly | **no** | **no** | **no** | **no** |

Builders are `sse2`, `sse`, `sse-b`, `se`, `ase`. "Push to main" is denied by policy in
`.opencode/opencode.json`, so it fails even if an agent tries.

## If something goes wrong

- CI red on `main`: revert the merge commit immediately, then fix forward on a branch.
- Wrong code merged: open `fix/<slug>`, reference the original PR, do not rewrite history.
- A guardrail was bypassed: fix the rule in `.opencode/guardrails/` in the same PR, not later.
