package vibecoders101.example.service;

import java.util.Map;

public class InvalidRequestException extends RuntimeException {
    private final Map<String, Object> details;

    public InvalidRequestException(String message) {
        super(message);
        this.details = Map.of();
    }

    public InvalidRequestException(String field, String message) {
        super(message);
        this.details = Map.of("fields", Map.of(field, message));
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
