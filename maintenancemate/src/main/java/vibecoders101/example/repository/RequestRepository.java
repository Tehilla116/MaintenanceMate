package vibecoders101.example.repository;

import vibecoders101.example.domain.MaintenanceRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository {
    void save(MaintenanceRequest request);

    Optional<MaintenanceRequest> findByReference(String reference);

    List<MaintenanceRequest> list();
}
