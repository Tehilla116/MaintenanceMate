package vibecoders101.example.repository;

import org.junit.jupiter.api.Test;
import vibecoders101.example.domain.MaintenanceRequest;
import vibecoders101.example.domain.Resident;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRequestRepositoryTest {
    @Test
    void savesAndFindsRequestsByReferenceAndReturnsAllRows() {
        var repository = new InMemoryRequestRepository();
        var resident = new Resident("resident-1", "Demo Resident", "Maple", "A-12");
        var request = new MaintenanceRequest("MM-000000000001", resident, "Leaking tap", null,
                Instant.parse("2026-10-29T10:00:00Z"));

        repository.save(request);

        assertEquals(request, repository.findByReference(request.getReference()).orElseThrow());
        assertEquals(1, repository.list().size());
        assertTrue(repository.findByReference("MM-000000000002").isEmpty());
    }
}
