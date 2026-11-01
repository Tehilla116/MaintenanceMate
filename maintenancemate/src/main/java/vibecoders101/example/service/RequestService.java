package vibecoders101.example.service;

import vibecoders101.example.domain.MaintenanceRequest;
import vibecoders101.example.domain.Resident;
import vibecoders101.example.repository.DuplicateReferenceException;
import vibecoders101.example.repository.InMemoryRequestRepository;
import vibecoders101.example.repository.RequestRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class RequestService {
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("MM-[0-9A-F]{12}");
    private static final String[] STATUSES = {"Received", "In Progress", "Resolved"};
    private final RequestRepository repository;
    private final Clock clock;
    private final java.util.function.Supplier<String> referenceGenerator;

    public RequestService() { this(new InMemoryRequestRepository(), Clock.systemUTC()); }
    public RequestService(Clock clock) { this(new InMemoryRequestRepository(), clock); }

    public RequestService(RequestRepository repository, Clock clock) {
        this(repository, clock, RequestService::newReference);
    }

    public RequestService(RequestRepository repository, Clock clock, java.util.function.Supplier<String> referenceGenerator) {
        this.repository = java.util.Objects.requireNonNull(repository, "repository is required");
        this.clock = java.util.Objects.requireNonNull(clock, "clock is required");
        this.referenceGenerator = java.util.Objects.requireNonNull(referenceGenerator, "referenceGenerator is required");
    }

    public MaintenanceRequest create(String hostelBlock, String roomNumber, String description) {
        String normalizedHostel = validateField(hostelBlock, "hostelBlock", 100);
        String normalizedRoom = validateField(roomNumber, "roomNumber", 30);
        String normalizedDescription = validateField(description, "description", 1000);
        for (int attempt = 0; attempt < 3; attempt++) {
            String reference = referenceGenerator.get();
            Resident resident = new Resident("DEMO-" + reference, "Demo Resident", normalizedHostel, normalizedRoom);
            MaintenanceRequest request = new MaintenanceRequest(reference, resident, normalizedDescription, null,
                    millisecond(clock.instant()));
            try {
                repository.save(request);
                return request;
            } catch (DuplicateReferenceException collision) {
                if (attempt == 2) throw new ReferenceGenerationException(collision);
            }
        }
        throw new ReferenceGenerationException(null);
    }

    public MaintenanceRequest create(Resident resident, String description) {
        if (resident == null) throw new InvalidRequestException("resident is required");
        return create(resident.getHostelBlock(), resident.getRoomNumber(), description);
    }

    public Optional<MaintenanceRequest> findByReference(String reference) {
        validateReference(reference);
        return repository.findByReference(reference);
    }

    public MaintenanceRequest requireByReference(String reference) {
        return findByReference(reference).orElseThrow(() -> new RequestNotFoundException(reference));
    }

    public List<MaintenanceRequest> list() { return repository.list(); }

    public MaintenanceRequest updateStatus(String reference, String nextStatus) {
        validateReference(reference);
        if (indexOf(nextStatus) < 0) throw new InvalidRequestException("status", "Must be one of Received, In Progress, or Resolved.");
        MaintenanceRequest current = repository.findByReference(reference)
                .orElseThrow(() -> new RequestNotFoundException(reference));
        int currentIndex = indexOf(current.getStatus());
        if (indexOf(nextStatus) != currentIndex + 1) {
            throw new InvalidStatusTransitionException("status must advance one step from Received to In Progress to Resolved");
        }
        Instant updatedAt = millisecond(clock.instant());
        Instant earliest = current.getUpdatedAt().plus(1, ChronoUnit.MILLIS);
        if (updatedAt.isBefore(earliest)) updatedAt = earliest;
        if (!repository.updateStatus(reference, current.getStatus(), nextStatus, updatedAt)) {
            if (repository.findByReference(reference).isEmpty()) throw new RequestNotFoundException(reference);
            throw new InvalidStatusTransitionException("request status changed before this update could be applied");
        }
        return repository.findByReference(reference).orElseThrow(() -> new RequestNotFoundException(reference));
    }

    public void checkStorageHealth() {
        if (!repository.checkHealth()) throw new StorageUnavailableException("SQLite storage is unavailable", null);
    }

    private static String validateField(String value, String field, int max) {
        if (value == null) throw new InvalidRequestException(field, "Must not be blank.");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new InvalidRequestException(field, "Must not be blank.");
        if (trimmed.length() > max) throw new InvalidRequestException(field, "Must contain at most " + max + " characters.");
        return trimmed;
    }

    private static void validateReference(String reference) {
        if (reference == null || !REFERENCE_PATTERN.matcher(reference).matches()) {
            throw new InvalidRequestException("reference must use MM- followed by 12 uppercase hexadecimal characters");
        }
    }

    private static Instant millisecond(Instant instant) { return instant.truncatedTo(ChronoUnit.MILLIS); }

    private static String newReference() {
        return "MM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static int indexOf(String value) {
        if (value == null) return -1;
        for (int index = 0; index < STATUSES.length; index++) if (STATUSES[index].equals(value)) return index;
        return -1;
    }
}
