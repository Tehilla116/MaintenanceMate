# Maintenance Mate

Maintenance Mate is a local classroom demo of a hostel maintenance-request workflow: residents submit repair requests, demo staff advance their status, and residents look up a request by reference. It uses synthetic data only. The six core types are `User`, `Resident`, `MaintenanceStaff`, `MaintenanceRequest`, `RequestService`, and `RequestRepository` (the storage interface).

## THE PROBLEM

Residents can find it difficult to report hostel maintenance issues and follow their progress, while maintenance staff need a clear way to receive and manage those requests. Maintenance Mate gives hostel residents and maintenance staff a simple shared workflow for submitting, tracking, and updating repair requests.

## HOW TO RUN

1. Install JDK 25 or newer and Maven.
2. Open a terminal at the repository root (the folder containing `maintenancemate/`).
3. Optionally, run the tests before starting the app:

   ```sh
   mvn -f maintenancemate/pom.xml clean test
   ```

4. Start the app from the repository root:

   ```sh
   mvn -f maintenancemate/pom.xml compile exec:java -Dexec.mainClass=vibecoders101.example.Main
   ```

5. Keep that terminal open and visit `http://localhost:7070/` in your browser. The Java app serves the page itself; you do not need to start a separate frontend server.
6. Press Ctrl+C in the terminal to stop the app.

## YOUR CLASSES

- `User` — Base class for a user, holding shared user ID and display name details.
- `Resident` — User type with hostel and room details that can be associated with a maintenance request.
- `MaintenanceStaff` — User type representing staff assigned to handle maintenance requests.
- `MaintenanceRequest` — Represents a reported issue, its resident, reference, status, and timestamps.
- `RequestService` — Applies request rules, including validation, request creation and lookup, and status updates.
- `RequestRepository` — Interface (storage contract) defining how requests are saved, found, listed, and updated; repository implementations provide the storage.

## Backend API (Java 25, Javalin 7)

Use a JDK 25 or newer and Maven. The Maven build targets Java 25. Run these commands from the repository root:

```sh
mvn -f maintenancemate/pom.xml clean test
mvn -f maintenancemate/pom.xml test
mvn -f maintenancemate/pom.xml package
mvn -f maintenancemate/pom.xml compile exec:java -Dexec.mainClass=vibecoders101.example.Main
```

`clean test` removes previous build output and runs the test suite. `package` builds `maintenancemate/target/maintenancemate-1.0-SNAPSHOT.jar`, including the browser page. The last command starts the app; press Ctrl+C in that terminal to stop it. The application listens on port `7070` by default. Set `-Dport=8080` before the exec goal to select another port. The application creates `./data/maintenancemate.db` on startup, relative to its working directory, and persists requests in SQLite. Do not add real personal data; this is a local classroom demonstration.

### Clean demo and persistence restart

For a clean demo, use a fresh clone or isolated worktree so its `./data/maintenancemate.db` is separate from any existing local data. From that clone's repository root, run `mvn -f maintenancemate/pom.xml clean test`, then `mvn -f maintenancemate/pom.xml package`, then start the app with the run command above. Open `http://localhost:7070/` and submit a synthetic request. Save its displayed reference, stop the app with Ctrl+C, start it again with the same run command from the same clone, and look up that reference. The saved request and its status should still be present. Never copy a database containing real-user information into a demo clone.

### Browser workflow demo

Run the existing Maven command from the repository root:

```sh
mvn -f maintenancemate/pom.xml compile exec:java -Dexec.mainClass=vibecoders101.example.Main
```

Open `http://localhost:7070/`. The page is served by Javalin from the application classpath and offers three plain workflows: submit a synthetic resident request, load the request list and advance status as demo staff, then find a request by its reference. A newly submitted reference is also filled into the lookup form. Status transitions are limited to `Received → In Progress → Resolved`; errors from the API are shown on the page.

The page and API are local demo software only. There is no login, authentication, authorization, or real identity handling. Use synthetic data only. The app persists to `./data/maintenancemate.db`; do not use an existing database with real-user information.

### Beginner walkthrough: one POST request from HTTP to SQLite

Start the application as shown above, then submit a request from the browser page or with `curl`:

