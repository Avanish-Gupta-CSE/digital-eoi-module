# Guardrail 2 — Customer-friendly UI/UX

Binding for every agent. The user of this application is a **bank officer at a branch or regional
office**, not a developer: mid-career, busy, on a desktop or a tablet, working to a regulatory
deadline. Every screen must be obvious on first sight and safe on a bad day.

Built with Bootstrap 5 and JSP/JSTL only. No UI framework, no icon library from the internet, no
animation library.

## The five questions every screen must answer

1. Where am I? — page title + breadcrumb, same place on every page.
2. What do I do here? — one primary action, visually dominant.
3. What is the current state? — counts, status chips, last-updated date.
4. Did my action work? — explicit success or error message, next to what changed.
5. What if there is nothing? — a written empty state, not a blank table.

If a screen cannot answer these, it is not done.

## Page shell

- Fixed header: UCO Bank wordmark, module name, signed-in user, role, Sign out.
- Left navigation: max 7 items, plain words ("EoI Entry", not "EoI Mst Data Mgmt").
- Breadcrumb under the header, always: `Home / EoI / Quarterly Return`.
- Content width capped (Bootstrap container), never edge-to-edge text on a wide monitor.
- Footer: application version + environment (`DEV`, `UAT`, `PROD`) — helps support calls.
- Active menu item visibly highlighted; the current page is never a dead end.

## Forms

- **One column.** Labels above the input, bold, sentence case. Placeholder is never the label.
- Required fields: mark with `*` **and** the legend `* Required`.
- Group with fieldsets and headings when a form passes 10 fields ("Borrower details",
  "Exposure details"). Do not build a 40-field wall.
- Read-only values look read-only (grey background, no border) and are labelled, never hidden silently.
- Validate on submit, and on blur for numbers. Error text goes **under the field**, in plain words,
  stating the fix: "Sanctioned limit must be a number, for example 25000000."
- Keep the user's input when validation fails. Never clear the form.
- One primary button (`Save`, `Generate EoI`), one secondary (`Cancel`), destructive actions in red
  and confirmed.
- Saving shows: "EoI for Q3 FY2026-27 saved. Reference: EOI-2026-0042." Include the reference.
- Never use a modal for the main task. Modals only for confirmations and short pickers.

## Tables and lists

- Sort by the column people care about (date, borrower). Show the sort direction.
- Right-align money, left-align text, centre status. Numbers use tabular figures.
- Money: `₹ 12,45,00,000` (lakh/crore grouping). Dates: `21-11-2026`. Both from one formatter helper.
- Status as a Bootstrap badge with text, never colour alone: `Due`, `Submitted`, `Overdue`.
- Paginate at 25 rows. Show "Showing 1–25 of 143".
- Every list has: a search box, an empty state, and an export (Excel for data, PDF for the
  prescribed format).
- Empty state names the reason and the next step: "No borrowers are due this quarter. The next
  quarterly window opens on 01-01-2027."
- Long text truncates with a title tooltip; never break the row height.
- Sticky header row when the list scrolls.

## Feedback and safety

- Loading: a spinner plus the word "Loading…" — never a frozen screen.
- Success: green alert at the top of the content, auto-fades after 6 seconds.
- Error: red alert, plain language, what to do next, and the reference number for support.
- Confirmation before: submit for checking, approve, reject, delete, generate a final report.
- Show the maker's name and timestamp on anything submitted; show the checker's on anything approved.
- Rejection requires a reason, minimum 10 characters, and the reason is visible to the maker.
- Never lose work: warn on navigating away from a dirty form.
- No auto-submit, no auto-save, no silent recalculation of bank figures.

## Language

- Short sentences. Active voice. Say "Save", not "Persist".
- Bank vocabulary stays as the bank uses it: borrower, facility, sanctioned limit, outstanding,
  account conduct, consortium, multiple banking arrangement, EoI.
- No abbreviations a branch officer would not know. No developer words on screen: "record",
  "entity", "null", "payload", "endpoint", "sync", "token".
- Errors say what happened and what to do — never a stack trace, never an error code alone.
- Numbers and units together: `₹ 5 crore`, `90 days`, `Q3 FY2026-27`.

## Accessibility and device basics

- Contrast at least 4.5:1 for text. UCO navy `#003366` on white is the default.
- Every input has a `<label for>`. No placeholder-only inputs.
- Full keyboard use: visible focus ring, logical tab order, Enter submits, Esc closes.
- Click targets at least 44×44 px.
- Base font 16px, never below 14px. Line height at least 1.5.
- Works at 1024×768 and on a 10-inch tablet without horizontal scrolling.
- No motion beyond 200 ms fades. No carousels, no parallax, no entrance animations.
- Print stylesheet for the prescribed EoI format: white background, no navigation, no buttons,
  page numbers, bank name and generation date in the footer.

## Offline assets

- Vendor Bootstrap 5 CSS/JS and any icon set under `src/main/resources/static/vendor/`.
- System font stack only: `system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif`.
- All images local, with `alt` text. No stock photos of people; a bank screen is not a landing page.
- No external requests at runtime. No map tiles, no web fonts, no CDN fallback, no analytics.

## UI definition of done

- [ ] The five questions answered on the screen.
- [ ] Empty, loading, error, and success states exist and are worded.
- [ ] Labels come from the requirement's wording table, not invented.
- [ ] Keyboard-only run-through completed.
- [ ] Tested at 1024×768 and on a tablet width.
- [ ] Printed or exported view looks correct.
- [ ] Zero inline JS/CSS, zero external requests.
