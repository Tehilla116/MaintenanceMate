package vibecoders101.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserPolymorphismTest {
    @Test
    void sameCanAccessCallDispatchesByUserRoleAndRequestAssignment() {
        var resident = new Resident("resident-1", "Demo Resident", "Maple", "A-12");
        var otherResident = new Resident("resident-2", "Other Resident", "Pine", "B-4");
        var assignedStaff = new MaintenanceStaff("staff-1", "Demo Staff");
        var otherStaff = new MaintenanceStaff("staff-2", "Other Staff");
        var request = new MaintenanceRequest("MM-000000000001", resident, "Leaking tap", assignedStaff,
                java.time.Instant.parse("2026-10-29T10:00:00Z"));
        User residentView = resident;
        User staffView = assignedStaff;
        User unassignedStaffView = otherStaff;

        assertTrue(residentView.canAccess(request));
        assertFalse(otherResident.canAccess(request));
        assertTrue(staffView.canAccess(request));
        assertFalse(unassignedStaffView.canAccess(request));
    }
}
