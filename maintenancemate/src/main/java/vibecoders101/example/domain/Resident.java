package vibecoders101.example.domain;

public class Resident extends User {
    private final String hostelBlock;
    private final String roomNumber;

    public Resident(String userId, String displayName, String hostelBlock, String roomNumber) {
        super(userId, displayName);
        this.hostelBlock = requireText(hostelBlock, "hostelBlock", 1, 100);
        this.roomNumber = requireText(roomNumber, "roomNumber", 1, 30);
    }

    public String getHostelBlock() {
        return hostelBlock;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    @Override
    public boolean canAccess(MaintenanceRequest request) {
        return request != null && request.getResident().getUserId().equals(getUserId());
    }
}
