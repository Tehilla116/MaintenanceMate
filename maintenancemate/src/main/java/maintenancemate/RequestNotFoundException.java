package maintenancemate;

/** Thrown when a well-formed reference matches no request. Mapped to 404 REQUEST_NOT_FOUND. */
public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(String message) {
        super(message);
    }
}
