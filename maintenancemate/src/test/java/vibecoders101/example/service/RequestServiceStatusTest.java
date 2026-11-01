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
        assertEquals(Instant.parse("2026-10-29T10:10:00.001Z"), updated.getUpdatedAt());
        assertEquals(updated.getStatus(), repository.findByReference(request.getReference()).orElseThrow().getStatus());
    }

    @Test
    void invalidStatusDoesNotMutateTheRequest() {
        var repository = new InMemoryRequestRepository();
        var service = new RequestService(repository,
                Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneOffset.UTC));
        var request = service.create(new Resident("resident-1", "Demo Resident", "Maple", "A-12"), "Leaking tap");
        var createdAt = request.getUpdatedAt();

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus(request.getReference(), "Resolved"));
        assertEquals("Received", request.getStatus());
        assertEquals(createdAt, request.getUpdatedAt());
    }

    @Test
    void updatedAtAdvancesByOneMillisecondWhenClockDoesNotMove() {
        Instant sameTick = Instant.parse("2026-10-29T10:00:00.123Z");
        Clock clock = new Clock() {
            @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(java.time.ZoneId zone) { return this; }
            @Override public Instant instant() { return sameTick; }
        };
        var repository = new InMemoryRequestRepository();
        var request = new vibecoders101.example.domain.MaintenanceRequest("MM-000000000777",
                new Resident("resident-1", "Demo Resident", "Maple", "A-12"), "Leaking tap", null, sameTick);
        repository.save(request);
        var service = new RequestService(repository, clock);

        var first = service.updateStatus(request.getReference(), "In Progress");
        assertEquals(sameTick.plusMillis(1), first.getUpdatedAt());
        var second = service.updateStatus(request.getReference(), "Resolved");
        assertEquals(sameTick.plusMillis(2), second.getUpdatedAt());
    }
}
