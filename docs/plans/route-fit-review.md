# Route Fit Review (Mwenya Simpungwe, 2410013)

Reviewed: `api-and-data-contract.md` (proposal, approval pending) against `RequestService`.
Status of each route = can the service support it as the contract describes?

| # | Route | Service method | Fit | Notes |
|---|---|---|---|---|
| 1 | `POST /api/requests` | `create(hostelBlock, roomNumber, description)` | Fits (built + tested) | 400 via `InvalidRequestException` (with `getFields()` for `details.fields`); 409 via `ReferenceConflictException`; 503 comes from the repository on 30 Oct |
| 2 | `GET /api/requests` | `list()` | Fits (built + tested) | Newest first, ties by reference ascending, empty list when none. 503 comes from the repository on 30 Oct |
| 3 | `GET /api/requests/{reference}` | `findByReference(reference)` | Fits (built + tested) | Malformed -> `InvalidRequestException` (400); absent -> `RequestNotFoundException` (404) |
| 4 | `PUT /api/requests/{reference}/status` | `updateStatus(reference, status)` | NOT built yet | Needs Bright's transition rules and `InvalidStatusTransitionException` (not created yet). Also needs repository compare-and-update (30 Oct) |
| 5 | `GET /api/health` | `checkStorageHealth()` | NOT built yet | Only meaningful with SQLite (30 Oct); needs `StorageUnavailableException` |

Routes 1-3 are ready for the 31 Oct Javalin layer. Routes 4-5 are blocked on other owners/dates, not on a design problem.

## Where the service differs from what I first assumed (now fixed)
- Route paths are `/api/requests...` (I had guessed `/requests`).
- Reference format is `MM-` + 12 uppercase hex (I had `MR-0001`).
- Not-found throws `RequestNotFoundException` (I had returned an empty Optional).
- Create takes `hostelBlock`, `roomNumber`, `description` (I had a `Resident` plus one `location`).
- There is no assignment route, so the assign route I guessed earlier is dropped.

## Issues and questions to raise with the team
1. **Overlap with Bright:** the contract makes `RequestService` enforce transitions and throw `InvalidStatusTransitionException`, while the 29 Oct plan gives "valid status transitions" to Bright. Agree who writes `updateStatus` and the exception.
2. **Exception list:** the contract names 4 exceptions (`InvalidRequestException`, `RequestNotFoundException`, `InvalidStatusTransitionException`, `StorageUnavailableException`) but not one for `REFERENCE_CONFLICT`. I added `ReferenceConflictException`; confirm the name. Also confirm these extra classes are acceptable alongside the "six classes" rule.
3. **Who sorts the list:** the contract sets the order but the flow says the repository returns the list. I sort in the service so it works today; decide whether the repository (SQL `ORDER BY`) takes over on 30 Oct.
4. **Resident identity:** `Resident` holds `hostelBlock`/`roomNumber`, but the API has no resident identity. The service builds a synthetic `Resident("demo-resident", ...)` per request. Tehilla should confirm this matches her `Resident`.
5. **`MaintenanceRequest` shape:** my placeholder takes `(reference, resident, description, createdAt)`. Cheela's real constructor must match, or the service call changes.
6. **Messages:** the error envelope must not leak raw exception messages. Service messages are fixed safe strings; the HTTP handler should use the fixed envelope message and `getFields()`.
7. **Toolchain:** scaffold POM targets Java 17 (I tested on Java 21 with JUnit 5 via a script). Exact build/test command still needs the team's agreed Maven command.
