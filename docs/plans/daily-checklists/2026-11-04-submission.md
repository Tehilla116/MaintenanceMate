# Wednesday 4 November 2026 — Final smoke test and submission only

## Objective
Verify and submit the frozen repository, demo, and peer-rating requirements by the course deadline. No features, refactors, or planned implementation work today.

## Timeboxed shared-session agenda (75 minutes)
- [ ] **0–12 min — Clean start:** run the README's exact kickoff-recorded commands and record the result.
- [ ] **12–27 min — Backend smoke:** verify five routes, one invalid/error response, and save → full restart → reload.
- [ ] **27–38 min — Browser/demo:** run the browser happy path and open the final Unlisted demo link; verify duration ≤3 minutes.
- [ ] **38–58 min — All-five confirmation:** each member explains one acceptance artifact they just verified and the OOP behavior it demonstrates.
- [ ] **58–68 min — Submission checks:** confirm repository/video links, peer-rating completion, names/contributions, and course destination/deadline.
- [ ] **68–75 min — Submit:** Tehilla coordinates submission; all five verify the submitted artifacts and retain confirmation evidence.

## Owners and partner/reviewer tasks
- [ ] Tehilla Chiwama (2410014) owns submission/link confirmation; Mwenya Simpungwe (2410013) reviews the destination and evidence.
- [ ] Cheela Mulilo (2410032) owns clean-start and restart smoke test; Bright Mwansa (2410010) reproduces it.
- [ ] Bright Mwansa (2410010) verifies GET/POST/PUT (or agreed DELETE) behavior and one error case; Blessings Syansowa (2421035) reviews.
- [ ] Blessings Syansowa (2421035) verifies the Javalin-served page and demo playback; Mwenya Simpungwe (2410013) reviews.
- [ ] All five confirm their own name, contribution, peer-rating/interview obligations, and successful access to the submitted links.

## Deliverables and evidence
- [ ] Record clean-start result using the exact README command recorded at kickoff; do not invent or substitute commands.
- [ ] Smoke-check at least five Javalin 5+ endpoints with GET, POST, and PUT or DELETE, plus one invalid/error response.
- [ ] Verify a request survives full restart, the Javalin-served browser workflow works, and the final demo opens, is Unlisted, and is ≤3 minutes.
- [ ] Verify the repository contains the six agreed classes and evidence for encapsulation, used inheritance, observable polymorphism, a custom exception actually thrown and caught, and `List`/`Map` use.
- [ ] Confirm README, repository link, video link, peer-rating completion, member names/contributions, correct submission destination, and actual submission confirmation.
- [ ] Keep the completed smoke checklist and confirmation/link evidence. Use demo data only; make no real auth/security claim.
- [ ] No code changes are scheduled. If a release-blocking defect appears, stop feature work; prefer reverting the last risky change. Any unavoidable minimal hotfix must follow test-first → observed RED → minimum fix → focused GREEN → relevant regression → author's own-account commit, then rerun affected smoke checks.

## All-five shared-learning checkpoint
- [ ] Each member explains one verified artifact (test, route response, persisted request, page, README, or demo) and the OOP behavior it demonstrates.
- [ ] Record all five confirmations against the actual artifacts, not a verbal claim without a check.

## Go/no-go gate
- [ ] **GO / SUBMIT** only after clean-start, route/error, restart, browser/demo, repository/video, and peer-rating checks pass and submission confirmation is captured.
- [ ] **NO-GO / cut:** do not add a feature or expand scope. If any release-blocking check fails, hold submission long enough to revert the risky change or resolve only the minimal blocker, rerun the affected checks, and record the outcome; escalate if unresolved by the course deadline.
