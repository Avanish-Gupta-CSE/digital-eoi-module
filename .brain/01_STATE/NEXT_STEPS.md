# NEXT_STEPS

Last updated: 2026-11-21

## Blocking questions — must be answered before any code is written

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

## Order of work once unblocked

1. Data model for Annex II (Parts I–VI) → verify: every Annex II cell maps to a column/field.
2. Part VI grading rules engine → verify: each rubric row (pp. 50–52) has a unit test.
3. Maker–Checker state machine → verify: maker cannot approve own record.
4. Quarterly scheduler + audit trail → verify: generation logged with user, timestamp, version.
5. Report renderer matching the prescribed layout → verify: side-by-side vs BRD.pdf pages 3–9.

## Notes
- All work now runs through the agent team in `.opencode/agents/` — talk to `manager`, not to
  `build`. Do not paste the guardrails into a prompt; they load automatically.
- Part V (currency positions) and Part IV (derivatives MTM) are the least likely data to exist
  cleanly in Finacle. Confirm with treasury before promising auto-population.
- Page 6 of the BRD scan OCR'd poorly; re-read the original if Part V precision matters.
