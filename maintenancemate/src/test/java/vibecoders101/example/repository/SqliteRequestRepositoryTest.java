package vibecoders101.example.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vibecoders101.example.domain.MaintenanceRequest;
import vibecoders101.example.domain.Resident;
import vibecoders101.example.service.StorageUnavailableException;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteRequestRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void savesAndReloadsARequestWithSyntheticResidentAndMillisecondTimestamps() {
        var repository = new SqliteRequestRepository(temporaryDirectory.resolve("data/maintenancemate.db"));
        var original = request("MM-000000000001", "Maple Hostel, Block B", "A-12", "Leaking tap",
                "2026-10-30T09:15:30.123Z", "Received", "2026-10-30T09:15:30.123Z");
        repository.save(original);

        var loaded = repository.findByReference(original.getReference()).orElseThrow();
        assertEquals(original.getReference(), loaded.getReference());
        assertEquals("Maple Hostel, Block B", loaded.getHostelBlock());
        assertEquals("A-12", loaded.getRoomNumber());
        assertEquals("Leaking tap", loaded.getDescription());
        assertEquals("Received", loaded.getStatus());
        assertNull(loaded.getAssignedStaff());
        assertEquals(original.getCreatedAt(), loaded.getCreatedAt());
        assertEquals(original.getUpdatedAt(), loaded.getUpdatedAt());
        assertEquals("DEMO-MM-000000000001", loaded.getResident().getUserId());
        assertTrue(repository.checkHealth());
    }

    @Test
    void statusUpdateIsConditionalAndSurvivesClosingAndReopeningTheRepository() {
        Path database = temporaryDirectory.resolve("requests.db");
        var created = request("MM-000000000002", "Maple", "A-12", "Leaking tap",
                "2026-10-30T09:15:30.123Z", "Received", "2026-10-30T09:15:30.123Z");
        var repository = new SqliteRequestRepository(database);
        repository.save(created);

        assertTrue(repository.updateStatus(created.getReference(), "Received", "In Progress",
                Instant.parse("2026-10-30T09:15:30.124Z")));
        assertFalse(repository.updateStatus(created.getReference(), "Received", "Resolved",
                Instant.parse("2026-10-30T09:15:30.125Z")));

        var reopened = new SqliteRequestRepository(database);
        var loaded = reopened.findByReference(created.getReference()).orElseThrow();
        assertEquals("In Progress", loaded.getStatus());
        assertEquals(Instant.parse("2026-10-30T09:15:30.124Z"), loaded.getUpdatedAt());
        assertEquals(1, reopened.list().size());
        assertEquals(loaded.getReference(), reopened.list().get(0).getReference());
    }

    @Test
    void schemaInitializationIsIdempotentAndCreatesTheExpectedTableAndIndex() throws Exception {
        Path database = temporaryDirectory.resolve("requests.db");
        new SqliteRequestRepository(database);
        new SqliteRequestRepository(database);

        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             var statement = connection.createStatement();
             var results = statement.executeQuery("SELECT name FROM sqlite_master WHERE type IN ('table', 'index')")) {
            var names = new java.util.HashSet<String>();
            while (results.next()) names.add(results.getString("name"));
            assertTrue(names.contains("maintenance_requests"));
            assertTrue(names.contains("idx_maintenance_requests_created_at"));
        }
    }

    @Test
    void duplicateReferenceIsReportedWithoutReplacingTheExistingRow() {
        var repository = new SqliteRequestRepository(temporaryDirectory.resolve("requests.db"));
        var original = request("MM-000000000003", "Maple", "A-12", "Leaking tap",
                "2026-10-30T09:15:30.123Z", "Received", "2026-10-30T09:15:30.123Z");
        repository.save(original);

        assertThrows(DuplicateReferenceException.class, () -> repository.save(request(
                original.getReference(), "Oak", "B-9", "Broken light",
                "2026-10-30T09:16:30.123Z", "Received", "2026-10-30T09:16:30.123Z")));
        assertEquals("Leaking tap", repository.findByReference(original.getReference()).orElseThrow().getDescription());
    }

    @Test
    void nonPrimaryKeyConstraintFailureMapsToStorageUnavailable() throws Exception {
        Path database = temporaryDirectory.resolve("requests.db");
        var repository = new SqliteRequestRepository(database);
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
             var statement = connection.createStatement()) {
            statement.execute("CREATE TRIGGER reject_request BEFORE INSERT ON maintenance_requests "
                    + "BEGIN SELECT RAISE(ABORT, 'intentional failure'); END");
        }
        assertThrows(StorageUnavailableException.class, () -> repository.save(request("MM-000000000004",
                "Maple", "A-12", "Leaking tap", "2026-10-30T09:15:30.123Z", "Received",
                "2026-10-30T09:15:30.123Z")));
    }

    private static MaintenanceRequest request(String reference, String hostelBlock, String roomNumber,
                                             String description, String createdAt, String status, String updatedAt) {
        return MaintenanceRequest.restore(reference,
                new Resident("DEMO-" + reference, "Demo Resident", hostelBlock, roomNumber), description,
                null, status, Instant.parse(createdAt), Instant.parse(updatedAt));
    }
}
