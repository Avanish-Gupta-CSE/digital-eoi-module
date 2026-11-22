## What changed

<!-- One or two sentences. What does the bank officer get that they did not have before? -->

## Who built it

<!-- Person and role, e.g. "Uday Kiran Sayaboyina (SSE), reviewed by Shaik Arif (TL)" -->

## Requirement

<!-- REQ-xxx from .brain/03_SYSTEM/Requirements.md, or "no requirement: chore/fix" -->

## Guardrail checklist

- [ ] No inline `<script>` or `style="..."` in any JSP
- [ ] No CDN link, web font, remote image, or analytics
- [ ] No new Maven/npm dependency (or owner approval linked below)
- [ ] Files ≤ 200 lines, methods ≤ 25 lines, nesting ≤ 3
- [ ] Empty, loading and error state present for every list and form
- [ ] Money formatted `₹ 12,45,00,000`, dates `dd-mm-yyyy`, status as text not colour
- [ ] SQL runs on **both** H2 and Oracle 19c
- [ ] One test added or updated; it failed before the change

## Verification

<!-- Exact commands and their result. "Should work" is not verification. -->

```
$ mvn -q test
...
```

## Data

- [ ] This PR contains no real borrower data, facility numbers, or Finacle extracts
- [ ] Any sample data added lives in `h2-init.sql`

## Risk

<!-- Oracle vs H2 difference, concurrency, audit trail, or "none". -->
