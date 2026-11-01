package vibecoders101.example.service;

import org.junit.jupiter.api.Test;
import vibecoders101.example.domain.MaintenanceRequest;
import vibecoders101.example.domain.Resident;
import vibecoders101.example.repository.InMemoryRequestRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestServiceReferenceTest {
    @Test
    void retriesDuplicateReferencesAndSavesTheNextUniqueReference() {
        var repository = new InMemoryRequestRepository();
        repository.save(new MaintenanceRequest("MM-000000000001",
                new Resident("DEMO-MM-000000000001", "Demo Resident", "Maple", "A-12"),
                "Existing", null, Instant.parse("2026-10-30T09:00:00Z")));
        var values = new String[] {"MM-000000000001", "MM-000000000002"};
        var calls = new AtomicInteger();
        var service = new RequestService(repository, Clock.fixed(Instant.parse("2026-10-30T09:15:30Z"), ZoneOffset.UTC),
                () -> values[calls.getAndIncrement()]);

        var created = service.create("Maple", "A-12", "New request");

        assertEquals("MM-000000000002", created.getReference());
        assertEquals(2, calls.get());
    }

    @Test
    void reportsAReferenceConflictAfterThreeCollisions() {
        var repository = new InMemoryRequestRepository();
        repository.save(new MaintenanceRequest("MM-000000000001",
                new Resident("DEMO-MM-000000000001", "Demo Resident", "Maple", "A-12"),
                "Existing", null, Instant.parse("2026-10-30T09:00:00Z")));
        var calls = new AtomicInteger();
        var service = new RequestService(repository, Clock.fixed(Instant.parse("2026-10-30T09:15:30Z"), ZoneOffset.UTC),
                () -> {
                    calls.incrementAndGet();
                    return "MM-000000000001";
                });

        assertThrows(ReferenceGenerationException.class, () -> service.create("Maple", "A-12", "New request"));
        assertEquals(3, calls.get());
        assertEquals(1, repository.list().size());
    }
}
