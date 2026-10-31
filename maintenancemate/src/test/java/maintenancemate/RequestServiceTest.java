package maintenancemate;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RequestServiceTest {
    private static final String BLOCK = "Maple Hostel, Block B";

    /** Clock the test can move forward. */
    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-10-29T09:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private MutableClock clock;
    private RequestService service;

    private static Supplier<String> refs(String... values) {
        Deque<String> queue = new ArrayDeque<>(List.of(values));
        return () -> queue.isEmpty() ? "FFFFFFFFFFFF" : queue.poll();
    }

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        service = new RequestService(refs("AAAAAAAAAAAA", "BBBBBBBBBBBB", "CCCCCCCCCCCC"), clock);
    }

    // ---- create: success ----

    @Test
    void createReturnsServerManagedFields() {
        MaintenanceRequest r = service.create(BLOCK, "A-12", "The sink tap is leaking.");
        assertEquals("MM-AAAAAAAAAAAA", r.getReference());
        assertEquals("Received", r.getStatus());
        assertNull(r.getAssignedStaffId());
        assertEquals(Instant.parse("2026-10-29T09:00:00Z"), r.getCreatedAt());
        assertEquals(r.getCreatedAt(), r.getUpdatedAt());
    }

    @Test
    void createTrimsInputs() {
        MaintenanceRequest r = service.create("  " + BLOCK + " ", " A-12  ", "  Leaking tap ");
        assertEquals(BLOCK, r.getHostelBlock());
        assertEquals("A-12", r.getRoomNumber());
        assertEquals("Leaking tap", r.getDescription());
    }

    @Test
    void defaultConstructorGeneratesReferenceInAgreedFormat() {
        MaintenanceRequest r = new RequestService().create(BLOCK, "A-12", "Leaking tap");
        assertTrue(r.getReference().matches("^MM-[0-9A-F]{12}$"), r.getReference());
    }

    @Test
    void eachRequestGetsADifferentReference() {
        MaintenanceRequest a = service.create(BLOCK, "A-12", "Leaking tap");
        MaintenanceRequest b = service.create(BLOCK, "A-13", "Broken window");
        assertNotEquals(a.getReference(), b.getReference());
    }

    // ---- create: validation (custom exception THROWN) ----

    @Test
    void blankRoomNumberThrowsInvalidRequestExceptionNamingTheField() {
        InvalidRequestException e = assertThrows(InvalidRequestException.class,
                () -> service.create(BLOCK, "   ", "Leaking tap"));
        assertEquals("Must not be blank.", e.getFields().get("roomNumber"));
    }

    @Test
    void nullAndBlankFieldsAreRejected() {
        assertTrue(assertThrows(InvalidRequestException.class,
                () -> service.create(null, "A-12", "x")).getFields().containsKey("hostelBlock"));
        assertTrue(assertThrows(InvalidRequestException.class,
                () -> service.create(BLOCK, "A-12", "")).getFields().containsKey("description"));
    }

    @Test
    void overLimitFieldsAreRejected() {
        assertTrue(assertThrows(InvalidRequestException.class,
                () -> service.create("x".repeat(101), "A-12", "x")).getFields().containsKey("hostelBlock"));
        assertTrue(assertThrows(InvalidRequestException.class,
                () -> service.create(BLOCK, "x".repeat(31), "x")).getFields().containsKey("roomNumber"));
        assertTrue(assertThrows(InvalidRequestException.class,
                () -> service.create(BLOCK, "A-12", "x".repeat(1001))).getFields().containsKey("description"));
    }

    @Test
    void limitsAreInclusiveAfterTrimming() {
        assertDoesNotThrow(() -> service.create("x".repeat(100), "y".repeat(30), "z".repeat(1000)));
    }

    @Test
    void allInvalidFieldsAreReportedTogether() {
        InvalidRequestException e = assertThrows(InvalidRequestException.class,
                () -> service.create("", null, " "));
        assertEquals(3, e.getFields().size());
    }

    @Test
    void invalidSubmissionIsNotSaved() {
        assertThrows(InvalidRequestException.class, () -> service.create("", "A-12", "x"));
        assertEquals(0, service.list().size());
    }

    // ---- reference collision ----

    @Test
    void collidingReferenceIsRetried() {
        RequestService s = new RequestService(refs("AAAAAAAAAAAA", "AAAAAAAAAAAA", "BBBBBBBBBBBB"), clock);
        s.create(BLOCK, "A-12", "one");
        assertEquals("MM-BBBBBBBBBBBB", s.create(BLOCK, "A-13", "two").getReference());
    }

    @Test
    void referenceStillCollidingAfterRetriesThrowsReferenceConflictException() {
        RequestService s = new RequestService(() -> "AAAAAAAAAAAA", clock);
        s.create(BLOCK, "A-12", "one");
        assertThrows(ReferenceConflictException.class, () -> s.create(BLOCK, "A-13", "two"));
        assertEquals(1, s.list().size());
    }

    // ---- findByReference ----

    @Test
    void findByReferenceReturnsSavedRequest() {
        MaintenanceRequest created = service.create(BLOCK, "A-12", "Leaking tap");
        assertSame(created, service.findByReference("MM-AAAAAAAAAAAA"));
    }

    @Test
    void malformedReferenceThrowsInvalidRequestException() {
        for (String bad : new String[] {"mm-aaaaaaaaaaaa", "MM-123", "XX-AAAAAAAAAAAA", "", null}) {
            InvalidRequestException e = assertThrows(InvalidRequestException.class,
                    () -> service.findByReference(bad), "ref: " + bad);
            assertTrue(e.getFields().containsKey("reference"));
        }
    }

    @Test
    void wellFormedButAbsentReferenceThrowsRequestNotFoundException() {
        assertThrows(RequestNotFoundException.class, () -> service.findByReference("MM-0123456789AB"));
    }

    // ---- list ----

    @Test
    void emptyListWhenNothingCreated() {
        assertTrue(service.list().isEmpty());
    }

    @Test
    void listIsNewestFirstThenReferenceAscending() {
        service.create(BLOCK, "A-1", "first");            // MM-AAAA..., t0
        clock.now = clock.now.plusSeconds(5);
        service.create(BLOCK, "A-2", "second");           // MM-BBBB..., t0+5
        service.create(BLOCK, "A-3", "third");            // MM-CCCC..., t0+5 (tie)
        List<String> order = service.list().stream().map(MaintenanceRequest::getReference).toList();
        assertEquals(List.of("MM-BBBBBBBBBBBB", "MM-CCCCCCCCCCCC", "MM-AAAAAAAAAAAA"), order);
    }
}
