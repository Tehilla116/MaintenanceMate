package vibecoders101.example.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @Test
    void statusUpdateRejectsEarlierTimestampAndLeavesRequestUnchanged() {
    var request = new MaintenanceRequest("MM-000000000001", resident, "Leaking tap", null, createdAt);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> request.advanceStatus("In Progress", createdAt.minusSeconds(1)));

    assertEquals("Received", request.getStatus());
    assertEquals(createdAt, request.getUpdatedAt());
    }

    @Test
    void requestRejectsMalformedReferences() {
        assertThrows(IllegalArgumentException.class,
                () -> new MaintenanceRequest(null, resident, "Leaking tap", null, createdAt));
        assertThrows(IllegalArgumentException.class,
                () -> new MaintenanceRequest("MM-123", resident, "Leaking tap", null, createdAt));
        assertThrows(IllegalArgumentException.class,
                () -> new MaintenanceRequest("mm-000000000001", resident, "Leaking tap", null, createdAt));
        assertThrows(IllegalArgumentException.class,
                () -> new MaintenanceRequest("MM-00000000000G", resident, "Leaking tap", null, createdAt));
    }

    @Test
    void requestRejectsMissingResidentAndCreatedAt() {
        assertThrows(NullPointerException.class,
                () -> new MaintenanceRequest("MM-000000000001", null, "Leaking tap", null, createdAt));
        assertThrows(NullPointerException.class,
                () -> new MaintenanceRequest("MM-000000000001", resident, "Leaking tap", null, null));
    }

    @Test
    void requestExposesResidentLocationAndAssignedStaff() {
        var staff = new MaintenanceStaff("staff-1", "Demo Staff");
        var request = new MaintenanceRequest("MM-000000000001", resident, "Leaking tap", staff, createdAt);

        assertEquals("MM-000000000001", request.getReference());
        assertEquals("Maple", request.getHostelBlock());
        assertEquals("A-12", request.getRoomNumber());
        assertEquals(staff, request.getAssignedStaff());
    }

    @Test
    void statusRejectsUnknownOrMissingStatusAndLeavesRequestUnchanged() {
        var request = new MaintenanceRequest("MM-000000000001", resident, "Leaking tap", null, createdAt);

        assertThrows(IllegalArgumentException.class,
                () -> request.advanceStatus(null, createdAt.plusSeconds(1)));
        assertThrows(IllegalArgumentException.class,
                () -> request.advanceStatus("Done", createdAt.plusSeconds(1)));

        assertEquals("Received", request.getStatus());
        assertEquals(createdAt, request.getUpdatedAt());
    }

    @Test
    void statusRejectsMissingTimestampAndLeavesRequestUnchanged() {
        var request = new MaintenanceRequest("MM-000000000001", resident, "Leaking tap", null, createdAt);

        assertThrows(NullPointerException.class,
                () -> request.advanceStatus("In Progress", null));

        assertEquals("Received", request.getStatus());
        assertEquals(createdAt, request.getUpdatedAt());
    }

    @Test
    void requestStateIsPrivateAndIdentityFieldsAreFinal() {
        assertFalse(Arrays.stream(MaintenanceRequest.class.getMethods())
                .anyMatch(method -> method.getName().startsWith("set")));
        assertTrue(Arrays.stream(MaintenanceRequest.class.getDeclaredFields())
                .allMatch(field -> Modifier.isPrivate(field.getModifiers())));
        assertTrue(Arrays.stream(MaintenanceRequest.class.getDeclaredFields())
                .filter(field -> !field.getName().equals("status"))
                .filter(field -> !field.getName().equals("updatedAt"))
                .allMatch(field -> Modifier.isFinal(field.getModifiers())));
    }
}
