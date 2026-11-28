# New maker-checker action (for example "reopen")

Copy: `WorkflowController.approve` and `WorkflowService.approve`.

1. Controller: check the role first (`notChecker` style helper), call the service, catch
   `IllegalStateException` and flash its message.
2. Service, one `@Transactional` method: (a) a business pre-check such as "not the preparer",
   (b) the status-guarded UPDATE `... WHERE id = ? AND status = 'SUBMITTED'` (rule 4),
   (c) `writeAudit(...)` (rule 7).
3. The refusal message says what to do next, in plain words.
4. The workflow URL is the one kind of write URL that does **not** go into `ReportLockConfig`.
5. Tests: happy path, wrong role, wrong status (draft, already approved), same officer.
