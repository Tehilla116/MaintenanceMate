package vibecoders101.example.repository;

import vibecoders101.example.domain.MaintenanceRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRequestRepository implements RequestRepository {
    private final Map<String, MaintenanceRequest> requestsByReference = new ConcurrentHashMap<>();

    @Override
    public void save(MaintenanceRequest request) {
        if (requestsByReference.putIfAbsent(request.getReference(), request) != null) {
            throw new DuplicateReferenceException(request.getReference(), null);
        }
    }

    @Override
    public Optional<MaintenanceRequest> findByReference(String reference) {
        return Optional.ofNullable(requestsByReference.get(reference));
    }

    @Override
    public List<MaintenanceRequest> list() {
        return requestsByReference.values().stream()
                .sorted(java.util.Comparator.comparing(MaintenanceRequest::getCreatedAt)
                        .reversed().thenComparing(MaintenanceRequest::getReference))
                .toList();
    }

    @Override
    public synchronized boolean updateStatus(String reference, String expectedCurrentStatus,
                                             String newStatus, Instant updatedAt) {
        MaintenanceRequest current = requestsByReference.get(reference);
        if (current == null || !current.getStatus().equals(expectedCurrentStatus)) return false;
        current.advanceStatus(newStatus, updatedAt);
        return true;
    }

    @Override
    public boolean checkHealth() {
        return true;
    }
}
