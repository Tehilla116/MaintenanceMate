package maintenancemate;

/**
 * Thrown when a generated reference still collides after bounded retries.
 * Mapped to 409 REFERENCE_CONFLICT. (Class name proposed: contract names the condition, not the class.)
 */
public class ReferenceConflictException extends RuntimeException {
    public ReferenceConflictException(String message) {
        super(message);
    }
}
