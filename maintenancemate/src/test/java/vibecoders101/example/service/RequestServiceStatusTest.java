package vibecoders101.example.service;

import org.junit.jupiter.api.Test;
import vibecoders101.example.domain.Resident;
import vibecoders101.example.repository.InMemoryRequestRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestServiceStatusTest {
    @Test
    void updateStatusAdvancesARequestStoredBehindTheRepositoryBoundary() {
        var repository = new InMemoryRequestRepository();
        var service = new RequestService(repository,
                Clock.fixed(Instant.parse("2026-10-29T10:10:00Z"), ZoneOffset.UTC));
        var resident = new Resident("resident-1", "Demo Resident", "Maple", "A-12");
        var request = service.create(resident, "Leaking tap");

        var updated = service.updateStatus(request.getReference(), "In Progress");

        assertEquals("In Progress", updated.getStatus());
        assertEquals(Instant.parse("2026-10-29T10:10:00Z"), updated.getUpdatedAt());
        assertEquals(updated, repository.findByReference(request.getReference()).orElseThrow());
    }

    @Test
    void invalidStatusDoesNotMutateTheRequest() {
        var repository = new InMemoryRequestRepository();
        var service = new RequestService(repository,
                Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneOffset.UTC));
        var resident = new Resident("resident-1", "Demo Resident", "Maple", "A-12");
        var request = service.create(resident, "Leaking tap");
        var createdAt = request.getUpdatedAt();

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus(request.getReference(), "Resolved"));
        assertEquals("Received", request.getStatus());
        assertEquals(createdAt, request.getUpdatedAt());
    }
}
