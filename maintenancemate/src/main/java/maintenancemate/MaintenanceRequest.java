package maintenancemate;

import java.time.Instant;

/** PLACEHOLDER: owned by Cheela. Replace with the team's version. */
public class MaintenanceRequest {
    private final String reference;
    private final Resident resident;
    private final String description;
    private String status = "Received";
    private String assignedStaffId = null;
    private final Instant createdAt;
    private Instant updatedAt;

    public MaintenanceRequest(String reference, Resident resident, String description, Instant createdAt) {
        this.reference = reference;
        this.resident = resident;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getReference() { return reference; }
    public Resident getResident() { return resident; }
    public String getHostelBlock() { return resident.getHostelBlock(); }
    public String getRoomNumber() { return resident.getRoomNumber(); }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getAssignedStaffId() { return assignedStaffId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
