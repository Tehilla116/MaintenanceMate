package vibecoders101.example.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaintenanceRequestTest {
    private final Resident resident = new Resident("resident-1", "Demo Resident", "Maple", "A-12");
    private final Instant createdAt = Instant.parse("2026-10-29T10:00:00Z");

    @Test
    void requestStartsReceivedWithTrimmedDescriptionAndServerTimestamps() {
        var request = new MaintenanceRequest("MM-000000000001", resident, "  Leaking tap  ", null, createdAt);

        assertEquals("Leaking tap", request.getDescription());
        assertEquals("Received", request.getStatus());
        assertEquals(createdAt, request.getCreatedAt());
        assertEquals(createdAt, request.getUpdatedAt());
        assertNull(request.getAssignedStaff());
    }

    @Test
    void requestRejectsBlankAndOverlongDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new MaintenanceRequest("MM-000000000001", resident, "  ", null, createdAt));
        assertThrows(IllegalArgumentException.class,
                () -> new MaintenanceRequest("MM-000000000001", resident, "x".repeat(1001), null, createdAt));
    }

    @Test
    void statusCanAdvanceOnlyAlongTheForwardWorkflow() {
        var request = new MaintenanceRequest("MM-000000000001", resident, "Leaking tap", null, createdAt);
        var progressedAt = createdAt.plusSeconds(30);

        assertThrows(IllegalArgumentException.class,
                () -> request.advanceStatus("Resolved", progressedAt));
        request.advanceStatus("In Progress", progressedAt);

        assertEquals("In Progress", request.getStatus());
        assertEquals(createdAt, request.getCreatedAt());
        assertEquals(progressedAt, request.getUpdatedAt());
        request.advanceStatus("Resolved", progressedAt.plusSeconds(30));
        assertEquals("Resolved", request.getStatus());
        assertThrows(IllegalArgumentException.class,
                () -> request.advanceStatus("Resolved", progressedAt.plusSeconds(60)));
    }
}
