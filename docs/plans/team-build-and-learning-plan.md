# Maintenance Mate — Team Build and Shared-Learning Plan

**Planning date / first work day:** Wednesday, 28 October 2026
**Target submission:** Wednesday, 4 November 2026
**Status:** Project proposal accepted; implementation plan only.
**Goal:** Deliver a small, working hostel-maintenance tracker and ensure all five members can explain and demonstrate the same required OOP concepts—not just the code they personally led.

## Verified starting point

- The repository is `/Users/tehillachiwama/projects/MaintenanceMate`, on `main`.
- The tracked README currently contains only the project title. No Java source, build file, test suite, or application scaffold is present yet.
- `docs/` contains the accepted proposal and its editable source. The accepted proposal lists Tehilla Chiwama (2410014), Bright Mwansa (2410010), Mwenya Simpungwe (2410013), Cheela Mulilo (2410032), and Blessings Syansowa (2421035).
- The assignment brief requires real classes, encapsulation, at least one used inheritance or interface relationship, polymorphism, a custom exception that is thrown and handled, collections, at least five Javalin 5+ REST endpoints including GET and POST plus PUT or DELETE, data that survives restart, a simple web front end, a short demo video, and individual member understanding.

## Scope and decisions

**First-version workflow:** A resident submits a repair request with hostel/block, room number, and description; the app creates a reference number and saves it. Maintenance staff can view requests and update status. A resident can look up the latest status by reference number.

**Keep version one deliberately small:** one local demo app, one request workflow, three statuses (`Received`, `In Progress`, `Resolved`), a basic page served by Javalin, and one persistence choice (a file or SQLite). Do not add chat, notifications, image uploads, emergency dispatch, external integrations, or production-grade authentication/permissions. Use only demo data and explain that authentication is outside this prototype's scope.

**Resolve these decisions together at the Wednesday 28 October kickoff, before implementation:**
1. Check everyone can run the selected Java version and agree one build tool/test setup; record exact versions and commands in the README.
2. Choose either the simplest persistent store the group can all test (the assignment allows a file or SQLite); do not build both.
3. Agree the request fields, status names/transitions, reference-number format, and JSON request/response shapes.
4. Agree ownership of the GitHub repository and ensure all five members can push from their own accounts.
5. Agree who will capture/edit/upload the demo video and who confirms the submission links.

If the group cannot settle a technical choice quickly, pick the least complicated option that can be demonstrated saving and reloading a request; timebox the discussion to 30 minutes.

## Main classes and concept checkpoints

Keep the accepted proposal's six main classes as the teaching model:

| Class | Shared responsibility/checkpoint |
|---|---|
| `User` | Abstract base with shared private identity/contact fields and common behavior. |
| `Resident extends User` | Stores hostel/block and room; submits requests and checks by reference. |
| `MaintenanceStaff extends User` | Represents staff handling requests and updating progress. |
| `MaintenanceRequest` | Stores reference, location, description, resident, optional assigned staff, status, and timestamps. |
| `RequestService` | Validates input and coordinates creation, lookup, assignment, and permitted status transitions. |
| `RequestRepository` | Stores and retrieves requests using the one selected persistent store. |

Use private fields and controlled methods for encapsulation. Use a `Map` keyed by request reference (or another agreed `List`/`Map`) in the relevant service/repository path, and explain why it is used. Demonstrate polymorphism in one meaningful shared `User` operation, such as `canAccess(request)`: a `Resident` may access only requests they submitted, while `MaintenanceStaff` may access requests assigned to them. Add a test that calls the same method through a `User` reference and checks the two different outcomes. Keep that demonstration local to the prototype; it is not a substitute for real authentication.

Add at least one focused custom exception (for example, `InvalidRequestException` or `RequestNotFoundException`): the service throws it for a defined invalid condition and the Javalin handler catches it and returns a suitable HTTP error response. Each member must be able to trace the exception from cause, through throw/catch, to response.

## Work split with cross-learning

