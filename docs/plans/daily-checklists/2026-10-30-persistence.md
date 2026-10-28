# Friday 30 October 2026 — Persistence and backend slice

## Objective
Prove a request survives a full application stop/restart using the single kickoff-selected store, connect repository/service behavior, and establish the first route-to-service path.

## Timeboxed shared-session agenda (75 minutes)
- [ ] **0–8 min — Model/store:** confirm the agreed store and request representation.
- [ ] **8–30 min — Persistence TDD:** pair on save/find/load and restart behavior; observe RED before implementation.
- [ ] **30–42 min — Integration:** connect repository and service behavior to the first route in the agreed contract; include an error case.
- [ ] **42–62 min — Learning artifact:** all five perform save → stop → restart → reload and explain ownership from the test.
- [ ] **62–70 min — Gate/evidence:** record the actual restart result and remaining defect.
- [ ] **70–75 min — Handoff:** verify meaningful commits and the API work handoff.

## Owners and partner/reviewer tasks
- [ ] Cheela Mulilo (2410032) owns `RequestRepository` save/load/find and restart test; Bright Mwansa (2410010) reviews and reproduces it.
- [ ] Mwenya Simpungwe (2410013) owns service-to-repository behavior and not-found/invalid exception coverage; Tehilla Chiwama (2410014) reviews.
- [ ] Tehilla Chiwama (2410014) checks the request-creation contract against repository behavior; Cheela Mulilo (2410032) reviews.
- [ ] Bright Mwansa (2410010) checks list/status data integrity; Blessings Syansowa (2421035) reviews.
- [ ] Blessings Syansowa (2421035) prepares the agreed page-to-API acceptance checklist; Mwenya Simpungwe (2410013) reviews. No new UI framework or store.
- [ ] All five reproduce the restart test and inspect the same result.

## Deliverables and evidence
- [ ] For each repository/service/route code slice: write its behavior test first; run and observe expected RED; implement the minimum; focused GREEN; relevant regression; commit from the author's own GitHub account.
- [ ] A test writes a request, stops and restarts the app using the exact recorded procedure, reloads it, and verifies its reference and status.
- [ ] Focused repository/service tests pass; the first agreed route reaches the service and has one recorded success/error result.
- [ ] Record the exact restart procedure, test command from the 28 Oct kickoff, and real output/evidence; do not invent commands.

## All-five shared-learning checkpoint
- [ ] Each member traces `RequestService → RequestRepository → chosen store` from the restart test and explains which layer owns validation versus persistence.
- [ ] Preserve the test/result or short annotated trace as the learning artifact.

## Go/no-go gate
- [ ] **GO** only after a full app restart preserves and reloads a request with the expected reference/status, and the first route/service path is reproducible.
- [ ] **NO-GO / cut:** repair the one selected store and keep scope to save/find/list. Cut serialization polish, optional metadata, and styling; do not accept in-memory-only success or add a second store. Record the blocker and owner rather than proceeding on a false persistence pass.
