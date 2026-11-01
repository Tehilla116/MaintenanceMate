package vibecoders101.example.repository;

public class DuplicateReferenceException extends RuntimeException {
    public DuplicateReferenceException(String reference, Throwable cause) {
        super("Request reference already exists", cause);
    }
}
