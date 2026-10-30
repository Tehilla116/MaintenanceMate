package vibecoders101.example.repository;

import vibecoders101.example.domain.MaintenanceRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRequestRepository implements RequestRepository {
    private final Map<String, MaintenanceRequest> requestsByReference = new ConcurrentHashMap<>();

    @Override
    public void save(MaintenanceRequest request) {
        requestsByReference.put(request.getReference(), request);
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
}
