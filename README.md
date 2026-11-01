# Maintenance Mate

## Backend API (Java 17, Javalin 7)

Run the backend from the repository root:

```sh
mvn -f maintenancemate/pom.xml test
mvn -f maintenancemate/pom.xml package
mvn -f maintenancemate/pom.xml compile exec:java -Dexec.mainClass=vibecoders101.example.Main
```

The API listens on port `7070` by default. Set `-Dport=8080` before the exec goal to select another port. The application creates `./data/maintenancemate.db` on startup and persists requests in SQLite. Do not add real personal data; this is a local classroom demonstration.

### Routes

- `POST /api/requests` — JSON `{ "hostelBlock": "Maple Hostel, Block B", "roomNumber": "A-12", "description": "The sink tap is leaking." }`; returns `201` and the complete request representation.
- `GET /api/requests` — returns `{ "requests": [...], "count": n }`.
- `GET /api/requests/{reference}` — returns one request, or `404` if a valid reference is absent.
- `PUT /api/requests/{reference}/status` — JSON `{ "status": "In Progress" }`; only `Received → In Progress → Resolved` is accepted.
- `GET /api/health` — returns `{ "status": "ok" }` only when SQLite can be queried.

Errors use `{ "error": { "code": "...", "message": "...", "details": {} } }`. Validation/malformed JSON returns `400`, absent requests `404`, invalid transitions `409`, unavailable storage `503`, and unexpected errors `500`.

All identities are synthetic. `User.canAccess` is a polymorphism example only: this API has no authentication or authorization boundary and is not a production privacy/security system.
