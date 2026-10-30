package vibecoders101.example.service;

import org.junit.jupiter.api.Test;
import vibecoders101.example.domain.Resident;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestServiceTest {
    private final RequestService service = new RequestService(
            Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneOffset.UTC));
    private final Resident resident = new Resident("resident-1", "Demo Resident", "Maple", "A-12");

    @Test
    void createNormalizesDescriptionAndStoresRequestInReferenceCollection() {
        var request = service.create(resident, "  Leaking tap  ");

        assertEquals("Leaking tap", request.getDescription());
        assertTrue(request.getReference().matches("MM-[0-9A-F]{12}"));
        assertEquals(request, service.findByReference(request.getReference()).orElseThrow());
        assertEquals(1, service.list().size());
    }

    @Test
    void createThrowsCustomExceptionForInvalidDescriptionWithoutStoringRequest() {
        assertThrows(InvalidRequestException.class, () -> service.create(resident, "  "));
        assertThrows(InvalidRequestException.class, () -> service.create(resident, "x".repeat(1001)));
        assertTrue(service.list().isEmpty());
    }

    @Test
    void createThrowsCustomExceptionWhenResidentIsMissing() {
        assertThrows(InvalidRequestException.class, () -> service.create(null, "Leaking tap"));
    }
}
