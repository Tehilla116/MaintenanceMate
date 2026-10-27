# Hostel Maintenance Request Tracker
## Object-Oriented Programming / Group Project
### Part 1 Proposal — Java + Javalin backend with a simple web front end

### The problem
Residents of Zambia University College Of Technology often report repairs informally, in person or by message. Requests get forgotten or reported twice, and residents cannot tell whether a request was received, who is handling it, or whether it has been fixed. This tracker gives residents and maintenance staff one shared record for every repair request.

### Intended users
- **Residents:** Submit a repair with location and description; check progress by reference number.
- **Maintenance staff:** View incoming requests, take responsibility for work, and update progress.

### Proposed workflow and scope
A resident submits a request in a web form. The Java/Javalin backend validates and saves it, then returns a reference number. Maintenance staff view open requests and update status: **Received → In Progress → Resolved**. Residents check the latest status by reference number. Version one covers request recording, status tracking, and simple persistence (file or SQLite); it excludes emergency dispatch, chat, notifications, and facilities-management integrations.

### Main classes (6)
| Class | Responsibility |
|---|---|
| `User` | Base class for shared identity and contact details; defines the common user role. |
| `Resident` | A `User` with a room number and hostel block who submits repair requests and checks them by reference number. |
| `MaintenanceStaff` | A `User` who views incoming requests and updates work status. |
| `MaintenanceRequest` | Stores reference, location, description, resident, optional assigned staff, status, and timestamps. |
| `RequestService` | Validates submissions, coordinates assignment and updates, and enforces valid status changes. |
| `RequestRepository` | Saves, finds, and loads requests using one simple persistent store (file or SQLite). |

**Relationship:** Resident and MaintenanceStaff inherit shared identity from User. Both interact with requests through RequestService, which uses RequestRepository for persistence.

### Team (5 students required)
1. Tehilla Chiwama — 2410014
2. Bright Mwansa — 2410010
3. Mwenya Simpungwe — 2410013
4. Cheela Mulilo — 2410032
5. **PENDING — fifth student's name and student number to be added (not yet provided).**
