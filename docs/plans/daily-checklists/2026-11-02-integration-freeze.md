# Monday 2 November 2026 — Integration, regression, and feature freeze

## Objective
Prove the required concepts and workflow together, repair only acceptance-blocking defects, complete the shared teach-back, and freeze feature scope.

## Timeboxed shared-session agenda (75 minutes)
- [ ] **0–10 min — Triage:** inspect the acceptance checklist and actual failures.
- [ ] **10–28 min — Blocking fixes only:** reproduce/rank blockers; for each code fix use the complete test-first cycle.
- [ ] **28–40 min — Regression:** rerun relevant tests and the persistence restart check.
- [ ] **40–62 min — Blind teach-back:** randomly assign classes, concepts, routes, and failure cases; use real code/test artifacts.
- [ ] **62–70 min — Gate/freeze:** record pass/fail evidence and freeze scope.
- [ ] **70–75 min — Handoff:** assign only README/demo/submission preparation; no feature work remains.

## Owners and partner/reviewer tasks
- [ ] Tehilla Chiwama (2410014) owns POST/request workflow regression; Cheela Mulilo (2410032) reproduces and reviews.
- [ ] Bright Mwansa (2410010) owns status/PUT regression; Blessings Syansowa (2421035) reproduces and reviews.
- [ ] Mwenya Simpungwe (2410013) owns custom-exception and polymorphism evidence; Tehilla Chiwama (2410014) reviews.
- [ ] Cheela Mulilo (2410032) owns persistence/restart regression; Bright Mwansa (2410010) reproduces and reviews.
- [ ] Blessings Syansowa (2421035) owns page-to-API regression; Mwenya Simpungwe (2410013) reproduces and reviews.
- [ ] Each author commits any blocker fix from their own GitHub account; partner reproduces it.

## Deliverables and evidence
- [x] Confirm exactly the six agreed classes: `User`, `Resident`, `MaintenanceStaff`, `MaintenanceRequest`, `RequestService`, `RequestRepository`. Evidence: source tree `maintenancemate/src/main/java/vibecoders101/example/{domain,service,repository}`.
- [x] Verify encapsulation and an actually used `Resident`/`MaintenanceStaff` inheritance relationship. Evidence: `User` subclasses, private fields, `ResidentTest.identityAndLocationAreNotExposedThroughPublicMutators`.
- [x] Verify observable polymorphism through the same `User` behavior with two outcomes; keep the passing test. Evidence: `UserPolymorphismTest.sameCanAccessCallDispatchesByUserRoleAndRequestAssignment`.
- [x] Verify the custom exception is actually thrown and caught/mapped to an HTTP error; retain service/route test evidence. Evidence: `RequestApiTest` exercises `RequestNotFoundException`, `InvalidStatusTransitionException`, `ReferenceGenerationException`, and `StorageUnavailableException` mappings; existing `InvalidRequestException` validation mapping also covered.
- [x] Verify `List`/`Map` use in the working path and at least five Javalin 5+ endpoints including GET, POST, and PUT or DELETE. Evidence: `Main.java` has `GET /`, `GET /api/health`, `POST /api/requests`, `GET /api/requests`, `GET /api/requests/{reference}`, and `PUT /api/requests/{reference}/status`; the API uses `List` and `Map` in routes and representations.
- [x] Verify request data survives a full restart and the basic page served by Javalin completes the browser workflow. Evidence: existing `RequestApiTest.createdRequestRemainsAvailableAfterApplicationAndRepositoryRestart` and new `BrowserWorkflowTest.browserWorkflowUsesApiAndPersistsRequestsAcrossApplicationRestart`; headless Chrome scenario recorded in the 1 Nov checklist.
- [ ] For each blocker code fix: write behavior test first; observe expected RED; minimum implementation; focused GREEN; relevant regression; own-account commit. Do not invent test commands; use the exact command recorded at kickoff. (No unrelated acceptance fixes were made; browser-page route had expected RED then GREEN.)
- [ ] Record actual green/regression results, remaining blocker log, all-five teach-back results, and explicit feature-freeze note. README and demo completion remain scheduled for 3 Nov.

## All-five shared-learning checkpoint
- [ ] Each member demonstrates a randomly selected concept outside their lead slice from code/test/route/restart/browser evidence, not slides.
- [ ] Re-teach and recheck any gap today; record who explained which artifact and result.

## Go/no-go gate
- [ ] **GO** when all acceptance evidence above passes, the browser workflow works, and all five clear teach-back; freeze features.
- [ ] **NO-GO / cut:** spend remaining time only on required concepts/workflow and acceptance blockers. Cut enhancements, styling, and optional fields. No new features after today; record any unresolved blocker and owner, and do not mark acceptance complete without evidence.

## Agent implementation evidence (software evidence only; no human/team gate signoff)
- Collection/storage coverage already exists: `SqliteRequestRepositoryTest` covers save/reload and restart; no duplicate test added.
- Custom exception/status API mappings already exist: `RequestApiTest` covers validation, 404, 409, reference conflict, and 503. Existing `UserPolymorphismTest` and `ResidentTest` cover polymorphic dispatch and encapsulation; retained without duplicating.
- Test commands and verified results: `mvn -f maintenancemate/pom.xml -Dtest=BrowserWorkflowTest test` — 2 tests, 0 failures/errors/skips after expected initial RED; `mvn -f maintenancemate/pom.xml clean test` — 34 tests, 0 failures/errors/skips; `mvn -f maintenancemate/pom.xml test` — 34 tests, 0 failures/errors/skips; `mvn -f maintenancemate/pom.xml package` — success, classpath page present in JAR; `git diff --check` — clean.
- Browser evidence: headless Chrome accepted synthetic request, staff list/status advance, then reference lookup; details and evidence ID in 1 Nov checklist. A favicon 404 was observed and is unrelated to acceptance. Not an all-five run.
- Feature scope stayed within the 1/2 Nov acceptance work. Implementation commit: `139c194ac3e104f591a649ac917ecf08ecd4c45a` on `feature/maintenance-mate-sqlite-api`. No human review/own-account commit signoff, partner reproductions, multi-member browser verification, or teach-back has been claimed.
