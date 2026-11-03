package vibecoders101.example.service;

public class ReferenceGenerationException extends RuntimeException {
    public ReferenceGenerationException(Throwable cause) {
        super("Could not generate a unique request reference", cause);
    }
}
