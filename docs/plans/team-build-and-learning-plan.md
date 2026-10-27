# Maintenance Mate — Team Build and Shared-Learning Plan

**Planning date / first work day:** Wednesday, 28 October 2026
**Target submission:** Wednesday, 4 November 2026
**Status:** Project proposal accepted; implementation plan only.
**Goal:** Deliver a small, working hostel-maintenance tracker and ensure all five members can explain and demonstrate the same required OOP concepts—not just the code they personally led.

## Verified starting point

- The repository is `/Users/tehillachiwama/projects/MaintenanceMate`, on `main`.
- The tracked README currently contains only the project title. No Java source, build file, test suite, or application scaffold is present yet.
- `docs/` contains the accepted proposal and its editable source. The accepted proposal lists Tehilla Chiwama (2410014), Bright Mwansa (2410010), Mwenya Simpungwe (2410013), Cheela Mulilo (2410032), and Blessings Syansowa (2421035).
- The assignment brief requires real classes, encapsulation, at least one used inheritance or interface relationship, polymorphism, a custom exception that is thrown and handled, collections, at least five Javalin REST endpoints including GET and POST plus PUT or DELETE, data that survives restart, a simple web front end, a short demo video, and individual member understanding.

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

## Schedule and completion gates

| Date | Deliverable | All-member learning/checkpoint |
|---|---|---|
| **Wed 28 Oct** | Kickoff: settle all five decisions above (Java/build/test toolchain, one file-or-SQLite store, request/status/reference and JSON/API contract, GitHub ownership/access, and video/submission roles); record them and exact run commands. Create the scaffold, prove it builds/runs, grant all five repository access, and start the six domain classes. | Draw the six-class relationship and agree the API/data contract. Each member explains classes/objects, private fields and encapsulation, and `Resident`/`MaintenanceStaff` inheritance. |
| **Thu 29 Oct** | Complete the six domain classes and first focused tests. Implement the service's create/find/status rules, collection use, validation, and custom-exception path. | All five trace a request through the `Map`/`List`; explain encapsulation and the exception throw/catch. Demonstrate the same polymorphic `User` method on both user types with a test. |
| **Fri 30 Oct** | Implement repository save/load/find using the chosen store; prove persistence across restart. Connect request creation and lookup service behavior and start the first API routes. | All five run save → stop/restart → reload and explain repository ownership. Each traces the custom exception to its intended HTTP error response. |
| **Sat 31 Oct** | Complete at least five Javalin endpoints, including GET, POST, and PUT, with structured success/error responses; integrate route, service, and repository. | Each member explains one endpoint end to end, including method, request body, status code, and error behavior. |
| **Sun 1 Nov** | Serve and connect the simple front end; complete the resident submit → staff status update → resident lookup happy path. | Rotate the driver. Every member runs and explains the complete workflow and reviews/walks through code outside their lead slice. |
| **Mon 2 Nov** | Run integration and regression checks; fix only blocking defects and freeze feature scope. | Blind teach-back: randomly assign each person a class, OOP concept, endpoint, and failure case. Re-teach gaps the same day. |
| **Tue 3 Nov** | Finish README setup/class list, clean-clone run-through, short demo recording/edit, and final review; confirm meaningful code commits, peer-rating requirements, and submission links. | Each person rehearses a two-minute code walkthrough and answers a question about a class they did not lead. Confirm the video meets the brief. |
| **Wed 4 Nov** | Final smoke test and submit repository/video/peer-rating links by the course deadline. | No new features. Verify clean setup instructions and working demo links; each member confirms understanding of the submitted code. |

**Minimum daily session and priorities:** The plan assumes all five members can attend one shared 60-minute session on each date from 28 October through 4 November. Reserve 20 minutes for the listed concept checkpoint and use the remaining time for the day's deliverable; leads and partners pair on their assigned slice within or between sessions. If a session is shortened, keep the shared learning checkpoint and prioritize, in order: required OOP concepts/tests, persistent request workflow, five API endpoints, simple front end, then README/video polish. Do not move setup, decisions, or other work before the 28 October kickoff.

## Required API acceptance checklist

Use five small routes that jointly satisfy the course's REST requirement (adjust exact route names only by group agreement):

1. `POST /api/requests` — validate and create a request; return its reference.
2. `GET /api/requests` — list requests for the staff demo view.
3. `GET /api/requests/{reference}` — return one request or a not-found response.
4. `PUT /api/requests/{reference}/status` — validate and update a request's status.
5. `GET /api/health` — report that the local app is running.

The accepted code must demonstrate that the methods work, errors are handled, and request data is still available after restarting the app. Do not claim real resident/staff security unless authentication and authorization are actually implemented and tested.

## Shared learning and interview readiness

Reserve 20–30 minutes each day for a no-slides code walkthrough. Use a rotating driver; the other four ask questions. Each member maintains a small personal learning checklist and is only marked ready after explaining and demonstrating each item without relying on their feature lead:

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
