# Maintenance Mate demo video script

**Target length:** about 2 minutes 35 seconds, including clicks and pauses  
**Maximum:** 3 minutes  
**Speakers:** replace `[Member 1]` through `[Member 5]` with your presenters' names.

## Before recording

- Start the app and open `http://localhost:7070/` in a browser.
- Play [the Classic grid animation](demo-video-intro-classic.mp4) during the opening narration, hold its final frame until 0:25, then cut to the browser.
- Use synthetic demo details, for example: `Maple Hostel, Block B`, `A-12`, and `The sink tap is leaking.`
- Keep the browser and source code ready so the demo can move quickly.
- Do not include real residents' personal information.

## Script

### 0:00–0:25 — The problem | Member 1

**Show:** Play [the Classic grid animation](demo-video-intro-classic.mp4) through three problem panels and the solution panel, then hold on the full grid during the rest of the narration. Cut to the browser at 0:25.

> In shared residences, a leaking tap is only the start. Repairs are often reported through informal messages, which can go unanswered. Requests can also be forgotten or entered twice, leaving residents unsure whether anything is being done. Maintenance Mate gives each repair a reference and a status residents can check.

### 0:25–0:55 — Submit a request | Member 2

**Show:** Enter the sample hostel, room, and repair description, then submit the form. Pause briefly on the returned reference and `Received` status.

> I’ll submit a sample repair request. I enter the hostel block, room number, and a short description, then submit. Maintenance Mate checks the details, creates a unique reference, and records the request as Received. The Java backend uses Javalin, and the request is saved in SQLite.

### 0:55–1:20 — Update the status | Member 3

**Show:** Load the request list and click **Advance to In Progress**. Pause so the changed status is visible.

> In the demo staff view, I can load the saved requests and see their current status. I’ll move this request to In Progress. When the work is finished, the next allowed status is Resolved. The request moves through these stages in order.

### 1:20–1:45 — Check progress | Member 4

**Show:** Use the reference already filled into the lookup form and submit it. Point to the returned status.

> A resident can use the reference number to check the latest status. Here, the lookup returns In Progress, so the resident can see that the request has been recorded and work has started. The browser page sends these actions to our Javalin API.

### 1:45–2:35 — OOP example and close | Member 5

**Show:** Briefly show `User.java`, `Resident.java`, and `MaintenanceStaff.java`, highlighting their `extends User` declarations.

> One OOP idea we use is inheritance. User is the parent class and holds shared details like a user ID and display name. Resident and MaintenanceStaff extend User, so they inherit those common details. Resident also adds a hostel block and room number. This keeps shared user information in one place while the child class represent different kinds of users.
>
> Maintenance Mate gives residents and maintenance teams one shared record to follow. Thank you.

## Recording check

- Keep the finished video under 3 minutes.
- Make sure the problem is explained in the first 30 seconds.
- Show the running app and complete the submit, status update, and lookup actions.
- Keep the source-code explanation brief and readable on screen.
- Upload the video to YouTube as **Unlisted** and check that the link opens.
