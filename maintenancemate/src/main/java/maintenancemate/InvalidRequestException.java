package maintenancemate;

import java.util.Map;

/**
 * Thrown by RequestService for invalid input (body/path/field/status value).
 * HTTP layer maps it to 400 VALIDATION_ERROR using getFields() for details.fields.
 */
public class InvalidRequestException extends RuntimeException {
    private final Map<String, String> fields;

    public InvalidRequestException(String message, Map<String, String> fields) {
        super(message);
        this.fields = Map.copyOf(fields);
    }

    /** Field name -> short, safe message. Empty when no field details apply. */
    public Map<String, String> getFields() { return fields; }
}
