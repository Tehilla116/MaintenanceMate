package vibecoders101.example.domain;

public abstract class User {
    private final String userId;
    private final String displayName;

    protected User(String userId, String displayName) {
        this.userId = requireText(userId, "userId", 1, 64);
        this.displayName = requireText(displayName, "displayName", 1, 100);
    }

    public final String getUserId() {
        return userId;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public abstract boolean canAccess(MaintenanceRequest request);

    protected static String requireText(String value, String field, int min, int max) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new IllegalArgumentException(field + " must contain between " + min + " and " + max + " characters");
        }
        return trimmed;
    }
}
