# Maintenance Mate

## Backend scaffold (Java 17)

The minimal Javalin API and domain/service model live under `maintenancemate/`.

```sh
cd maintenancemate
mvn test
mvn package
mvn compile exec:java -Dexec.mainClass=vibecoders101.example.Main
```

The app listens on port `7070` by default. Set `-Dport=8080` (for example, `mvn compile exec:java -Dport=8080 -Dexec.mainClass=vibecoders101.example.Main`) to select another port. `GET http://localhost:7070/api/health` returns `{"status":"ok"}`. The health route is exercised with Javalin's test helper on an ephemeral test port.

The scaffold pins Javalin 7.2.4, targets Java 17, and uses JUnit 5.12.2. It currently stores requests in memory only; SQLite persistence and the remaining request API routes are later project tasks. All example identities are synthetic, and `User.canAccess` demonstrates polymorphic dispatch only—it is not authentication or an API security boundary.

## Planned day-2 lead / partner split

The existing team plan assigns Tehilla Chiwama / Cheela Mulilo to Resident submission fields and validation; Bright Mwansa / Blessings Syansowa to MaintenanceStaff and status behavior; Mwenya Simpungwe / Tehilla Chiwama to RequestService creation, lookup, and exception behavior; Cheela Mulilo / Bright Mwansa to MaintenanceRequest state and controlled updates; and Blessings Syansowa / Mwenya Simpungwe to the polymorphism test. These are the plan's future lead/partner assignments, not a claim that those members authored or reviewed this scaffold commit.