Each lead owns a small vertical slice and makes meaningful code commits from their own GitHub account. Their named partner reviews the slice and learns it by pairing/walking through the tests; the lead in turn pairs on the partner's area. All five attend the shared design, integration, and learning sessions. Avoid isolated “you do your part and never touch the rest” work.

| Lead | First implementation slice | Pair / reviewer |
|---|---|---|
| **Tehilla Chiwama** | Resident request submission flow: resident location/details validation and `POST /api/requests`, including a focused test. | Cheela Mulilo |
| **Bright Mwansa** | Maintenance workflow: request list and status transition behavior, including `PUT /api/requests/{reference}/status` and tests. | Blessings Syansowa |
| **Mwenya Simpungwe** | `RequestService` behavior: reference creation/lookup, validation rules, custom exception path, and focused service tests. | Tehilla Chiwama |
| **Cheela Mulilo** | `RequestRepository`: save/load/find behavior in the chosen store, including a restart/reload test. | Bright Mwansa |
| **Blessings Syansowa** | Simple HTML/JavaScript page served by Javalin: submit request, list/open requests for the demo, and look up status by reference using the API. | Mwenya Simpungwe |

**Shared integration work:** The API and class contract is agreed by all five before parallel coding. The named partner reviews the lead's change and asks the lead to explain it. During integration, rotate who drives the keyboard so each person makes changes outside their main slice. Review work should be recorded in GitHub; do not count a docs-only contribution as a member's only code contribution.

## Day-by-day team-lead playbook

**Start boundary:** No project work—including setup, repository changes, scaffold creation, or implementation—starts before Wednesday 28 October. All kickoff decisions and first setup happen on that date.

**Session assumption:** Plan for one 75-minute shared session each day with all five members present, plus a same-day focused work block of about 60–90 minutes on build days. Confirm availability at kickoff. Reserve 20–25 minutes of the shared session for the artifact-based learning checkpoint; if attendance or time is reduced, preserve the checkpoint and the acceptance-critical slice, and cut optional polish. Do not silently move work to a pre-start date or promise an unstaffed deliverable. Named lead/partner links below use the agreed split; they are review-and-learning links, not a requirement that every pair work simultaneously. Rotate the keyboard and explain decisions to the whole team.

**TDD rule for every code slice (Java, tests, routes, HTML, or JavaScript):** write one behavior test first; run it and observe the expected failure; implement only enough to pass; rerun the focused test; run relevant regression tests; then commit the meaningful slice from the member's own account. Do not bundle unrelated behavior or accept a test that never failed for the intended reason. Keep this loop visible in the commit/PR discussion and daily evidence.

**Prototype boundary:** Use demo data only. `User`, `canAccess`, and the role examples demonstrate OOP, not verified identity or secure authorization. There is no real login, session security, or production permission boundary; do not present this classroom prototype as protecting resident data.

### Wednesday 28 October — kickoff, lock decisions, prove the scaffold

- **Objective:** Agree the smallest accepted request-tracking MVP and settle every scope/toolchain/store/API/repository/demo decision before coding; prove the selected Java/Javalin project can run.
- **Shared session (75 min):** 0–8: Tehilla facilitates the assignment checklist, MVP boundary, and explicit exclusions. 8–20: all five verify the Java/build/test commands and GitHub access; Cheela checks the selected setup with the group. 20–30: choose exactly one file-or-SQLite store. 30–43: settle request fields, three statuses and transitions, reference shape, JSON/error shapes, class relationships, and five route contracts. 43–50: agree repository ownership, the verified run command to document, and named demo/submission-link owners. 50–70: all five use the class sketch to explain and correct the six-class relationship, private state, and `Resident`/`MaintenanceStaff` inheritance. 70–75: record decisions and the go/no-go checklist; no scope/tool/store/API decision is left for another day.
- **Lead/partner assignments:** Tehilla leads scope and Resident workflow (Cheela checks required fields); Bright leads status and API contract (Blessings checks the browser path); Mwenya records class/service/exception contract (Tehilla reviews); Cheela leads store/toolchain feasibility check (Bright verifies the run); Blessings leads the served-page/demo needs (Mwenya checks the route contract). All five approve the final choices together.
- **Evidence/deliverable:** A decision record in the README or plan notes with exact Java/build/test/run commands, chosen single store, class/API/data contract, GitHub access for all five, named demo/link owners, and a successful minimal Javalin start/build. Sketch the six-class relationship and route list.
- **Shared-learning checkpoint:** Each member uses the class sketch to explain one class boundary, private state/encapsulation, and why `Resident` and `MaintenanceStaff` extend `User`; all five correct the sketch together.
- **Go/no-go:** Go only when everyone can access the repo, run the chosen scaffold, and restate the frozen contract. If a choice is unresolved, use the simplest group-runnable option (one file store; one basic served page; only the agreed request workflow) and record it by end of 28 Oct. If the scaffold will not start, fix only the toolchain/scaffold blocker that day; cut styling and optional fields, never defer a second tool/store/API decision to later.

