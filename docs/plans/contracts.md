# Class, Service and Exception Contracts (recorded by Mwenya Simpungwe, 2410013)

Source: `api-and-data-contract.md` (proposal; all-five approval pending) and Project Proposal Part 1.
Review: Tehilla Chiwama (2410014) to review; all five to sign off in the contract file.

## Six classes
| Class | Responsibility |
|---|---|
| `User` | Abstract base: private synthetic `userId`/`displayName`; controlled getters; demo-only `canAccess(...)` for polymorphism (not security) |
| `Resident extends User` | Private `hostelBlock`, `roomNumber`; submits and looks up requests |
| `MaintenanceStaff extends User` | Synthetic staff identity; views requests and asks the service to advance status |
| `MaintenanceRequest` | Private reference, resident/location, description, status, `assignedStaffId` (nullable), `createdAt`, `updatedAt`; controlled status-transition method, no bypass setters |
| `RequestService` | Validates/normalizes input, creates requests, finds/lists, enforces transitions, sets timestamps, delegates storage health check |
| `RequestRepository` | Private JDBC/SQLite details; save, list, find, atomic status update, availability check; no HTTP and no transition policy |

## RequestService contract
| Method | Behavior |
|---|---|
| `create(hostelBlock, roomNumber, description) -> MaintenanceRequest` | Trims; `hostelBlock` 1-100, `roomNumber` 1-30, `description` 1-1000 chars. Reference `MM-` + 12 uppercase hex. Status `Received`, `assignedStaffId` null, `createdAt == updatedAt` (UTC, seconds). Retries a colliding reference (max 5). Throws `InvalidRequestException` (all bad fields reported together) or `ReferenceConflictException`. Nothing saved on failure. |
| `findByReference(reference)` | Malformed -> `InvalidRequestException`; well-formed but absent -> `RequestNotFoundException`. |
| `list()` | All requests, `createdAt` descending then `reference` ascending; empty list if none. |
| `updateStatus(reference, status)` | NOT built yet. `Received -> In Progress -> Resolved` only; disallowed -> `InvalidStatusTransitionException`; absent -> `RequestNotFoundException`; only `status` and `updatedAt` change. |
| `checkStorageHealth()` | NOT built yet (30 Oct, with SQLite). |

## Exception contract
| Exception | Thrown when | HTTP | Code |
|---|---|---|---|
| `InvalidRequestException` | Body/path/field/status value invalid; carries `getFields()` | 400 | `VALIDATION_ERROR` |
| `RequestNotFoundException` | Well-formed reference not found | 404 | `REQUEST_NOT_FOUND` |
| `InvalidStatusTransitionException` | Backward, skipped or repeated status (or raced update) | 409 | `STATUS_TRANSITION_CONFLICT` |
| `ReferenceConflictException` (name proposed) | Reference still collides after bounded retry | 409 | `REFERENCE_CONFLICT` |
| `StorageUnavailableException` | SQLite unavailable (wrapped by repository) | 503 | `STORAGE_UNAVAILABLE` |
| Any other | Unexpected | 500 | `INTERNAL_ERROR` |

One central Javalin handler maps these to the error envelope. Routes do not serialize errors themselves.
