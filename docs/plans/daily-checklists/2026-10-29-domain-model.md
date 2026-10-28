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
- [ ] Tehilla Chiwama (2410014) owns `Resident` submission fields/validation; Cheela Mulilo (2410032) reviews the behavior tests.
- [ ] Bright Mwansa (2410010) owns `MaintenanceStaff` and valid status transitions; Blessings Syansowa (2421035) reviews.
- [ ] Mwenya Simpungwe (2410013) owns `RequestService` create/find and the defined custom-exception throw path; Tehilla Chiwama (2410014) reviews.
- [ ] Cheela Mulilo (2410032) owns `MaintenanceRequest` private state/controlled updates; Bright Mwansa (2410010) reviews.
- [ ] Blessings Syansowa (2421035) owns the polymorphism behavior test; Mwenya Simpungwe (2410013) reviews.
- [ ] All five review how `RequestRepository` fits the six-class model; persistent implementation is scheduled for 30 Oct.

## Deliverables and evidence
- [ ] Represent exactly these six responsibilities: `User`, `Resident`, `MaintenanceStaff`, `MaintenanceRequest`, `RequestService`, `RequestRepository`.
- [ ] For each code slice: write the behavior test first; run and observe expected RED; implement the minimum; run focused GREEN; run relevant regression; commit from the author's own GitHub account.
- [ ] Test private state through controlled methods, an actual `Resident extends User` and `MaintenanceStaff extends User` relationship, create/find/status rules, and `List`/`Map` use in the agreed path.
- [ ] Test the same meaningful method through a `User` reference with two different observable outcomes; retain the test output as polymorphism evidence.
- [ ] Test that the service actually throws the agreed custom exception for its defined invalid condition. HTTP catching/mapping is scheduled with the API on 31 Oct.
- [ ] Keep today's core tests in the agreed in-memory collection path; do not add another persistence choice.
- [ ] Run tests only with the exact command recorded at the 28 Oct kickoff; record command and result.

## All-five shared-learning checkpoint
- [ ] Each member uses a passing test/class diagram to point to one private field and controlled update, the real subclass relation, the two polymorphic outcomes, and the exception's throw location.
- [ ] Retain the annotated test/class evidence and record any explanation gap to recheck today.

## Go/no-go gate
- [ ] **GO** when focused tests pass and the actual inheritance, polymorphic dispatch, collection use, and exception throw are visible in code/tests.
- [ ] **NO-GO / cut:** simplify validation/status edge cases to the agreed workflow and preserve one clear example of each required concept. Cut optional fields and polish; do not replace used inheritance/polymorphism with comments, an unused interface, or an exception that is never thrown. Record any blocker and owner.