### Thursday 29 October — domain model, service rules, and OOP evidence

- **Objective:** Build the six agreed class responsibilities and the smallest tested create/find/status behavior, including encapsulation, used inheritance, polymorphism, collection use, and the custom exception path.
- **Shared session (75 min):** 0–5: restate the frozen contract. 5–30: lead/partner work through small test-first domain/service slices. 30–40: connect the classes and inspect focused RED→GREEN evidence. 40–65: all five trace a request through the service and demonstrate the same polymorphic call through a `User` reference using the tests. 65–70: check the acceptance gate. 70–75: verify meaningful commit ownership.
- **Lead/partner assignments:** Tehilla leads `Resident` submission fields/validation with Cheela; Bright leads `MaintenanceStaff` and valid status transitions with Blessings; Mwenya leads `RequestService` create/find and the custom exception throw path with Tehilla; Cheela leads `MaintenanceRequest` private state/controlled updates with Bright; Blessings leads the `User`-reference polymorphism test with Mwenya. The whole group reviews how `RequestRepository` fits the six-class model; persistent implementation is tomorrow.
- **Evidence/deliverable:** Six agreed class responsibilities represented in the scaffold; focused tests for validation, status rules, collection lookup, two different `User` dispatch outcomes, and the service exception. Store only in the agreed in-memory collection for this day's core tests; do not add a second persistence choice.
- **Shared-learning checkpoint:** Using the passing tests and class diagram, each member explains one private field's controlled update, the actual subclass relationship, the polymorphic result, and where the custom exception originates.
- **Go/no-go:** Go when focused tests pass and the team can point to the real `extends User` relationship, common method dispatch, and collection. If blocked, simplify validation/status cases to the agreed workflow and preserve one clear example of every required concept; do not replace inheritance/polymorphism with comments or an unused interface.

### Friday 30 October — persistence and first end-to-end backend slice

- **Objective:** Save and reload a request from the single chosen store across a full application restart; connect repository and service behavior and establish the first route-to-service path.
- **Shared session (75 min):** 0–8: review the agreed store and model. 8–30: pair on save/find/load tests, run the restart test RED then GREEN. 30–42: connect creation/lookup through service and the first route, with an error case. 42–62: all five inspect the persisted data and reproduce a stop/restart/reload, explaining layer ownership from the test. 62–70: record evidence and check the gate. 70–75: confirm member commits and next-day handoff.
- **Lead/partner assignments:** Cheela leads `RequestRepository` save/load/find and restart test with Bright; Mwenya leads service-to-repository behavior and not-found/invalid exception coverage with Tehilla; Tehilla checks the request-creation contract against the repository test with Cheela; Bright checks list/status data integrity with Blessings; Blessings prepares the agreed page-to-API acceptance checklist with Mwenya (no new UI framework or store).
- **Evidence/deliverable:** Passing test that writes a request, stops/restarts the app, reloads it, and verifies reference/status; passing focused repository/service tests; one route demonstrably connected to the service; record the exact restart procedure and resulting evidence.
- **Shared-learning checkpoint:** Each member takes a turn tracing `RequestService → RequestRepository → chosen store` from the test and explains which layer owns validation versus persistence.
- **Go/no-go:** Go only after a full restart preserves and finds a request. If persistence fails, repair the one chosen store and keep the feature set to save/find/list; cut optional serialization polish and styling. Do not proceed by substituting in-memory-only success or adding a second store.

