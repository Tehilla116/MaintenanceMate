package vibecoders101.example.service;

import vibecoders101.example.domain.MaintenanceRequest;
import vibecoders101.example.domain.Resident;
import vibecoders101.example.repository.InMemoryRequestRepository;
import vibecoders101.example.repository.RequestRepository;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class RequestService {
    private final RequestRepository repository;
    private final Clock clock;

    public RequestService() {
        this(new InMemoryRequestRepository(), Clock.systemUTC());
    }

    public RequestService(Clock clock) {
        this(new InMemoryRequestRepository(), clock);
    }

    public RequestService(RequestRepository repository, Clock clock) {
        this.repository = java.util.Objects.requireNonNull(repository, "repository is required");
        this.clock = java.util.Objects.requireNonNull(clock, "clock is required");
    }

    public MaintenanceRequest create(Resident resident, String description) {
        if (resident == null) {
            throw new InvalidRequestException("resident is required");
        }
        String normalizedDescription = description == null ? null : description.trim();
        if (normalizedDescription == null || normalizedDescription.isEmpty() || normalizedDescription.length() > 1000) {
            throw new InvalidRequestException("description must contain between 1 and 1000 characters");
        }
        MaintenanceRequest request = new MaintenanceRequest(newReference(), resident, normalizedDescription, null, clock.instant());
        repository.save(request);
        return request;
    }

    public Optional<MaintenanceRequest> findByReference(String reference) {
        return repository.findByReference(reference);
    }

    public List<MaintenanceRequest> list() {
        return repository.list();
    }

    public MaintenanceRequest updateStatus(String reference, String nextStatus) {
        MaintenanceRequest request = repository.findByReference(reference)
                .orElseThrow(() -> new RequestNotFoundException(reference));
        try {
            request.advanceStatus(nextStatus, clock.instant());
        } catch (IllegalArgumentException exception) {
            throw new InvalidStatusTransitionException(exception.getMessage());
        }
        repository.save(request);
        return request;
    }

    private static String newReference() {
        return "MM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }
}