```sh
curl -i -X POST http://localhost:7070/api/requests \
  -H 'Content-Type: application/json' \
  -d '{"hostelBlock":"Maple Hostel, Block B","roomNumber":"A-12","description":"The sink tap is leaking."}'
```

The API returns `201 Created` and JSON shaped like this. The reference is generated for each request, so your value will differ.

```json
{
  "reference": "MM-012345ABCDEF",
  "hostelBlock": "Maple Hostel, Block B",
  "roomNumber": "A-12",
  "description": "The sink tap is leaking.",
  "status": "Received",
  "assignedStaffId": null,
  "createdAt": "2026-10-30T09:15:30.123Z",
  "updatedAt": "2026-10-30T09:15:30.123Z"
}
```

Follow the request through the code:

1. `Main` registers `POST /api/requests`. Its `createRequest` method reads the JSON body, rejects unknown or missing fields, and reads the three strings.
2. `RequestService.create` trims and validates the values, builds a demo `Resident` and a `MaintenanceRequest`, generates a reference, and asks the repository to save the request.
3. `RequestRepository` is the interface the service depends on. The running app uses `SqliteRequestRepository`, whose `save` method uses a JDBC `PreparedStatement` to insert one row into the `maintenance_requests` table in `./data/maintenancemate.db`. The repository also turns database rows back into objects when reading.
4. `Main.requestRepresentation` turns the saved object into the JSON response.

The path is: `Main` (HTTP and JSON) -> `RequestService` (application rules) -> `RequestRepository` (storage interface) -> `SqliteRequestRepository` (JDBC and SQL) -> SQLite file.

### OOP concepts in this project

- **Encapsulation**: `MaintenanceRequest`, `Resident`, and `MaintenanceStaff` keep their fields private and expose getters. Their constructors validate input.
- **Abstraction**: `RequestRepository` is an interface. `RequestService` calls methods such as `save` and `findByReference` without knowing whether storage is in memory or SQLite.
- **Inheritance**: `Resident` and `MaintenanceStaff` extend the abstract `User` class and reuse its user ID and display-name behavior.
- **Polymorphism**: `Resident` and `MaintenanceStaff` override `User.canAccess` differently. `RequestService` can also work with either `InMemoryRequestRepository` or `SqliteRequestRepository` through the same interface.
- **Composition and dependency injection**: `RequestService` receives a `RequestRepository` and a `Clock` in its constructor instead of creating them itself.
- **Custom exceptions**: `InvalidRequestException`, `RequestNotFoundException`, and the other exception classes describe failures that `Main` maps to HTTP error responses.

This is a classroom demo, not an authentication system. The API has no login or authorization and does not verify who sent a request. The browser page writes API values with `textContent`, so returned text is displayed as text instead of being executed as HTML.

### Routes

- `GET /` — serves the browser workflow page as `text/html` from the Java classpath.
- `POST /api/requests` — JSON `{ "hostelBlock": "Maple Hostel, Block B", "roomNumber": "A-12", "description": "The sink tap is leaking." }`; returns `201` and the complete request representation.
- `GET /api/requests` — returns `{ "requests": [...], "count": n }`.
- `GET /api/requests/{reference}` — returns one request, or `404` if a valid reference is absent.
- `PUT /api/requests/{reference}/status` — JSON `{ "status": "In Progress" }`; only `Received → In Progress → Resolved` is accepted.
- `GET /api/health` — returns `{ "status": "ok" }` only when SQLite can be queried.

Errors use `{ "error": { "code": "...", "message": "...", "details": {} } }`. Validation/malformed JSON returns `400`, absent requests `404`, invalid transitions `409`, unavailable storage `503`, and unexpected errors `500`.

All identities are synthetic. `User.canAccess` is a polymorphism example only: this API has no authentication or authorization boundary and is not a production privacy/security system.

## Group Members

| Full Name | Student Number |
|---|---|
| Tehilla Chiwama | 2410014 |
| Bright Mwansa | 2410010 |
| Mwenya Simpungwe | 2410013 |
| Cheela Mulilo | 2410032 |
| Blessings Syansowa | 2421035 |