### Saturday 31 October — complete and test the Javalin API

- **Objective:** Integrate at least five working Javalin endpoints with GET, POST, and PUT, consistent success/error responses, and tested service/repository wiring.
- **Shared session (75 min):** 0–8: agree endpoint test order against the Oct 28 contract. 8–30: finish route slices test-first, including one invalid/not-found case. 30–42: run the route/API regression and verify status codes and response bodies. 42–62: each member uses a route test to explain one endpoint end to end. 62–70: gate check. 70–75: commit check.
- **Lead/partner assignments:** Tehilla leads `POST /api/requests` with Cheela; Blessings leads `GET /api/requests`, `GET /api/requests/{reference}`, and `GET /api/health` with Mwenya; Bright leads `PUT /api/requests/{reference}/status` with Blessings; Mwenya owns the service exception-to-HTTP error mapping with Tehilla; Cheela verifies API-created requests still pass through repository persistence with Bright.
- **Evidence/deliverable:** Automated route tests for all five routes (or more), including GET/POST/PUT, expected success status/body, invalid/not-found handling, and persistence-backed create/read/update. Keep request/response examples as evidence for README/demo.
- **Shared-learning checkpoint:** Using the route tests, all five explain HTTP method, input body/path, response/status, service call, and one error behavior; rotate who drives the walkthrough.
- **Go/no-go:** Go when five endpoints pass and the restart requirement remains intact. If blocked, retain exactly the five agreed routes and their simplest useful responses; cut extra filters, fields, and styling, not a required method or error handling.

### Sunday 1 November — serve the page and complete the user workflow

- **Objective:** Connect a plain Javalin-served page to the real API for resident submission, staff list/status update, and resident reference lookup.
- **Shared session (75 min):** 0–8: confirm stable route contracts. 8–28: implement one page behavior at a time with test-first client/API checks. 28–40: run the full browser workflow against the persistence-backed service. 40–60: rotate the driver; every member operates and explains one workflow handoff using page/API evidence. 60–68: freeze feature scope once the path passes. 68–75: record the gate and meaningful commits.
- **Lead/partner assignments:** Blessings leads the served HTML/JavaScript page and API wiring with Mwenya; Tehilla leads the resident submit/reference lookup path with Cheela; Bright leads staff list/status action with Blessings; Cheela verifies the save/restart/read path with Bright; Mwenya checks validation/error display with Tehilla. Keep the UI to plain form/list/status output.
- **Evidence/deliverable:** A running page served by Javalin that completes resident submit → staff status update → resident lookup and shows the returned reference/status; record a short reproducible manual run plus the available focused tests.
- **Shared-learning checkpoint:** All five run the same end-to-end scenario and use the page source/network/API evidence to explain how a JavaScript action reaches a route and updates the persisted request.
- **Go/no-go:** Go when the core browser path works against saved data. If blocked, simplify to one page, plain controls, and no CSS polish; keep all three workflow steps. After this gate accept only defects blocking the definition of done.

### Monday 2 November — integration, regression, and feature freeze

- **Objective:** Remove acceptance-blocking defects, prove required concepts and behavior together, and freeze features.
- **Shared session (75 min):** 0–10: inspect the acceptance checklist and current failures. 10–28: reproduce and rank only blocking defects; fix each via the full TDD loop. 28–40: rerun relevant regression and persistence-restart checks. 40–62: random blind teach-back across classes, OOP, routes, and failures. 62–70: mark pass/fail and lock scope. 70–75: confirm the freeze and owners for documentation-only work.
- **Lead/partner assignments:** Tehilla leads POST/request workflow regression with Cheela; Bright leads status/PUT regression with Blessings; Mwenya leads custom-exception and polymorphism evidence with Tehilla; Cheela leads persistence/restart regression with Bright; Blessings leads page-to-API regression with Mwenya. Each partner must reproduce the test and explain the change before approval.
- **Evidence/deliverable:** Green relevant regression suite; completed acceptance checklist with actual evidence for every required OOP/API/persistence/UI item; defect log showing only blockers and outcomes; explicit feature freeze.
- **Shared-learning checkpoint:** Each member receives a randomly selected concept outside their lead slice and demonstrates it from the code/test, not slides. Any gap is taught and rechecked before the go decision.
- **Go/no-go:** Go when the full acceptance checklist passes and all five clear teach-back. If not, spend remaining time only on required concepts/flow and cut enhancements, styling, or optional fields. No new feature after this date.

