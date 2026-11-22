# Guardrail 1 — Minimal, beginner-readable code

Binding for every agent in this project. If a task cannot be done inside these limits, say so and
propose the smaller version. Never break a limit silently.

## The one rule

**Write the version a first-year developer can read, change, and explain six months from now, with
no internet and no help.** Clever is a defect. Short and obvious is correct.

## Size limits (hard)

| Thing | Limit | If exceeded |
|---|---|---|
| Java class | 200 lines | split by page, not by layer |
| Method | 25 lines | extract a method named after the rule |
| Nesting depth | 3 (`if`/`for`/`try`) | use guard clauses / extract |
| Parameters | 5 | pass an object, or split the method |
| JSP | 150 lines | use a shared fragment |
| JS file | 100 lines | split per page |
| Public methods per class | 12 | the class is doing two jobs |
| New files for one feature | 4 | re-cut the task |
| Inheritance depth | 2 | prefer composition |

## Banned in this codebase

| Banned | Do this instead |
|---|---|
| `<script>` inside a JSP | `assets/js/<page>.js`, bind by element ID |
| `style="..."` inside a JSP | Bootstrap 5 class, or `assets/css/style.css` |
| CDN `<link>`/`<script src="https://...">` | vendor file under `src/main/resources/static/vendor/` |
| Google Fonts, remote images, analytics, trackers | system font stack, local images |
| New Maven/npm dependency without written approval | JDK + Spring + Bootstrap already in the app |
| Lombok, MapStruct, ModelMapper, custom annotations | plain getters/setters, explicit mapping |
| Reflection, `@Scheduled` magic, AOP tricks | one explicit method call |
| Stream chains longer than one line | a `for` loop a beginner can debug |
| `static` mutable state, singletons of your own | Spring bean with constructor injection |
| String-concatenated SQL | bound parameters (`:name` / `?`) |
| Dialect-specific SQL (Oracle-only or H2-only) | portable SQL; test on both profiles |
| Copy-paste layers with no job | delete the layer |
| Interfaces with one implementation | plain class — unless it is a real seam (H2/Oracle, Finacle mock/live, export format) |
| Base classes named `Abstract*` / `Base*` | composition, or nothing |
| `Optional` used as a field type or parameter | `null` check at the boundary |
| Checked-exception wrapping chains | one domain exception, one message |

## Java / Spring shape

- One page = one controller method + one service method + one JSP. No extra manager/helper/facade
  layer unless a second caller exists today.
- Controller: read input, call service, add model attributes, return view name. No business rules.
- Service: the business rule, one rule per method, `@Transactional` where it writes.
- Repository: native SQL in one place; a comment naming the RBI paragraph it serves.
- Constructor injection only. No field injection, no `@Autowired` on fields.
- Config values in `application-<profile>.properties`, never hardcoded in Java.
- No mock data outside `h2-init.sql`. Business code never returns fake numbers.
- Money: `BigDecimal`, `RoundingMode.HALF_UP`, scale 2. Never `double`.
- Dates: `LocalDate` / `LocalDateTime`. Format only at the view boundary.

## Readability rules

- Names: `calculateQuarterlyDue`, `isSubmittedForQuarter`, `findBorrowersByConsortium`. No `data`,
  `info`, `temp`, `obj`, `mgr`, `util`, single letters (except loop `i`).
- A method reads like a sentence top to bottom.
- Comments explain **why**, never **what**. A comment that restates the code is deleted.
- No comment blocks longer than 3 lines.
- No "TODO" without a name and a date: `TODO(avanish 2026-12-01): remove after UAT`.
- Delete only what your own change made unused. Leave other people's dead code alone and mention it.

## Dependencies and the offline requirement

The application must build, start, and run with **no internet**:

- Runtime: every CSS/JS/font/image is a local file. Zero outbound requests. If the browser
  devtools Network tab shows a request to a domain we do not own, it is a defect.
- Build: keep the Maven dependency tree small enough that `mvn -o -q clean package` works against a
  pre-populated local repository. Adding a dependency is therefore a **human decision**, not an
  agent decision: state the reason, the size, and the alternative you rejected.
- No npm, no Node, no bundler, no transpiler in the build path. Plain assets only.

## Testing rule

- One test per business rule changed. Named as a sentence:
  `quarterlyDue_isOverdue_whenSubmissionDateIsNull`.
- Plain JUnit 5 + AssertJ-free assertions if possible. No mocking framework for a class you own;
  construct it.
- A test must fail before the fix and pass after. State both results.

## Definition of done (code)

- [ ] Limits in this file respected.
- [ ] No banned construct introduced.
- [ ] Builds and runs on the `h2` profile; SQL also valid for Oracle 19c.
- [ ] One test added or updated, result reported.
- [ ] Diff contains only lines that trace to the task.
- [ ] `Change_Map.md` updated: "to change X, edit Y".

## Change map (how a beginner changes this later)

Keep `.brain/03_SYSTEM/Change_Map.md` current. One line per screen:

```md
| Screen | Controller | Service | View | JS |
|---|---|---|---|---|
| Dashboard | DashboardController#index | DashboardService#summary | dashboard.jsp | dashboard.js |
```

If someone cannot find where to change a label using this table in 30 seconds, the task is not done.
