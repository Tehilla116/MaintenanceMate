package maintenancemate;

/** PLACEHOLDER: owned by Tehilla. Replace with the team's version. */
public class Resident extends User {
    private final String hostelBlock;
    private final String roomNumber;

    public Resident(String id, String name, String hostelBlock, String roomNumber) {
        super(id, name);
        this.hostelBlock = hostelBlock;
        this.roomNumber = roomNumber;
    }

    public String getHostelBlock() { return hostelBlock; }
    public String getRoomNumber() { return roomNumber; }
}