### Tuesday 3 November — README, clean run, demo, and interview readiness

- **Objective:** Make the accepted build reproducible and submission-ready; capture the required short unlisted demo and verify individual/peer requirements.
- **Shared session (75 min):** 0–10: assign README/demo/link checklist. 10–25: clean-clone/setup rehearsal from the README using the selected toolchain. 25–40: record and review a concise end-to-end demo, checking it is no longer than three minutes and marked Unlisted. 40–62: individual two-minute code walkthroughs and peer-rating/submission-link check. 62–70: fix documentation/demo issues only and hand off the final checklist. 70–75: confirm each member's learning gate and submission owner.
- **Lead/partner assignments:** Tehilla leads submission-link and final checklist with Mwenya; Cheela leads clean-clone/restart verification with Bright; Blessings leads recording/editing the demo with Bright; Mwenya reviews README class/API/exception explanations with Tehilla; all five rehearse their own contribution and answer one question outside their lead slice.
- **Evidence/deliverable:** README has problem, six classes, actual verified setup/run/test commands, routes, prototype/auth limits, and demo instructions; clean-start evidence; unlisted demo URL and measured duration ≤3 minutes; peer-rating and submission-link checklist; all five have meaningful code commits from their own accounts.
- **Shared-learning checkpoint:** Each member gives a two-minute walkthrough using a code/test artifact and accurately explains a class or behavior they did not lead. Record any missed interview topic and rehearse it again that day.
- **Go/no-go:** Go only when clean setup, demo playback/access, repository/video links, and individual/peer requirements are confirmed. If blocked, remove README ambiguity and trim the demo; do not add features or make cosmetic code changes.

### Wednesday 4 November — final smoke test and submission only

- **Objective:** Verify the frozen submission and submit repository/video/peer-rating links by the course deadline. No feature scope and no planned implementation work.
- **Shared session (75 min):** 0–12: clean-start smoke test using README commands. 12–27: verify five routes, one invalid/error response, and save → full restart → reload. 27–38: run the browser happy path and open the final unlisted demo link. 38–58: all five explain one acceptance artifact they verified and the OOP behavior it demonstrates. 58–68: verify all submission/peer links and each member's name/contribution. 68–75: Tehilla coordinates submission; all five confirm the submitted artifacts and record the confirmation.
- **Lead/partner assignments:** Tehilla leads submission/link confirmation with Mwenya; Cheela leads clean-start and restart smoke test with Bright; Bright verifies GET/POST/PUT behavior and one error case with Blessings; Blessings verifies the served page and demo playback with Mwenya. All five confirm their own peer/interview obligations.
- **Evidence/deliverable:** Completed smoke-test checklist with run result, route/error/restart/browser checks, final repository and unlisted-demo links, peer-rating completion, and submission confirmation.
- **Shared-learning checkpoint:** Each member explains one acceptance artifact they verified (test, route response, persisted request, page, README, or demo) and the OOP behavior it demonstrates.
- **Go/no-go:** Submit only after links and smoke checks are confirmed. If a release-blocking defect appears, do not expand scope: revert the last risky change or make the smallest test-first defect fix, rerun the affected smoke/regression checks, and submit. No new feature, refactor, or styling on submission day.

## Daily checklist sheets

Use the dated day sheets for actionable work items, owners, evidence, and go/no-go checks:

