package vibecoders101.example.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

public final class MaintenanceRequest {
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("MM-[0-9A-F]{12}");
    private static final String[] STATUSES = {"Received", "In Progress", "Resolved"};

    private final String reference;
    private final Resident resident;
    private final String description;
    private final MaintenanceStaff assignedStaff;
    private final Instant createdAt;
    private String status;
    private Instant updatedAt;

    public MaintenanceRequest(String reference, Resident resident, String description,
                              MaintenanceStaff assignedStaff, Instant createdAt) {
        if (reference == null || !REFERENCE_PATTERN.matcher(reference).matches()) {
            throw new IllegalArgumentException("reference must use MM- followed by 12 uppercase hexadecimal characters");
        }
        this.reference = reference;
        this.resident = Objects.requireNonNull(resident, "resident is required");
        this.description = User.requireText(description, "description", 1, 1000);
        this.assignedStaff = assignedStaff;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.status = "Received";
        this.updatedAt = createdAt;
    }

    public String getReference() {
        return reference;
    }

    public Resident getResident() {
        return resident;
    }

    public String getHostelBlock() {
        return resident.getHostelBlock();
    }

    public String getRoomNumber() {
        return resident.getRoomNumber();
    }

    public String getDescription() {
        return description;
    }

    public MaintenanceStaff getAssignedStaff() {
        return assignedStaff;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void advanceStatus(String nextStatus, Instant now) {
        int currentIndex = indexOf(status);
        if (indexOf(nextStatus) != currentIndex + 1) {
            throw new IllegalArgumentException("status must advance one step from Received to In Progress to Resolved");
        }
        Instant nextUpdatedAt = Objects.requireNonNull(now, "now is required");
        if (nextUpdatedAt.isBefore(updatedAt)) {
            throw new IllegalArgumentException("updatedAt cannot move backwards");
        }
        status = nextStatus;
        updatedAt = nextUpdatedAt;
    }

    private static int indexOf(String value) {
        for (int index = 0; index < STATUSES.length; index++) {
            if (STATUSES[index].equals(value)) {
                return index;
            }
        }
        return -1;
    }
}
