# Thursday 29 October 2026 — Domain model and service rules

## Objective
Implement and test the six agreed class responsibilities and the smallest create/find/status behavior, demonstrating encapsulation, used inheritance, observable polymorphism, a collection, and the service's custom-exception throw path.

## Timeboxed shared-session agenda (75 minutes)
- [ ] **0–5 min — Contract check:** confirm today's work follows the decisions recorded at the 28 Oct kickoff.
- [ ] **5–30 min — TDD slices:** leads and partners take small domain/service behaviors through RED → minimum implementation → GREEN.
- [ ] **30–40 min — Integration:** inspect the six-class model and focused test results; note any mismatch before expanding behavior.
- [ ] **40–65 min — Shared learning:** all five trace one request through the service, collection, and polymorphic `User` call using tests.
- [ ] **65–70 min — Acceptance gate:** verify the required OOP evidence below.
- [ ] **70–75 min — Ownership:** confirm meaningful code commits came from the authors' own GitHub accounts.

## Owners and partner/reviewer tasks

Planned assignments only: implementation work here is not evidence of a member's own-account authorship. Tehilla/Cheela review, all assigned-member ownership, and member-owned commits are still pending; the agent commit is not attributed to either member.

- [ ] Tehilla Chiwama (2410014) owns `Resident` submission fields/validation; Cheela Mulilo (2410032) reviews the behavior tests.
- [ ] Bright Mwansa (2410010) owns `MaintenanceStaff` and valid status transitions; Blessings Syansowa (2421035) reviews.
- [ ] Mwenya Simpungwe (2410013) owns `RequestService` create/find and the defined custom-exception throw path; Tehilla Chiwama (2410014) reviews.
- [ ] Cheela Mulilo (2410032) owns `MaintenanceRequest` private state/controlled updates; Bright Mwansa (2410010) reviews.
- [ ] Blessings Syansowa (2421035) owns the polymorphism behavior test; Mwenya Simpungwe (2410013) reviews.
- [ ] All five review how `RequestRepository` fits the six-class model; persistent implementation is scheduled for 30 Oct.

## Deliverables and evidence
- [x] Represent the six core responsibilities: `User`, `Resident`, `MaintenanceStaff`, `MaintenanceRequest`, `RequestService`, and `RequestRepository`.
- [x] Developer work-log handoff records test-first RED → implementation → focused GREEN evidence for the completed code slices; see the linked tests and commit in Completed evidence below.
- [ ] Confirm each slice was committed from its assigned author's own GitHub account; individual member ownership and partner reviews are still pending and are not inferred from the implementation commit.
- [x] Tests cover private state/controlled updates, actual `Resident extends User` and `MaintenanceStaff extends User` inheritance, create/find/status behavior, and `List`/`Map` collection use in the in-memory path.
- [x] The polymorphism test calls `canAccess` through `User` references and asserts different outcomes for resident/staff identities.
- [x] `RequestService` throws `InvalidRequestException` for invalid create input; HTTP exception mapping remains scheduled for the API work on 31 Oct.
- [x] Core repository tests use the in-memory `Map`-backed collection; no second persistence choice was added.
- [x] Plain `mvn test` and `mvn package` pass using the commands documented in the [README](../../../README.md); 14 tests pass.

## All-five shared-learning checkpoint
- [ ] Each member uses a passing test/class diagram to point to one private field and controlled update, the real subclass relation, the two polymorphic outcomes, and the exception's throw location.
- [ ] Retain the annotated test/class evidence and record any explanation gap to recheck today.

## Go/no-go gate
- [ ] **GO** when focused tests pass and the actual inheritance, polymorphic dispatch, collection use, and exception throw are visible in code/tests.
- [ ] **NO-GO / cut:** simplify validation/status edge cases to the agreed workflow and preserve one clear example of each required concept. Cut optional fields and polish; do not replace used inheritance/polymorphism with comments, an unused interface, or an exception that is never thrown. Record any blocker and owner.

## Completed evidence and outstanding team work

- Six-class implementation and service: [User.java](../../../maintenancemate/src/main/java/vibecoders101/example/domain/User.java), [Resident.java](../../../maintenancemate/src/main/java/vibecoders101/example/domain/Resident.java), [MaintenanceStaff.java](../../../maintenancemate/src/main/java/vibecoders101/example/domain/MaintenanceStaff.java), [MaintenanceRequest.java](../../../maintenancemate/src/main/java/vibecoders101/example/domain/MaintenanceRequest.java), [RequestService.java](../../../maintenancemate/src/main/java/vibecoders101/example/service/RequestService.java), [InvalidRequestException.java](../../../maintenancemate/src/main/java/vibecoders101/example/service/InvalidRequestException.java), and [RequestRepository.java](../../../maintenancemate/src/main/java/vibecoders101/example/repository/RequestRepository.java) with [InMemoryRequestRepository.java](../../../maintenancemate/src/main/java/vibecoders101/example/repository/InMemoryRequestRepository.java).
- Tests: [ResidentTest.java](../../../maintenancemate/src/test/java/vibecoders101/example/domain/ResidentTest.java), [MaintenanceRequestTest.java](../../../maintenancemate/src/test/java/vibecoders101/example/domain/MaintenanceRequestTest.java), [UserPolymorphismTest.java](../../../maintenancemate/src/test/java/vibecoders101/example/domain/UserPolymorphismTest.java), [RequestServiceTest.java](../../../maintenancemate/src/test/java/vibecoders101/example/service/RequestServiceTest.java), [RequestServiceStatusTest.java](../../../maintenancemate/src/test/java/vibecoders101/example/service/RequestServiceStatusTest.java), and [InMemoryRequestRepositoryTest.java](../../../maintenancemate/src/test/java/vibecoders101/example/repository/InMemoryRequestRepositoryTest.java).
- Code slice commit: `0701cb2`. Follow-up stale generated-class fix: `4829c8b` (plain Maven test reproducibility); reviewer APPROVE applies to the code slice at that HEAD. These do not demonstrate Tehilla/Cheela review, any member's authorship of the implementation commit, own-account commits by each member, or all-five learning/session completion; those remain pending.
