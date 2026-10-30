package vibecoders101.example.service;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(String reference) {
        super("No request found with reference: " + reference);
    }
}
