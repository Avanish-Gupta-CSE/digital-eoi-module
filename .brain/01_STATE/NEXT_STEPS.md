# NEXT_STEPS

Last updated: 2026-11-22

## Status

The build has started. A skeleton now runs (`/dashboard`, H2, 4 tests, CI green). The six questions
below are **no longer a full block** — each one is contained behind a property or a not-yet-built
layer, so work continues without guessing.

| Question | Contained by | Unlocks when answered |
|---|---|---|
| #1 Finacle mechanism | `BorrowerRepository` is the only data source | the real adapter |
| #2 v1 scope | only the dashboard exists today | which Annex II parts to build |
| #3 Submission channel | nothing exports yet | the export/transmit design |
| #4 Quarterly calendar | `eoi.current-quarter` property | the scheduler |
| #5 ₹5 crore threshold | `eoi.borrower-threshold-amount` | confirming the population rule |
| #6 Non-Finacle data | no manual-entry screens yet | Parts I/II entry screens |

## Build sequence — what is unlocked now

Each phase is one board card. Verify column states how to prove it.

| # | Phase | Depends on | Verify |
|---|---|---|---|
| 0 | Skeleton, dashboard, money formatting, CI | nothing | **done** — `/dashboard` 200, 4 tests, guardrails green |
| 1 | Annex II data model, Parts I–VI | #2 (shape only) | every Annex II cell maps to a column; schema runs on H2 |
| 2 | Maker entry form for Parts I/II | phase 1 | bad input errors under the field; good input persists |
| 3 | Maker–checker state machine + audit table | phases 1–2 | a maker cannot approve their own record |
| 4 | Part VI scoring engine | phase 1 | one unit test per rubric row (BRD pp. 50–52) |
| 5 | Report renderer (print/PDF) | phases 1–4 | side-by-side against BRD pp. 3–9 |
| 6 | Finacle adapter | #1 | a mock and a live adapter behind one interface |
| 7 | Quarterly scheduler + alerts | #4 | generation logged with user, timestamp, version |

Phase 1 needs no business answer — Annex II's structure is fixed by the RBI direction. Start there.

## The six questions, verbatim from the BRD

1. **Finacle integration mechanism.** API / web service, direct DB extract, or file drop?
   This single answer determines the whole architecture (sync adapter vs batch ETL).
2. **Scope v1.** Is it Annex II only (as the letter implies), or also Annex I capture and
   Annex III Diligence Report tracking as para 128 requires?
3. **Submission channel.** How does the generated EoI leave the bank — email with attachment,
   a lender portal, or an API to other banks? Nothing in the BRD defines a recipient system.
4. **Quarterly calendar.** Fixed as-on dates (30 Jun / 30 Sep / 31 Dec / 31 Mar) or
   rolling "at least quarterly" per borrower?
5. **Borrower population.** Is the ₹5 crore sanctioned-limit threshold (para 128(1)) the
   selection rule for which borrowers appear in the module?
6. **Data not in Finacle.** Shareholding changes, net worth of directors, integrity of
   management, litigation — manual entry screens + maker-checker, or external sources?

## Notes
- All work runs through the agent team in `.opencode/agents/` — talk to `manager`.
- Part V (currency positions) and Part IV (derivatives MTM) are the least likely data to exist
  cleanly in Finacle. Confirm with treasury before promising auto-population.
- Page 6 of the BRD scan OCR'd poorly; re-read the original if Part V precision matters.