- [Wednesday 28 October — Kickoff](daily-checklists/2026-10-28-kickoff.md)
- [Thursday 29 October — Domain model and service rules](daily-checklists/2026-10-29-domain-model.md)
- [Friday 30 October — Persistence and backend slice](daily-checklists/2026-10-30-persistence.md)
- [Saturday 31 October — Javalin API](daily-checklists/2026-10-31-api.md)
- [Sunday 1 November — Served page and workflow](daily-checklists/2026-11-01-browser-workflow.md)
- [Monday 2 November — Integration and freeze](daily-checklists/2026-11-02-integration-freeze.md)
- [Tuesday 3 November — README and demo](daily-checklists/2026-11-03-readme-demo.md)
- [Wednesday 4 November — Submission](daily-checklists/2026-11-04-submission.md)

## Required API acceptance checklist

Use five small routes that jointly satisfy the course's REST requirement (adjust exact route names only by group agreement):

1. `POST /api/requests` — validate and create a request; return its reference.
2. `GET /api/requests` — list requests for the staff demo view.
3. `GET /api/requests/{reference}` — return one request or a not-found response.
4. `PUT /api/requests/{reference}/status` — validate and update a request's status.
5. `GET /api/health` — report that the local app is running.

The accepted code must demonstrate that the methods work, errors are handled, and request data is still available after restarting the app. Do not claim real resident/staff security unless authentication and authorization are actually implemented and tested.

## Shared learning and interview readiness

Use the 20–25-minute artifact-based learning slot in each shared session for a no-slides code walkthrough. Rotate the driver; the other four ask questions, then rotate so every member speaks or demonstrates. Each member maintains a small personal learning checklist and is only marked ready after explaining and demonstrating each item without relying on their feature lead:

- [ ] Classes/objects and why each responsibility is separate.
- [ ] Encapsulation: point to private fields and explain a controlled update.
- [ ] Inheritance: explain `Resident` and `MaintenanceStaff` as `User` subclasses.
- [ ] Polymorphism: trace the same method call on two different `User` objects and show the test proving different behavior.
- [ ] Custom exception: identify where it is thrown, caught, and translated into an HTTP response.
- [ ] Collections: explain the `List` or `Map`, its key/value types, and how a request is found.
- [ ] REST: explain GET, POST, PUT, request body, status code, and one endpoint end to end.
- [ ] Persistence: demonstrate that a request survives a full app restart.
- [ ] Front end: explain how JavaScript calls the endpoint and displays the result.

On 2–3 November, practice likely interview prompts: “Show polymorphism,” “Why is this field private?”, “What changes if a field is added?”, and “Which code did you write?” Each member should honestly describe their contribution and be able to modify one small field or validation rule in a disposable branch.

## Definition of done

- The core resident-submit → staff-status-update → resident-lookup flow works from the browser.
- The app has at least five Javalin endpoints covering GET, POST, and PUT or DELETE.
- Domain classes are separate; encapsulation, an actually used inheritance relationship, polymorphism, a thrown-and-handled custom exception, and a collection are visible and testable.
- Requests persist across an application restart.
- README states the problem, exact verified run steps, and main classes.
- All five members have meaningful commits from their own accounts, have reviewed/presented work, and pass the shared learning checklist.
- Demo video is no longer than three minutes, shows the running project, and is set to Unlisted as required by the brief.
- All five members have individually rehearsed explanations; group submission and peer-rating requirements are checked.

## Risks and responses

- **No working scaffold yet:** spend the first session getting one minimal Javalin app to start before domain work; use the exact selected toolchain and record the successful command.
- **Late or broken persistence/API integration:** test persistence and the first end-to-end endpoint by 30 October; cut optional styling and features before cutting required course concepts.
- **Uneven learning or contribution:** use the daily rotating teach-back and require each member to explain a code slice outside their own lead task; check commit history before submission.
- **Feature creep:** after the 1 November end-to-end path works, accept only bug fixes needed for the acceptance checklist.
- **Last-day submission risk:** prepare README, video, and links on 3 November; reserve 4 November for verification and submission only.
