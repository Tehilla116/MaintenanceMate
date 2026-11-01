# Sunday 1 November 2026 — Served page and end-to-end workflow

## Objective
Connect a plain page served by Javalin to the agreed API and complete resident submission → staff list/status update → resident reference lookup using persisted demo data.

## Timeboxed shared-session agenda (75 minutes)
- [ ] **0–8 min — Route contract:** confirm browser work uses the stable API routes.
- [ ] **8–28 min — UI TDD slices:** test one page behavior at a time before the minimum HTML/JavaScript implementation.
- [ ] **28–40 min — Browser run:** perform the full workflow against the persistence-backed app.
- [ ] **40–60 min — Rotate driver:** all five operate and explain one workflow handoff from page/API evidence.
- [ ] **60–68 min — Scope freeze:** once the core workflow passes, accept only acceptance-blocking defects.
- [ ] **68–75 min — Evidence/commits:** record browser result and verify meaningful own-account commits.

## Owners and partner/reviewer tasks
- [ ] Blessings Syansowa (2421035) owns the Javalin-served HTML/JavaScript page and API wiring; Mwenya Simpungwe (2410013) reviews.
- [ ] Tehilla Chiwama (2410014) owns resident submit/reference lookup behavior; Cheela Mulilo (2410032) reviews.
- [ ] Bright Mwansa (2410010) owns staff list/status action; Blessings Syansowa (2421035) reviews.
- [ ] Cheela Mulilo (2410032) verifies save/restart/read behavior from the page workflow; Bright Mwansa (2410010) reviews.
- [ ] Mwenya Simpungwe (2410013) checks validation/error display; Tehilla Chiwama (2410014) reviews.
- [ ] All five verify the same browser path using demo data only.

## Deliverables and evidence
- [ ] For each HTML/JavaScript/API wiring code slice: write a behavior test first; run and observe expected RED; implement the minimum; run focused GREEN; run relevant regression; commit from the author's own GitHub account.
- [x] A basic page is served by Javalin and completes resident submit → staff list/status update → resident lookup, displaying the returned reference/status. Evidence: `Main.java`, classpath page `maintenancemate/src/main/resources/vibecoders101/example/index.html`, `BrowserWorkflowTest`, and browser scenario evidence `ev_df4f8928f93a771c58c1c93b335ab1809204fc2809432da002d1d7cda133102e`.
- [x] A page GET and create/list/status/lookup API operations are tested against temporary SQLite storage; same-database lookup is verified from a newly constructed Javalin app/repository. Evidence: `BrowserWorkflowTest`.
- [ ] Record a reproducible manual browser run and available focused test results; use only the exact recorded run/test commands. (A headless Chrome scenario passed; no human/manual multi-member browser run has been recorded.)
- [x] Keep the UI to plain form/list/status output; use demo data. Do not describe the prototype as having real login, secure sessions, or production authorization. Evidence: page copy, synthetic-only data, and safe DOM construction with `textContent`, `createElement`, and `replaceChildren`.

## All-five shared-learning checkpoint
- [ ] All five run the same end-to-end browser scenario and use page source, browser/network evidence, or route output to explain how a JavaScript action reaches a Java route and updates the persisted request.
- [ ] Retain the walkthrough checklist or browser evidence and record any explanation gap for recheck.

## Go/no-go gate
- [ ] **GO** when the full three-step browser path works against saved data and each member can trace one handoff.
- [ ] **NO-GO / cut:** simplify to one page, plain controls, and no CSS polish; keep submission, staff status update, and resident lookup. After the workflow gate, do not add features; record any acceptance-blocking defect and owner.

## Agent implementation evidence (code-only; shared/human gates remain open)
- Focused TDD: wrote `BrowserWorkflowTest` first; `mvn -f maintenancemate/pom.xml -Dtest=BrowserWorkflowTest test` failed as expected with GET `/` returning 404; added the classpath page/route; repeated command passed (2 tests).
- Browser smoke: launched the local app on port 17070 with a temporary `user.dir` under Hermes scratch, then ran headless Chrome scenario `ev_df4f8928f93a771c58c1c93b335ab1809204fc2809432da002d1d7cda133102e`. Submit returned 201, list GET 200, status PUT 200, follow-up list and lookup GET 200. Chrome emitted a non-workflow `/favicon.ico` 404. This is agent smoke evidence only; not an all-five/manual group browser test.
- Commands: `mvn -f maintenancemate/pom.xml -Dtest=BrowserWorkflowTest test`; `mvn -f maintenancemate/pom.xml clean test`; `mvn -f maintenancemate/pom.xml test`; `mvn -f maintenancemate/pom.xml package`; `git diff --check`.
- Verification counts: focused 2 tests, 0 failures/errors/skips; each full-suite run 34 tests, 0 failures/errors/skips. Maven package includes `vibecoders101/example/index.html` in `maintenancemate-1.0-SNAPSHOT.jar`.
- Implementation commit: `139c194ac3e104f591a649ac917ecf08ecd4c45a` on branch `feature/maintenance-mate-sqlite-api`. Human review/own-account signoff, partner reviews, manual multi-member testing, and group teach-back remain unchecked.
