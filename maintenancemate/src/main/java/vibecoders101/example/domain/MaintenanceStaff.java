package vibecoders101.example.domain;

public class MaintenanceStaff extends User {
    public MaintenanceStaff(String userId, String displayName) {
        super(userId, displayName);
    }

    @Override
    public boolean canAccess(MaintenanceRequest request) {
        return request != null && request.getAssignedStaff() != null
                && request.getAssignedStaff().getUserId().equals(getUserId());
    }
}
