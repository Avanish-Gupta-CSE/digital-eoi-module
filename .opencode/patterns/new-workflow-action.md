# New maker-checker action (for example "reopen")

Copy: `WorkflowController.approve` and `WorkflowService.approve`.

1. Controller: check the role first (`notChecker` style helper), call the service, catch
   `IllegalStateException` and flash its message.
2. Service, one `@Transactional` method: (a) a business pre-check such as "not the preparer",
   (b) the status-guarded UPDATE `... WHERE id = ? AND status = 'SUBMITTED'` (rule 4),
   (c) `writeAudit(...)` (rule 7).
3. The refusal message says what to do next, in plain words.
4. The workflow URL is the one kind of write URL that does **not** go into `ReportLockConfig`.
5. If the bank has not said whether the officer who prepared the report may also do this action
   (approve forbids it), **ask the owner** and write the answer in `.brain/02_DECISIONS/`; do not guess.
6. Tests: happy path, wrong role, wrong status (draft, already approved), same officer.
