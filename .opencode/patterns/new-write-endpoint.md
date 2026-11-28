# New write endpoint (POST under /reports/{id}/...)

Copy: `ReportChildController.addCurrency` / `deleteCurrency` and `ReportExposureRepository`.

1. Controller method: `@PostMapping("/reports/{id}/things/add")`, read params, call the repository,
   `return "redirect:/reports/" + id + "/edit?saved=1"`. No rules in the controller.
2. Repository: bound `?` SQL, new id from `eoi_seq.NEXTVAL`. A delete is
   `DELETE FROM t WHERE id = ? AND report_id = ?` (rule 5).
3. Add the URL pattern to `ReportLockConfig` (`"/reports/*/things/**"`) (rule 6).
4. Bad input must end in a flash message, not a 500: the advice class `ReportChildExceptionHandler`
   already covers the child controllers; add your controller to its `assignableTypes`.
5. Write the four negative tests (see `new-endpoint-test.md`).
6. Run `sh scripts/check-guardrails.sh` and `mvn -o -q test`. Then open the page and use it.
