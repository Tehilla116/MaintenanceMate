package vibecoders101.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vibecoders101.example.repository.SqliteRequestRepository;
import vibecoders101.example.service.RequestService;
import vibecoders101.example.service.StorageUnavailableException;

import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestApiTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    @TempDir
    Path temporaryDirectory;

    @Test
    void emptyListUsesTheContractEnvelope() {
        JavalinTest.test(app(), (server, client) -> {
            var response = client.get("/api/requests");
            assertEquals(200, response.code());
            JsonNode body = readJson(response.body().string());
            assertEquals(0, body.path("count").asInt());
            assertTrue(body.path("requests").isArray());
            assertEquals(0, body.path("requests").size());
        });
    }

    @Test
    void createdRequestRemainsAvailableAfterApplicationAndRepositoryRestart() {
        Path database = temporaryDirectory.resolve("restart.db");
        var firstApp = Main.createApp(new RequestService(new SqliteRequestRepository(database), fixedClock()));
        var reference = new java.util.concurrent.atomic.AtomicReference<String>();
        JavalinTest.test(firstApp, (server, client) -> {
            var created = post(client, "/api/requests", requestJson());
            assertEquals(201, created.code());
            reference.set(extractReference(created.body().string()));
        });

        var restartedApp = Main.createApp(new RequestService(new SqliteRequestRepository(database), fixedClock()));
        JavalinTest.test(restartedApp, (server, client) -> {
            var response = client.get("/api/requests/" + reference.get());
            assertEquals(200, response.code());
            JsonNode body = readJson(response.body().string());
            assertEquals(reference.get(), body.path("reference").asText());
            assertEquals("Received", body.path("status").asText());
            assertEquals("2026-10-30T09:15:30.123Z", body.path("createdAt").asText());
            assertEquals("Leaking tap", body.path("description").asText());
        });
    }

    @Test
    void createReturnsTheCompletePersistedRequestRepresentation() {
        JavalinTest.test(app(), (server, client) -> {
            var response = post(client, "/api/requests", requestJson());

            assertEquals(201, response.code());
            assertEquals("application/json", response.headers().get("Content-Type").get(0).split(";")[0]);
            JsonNode body = readJson(response.body().string());
            assertTrue(body.path("reference").asText().matches("MM-[0-9A-F]{12}"));
            assertEquals("Maple Hostel, Block B", body.path("hostelBlock").asText());
            assertEquals("A-12", body.path("roomNumber").asText());
            assertEquals("Leaking tap", body.path("description").asText());
            assertEquals("Received", body.path("status").asText());
            assertTrue(body.path("assignedStaffId").isNull());
            assertEquals("2026-10-30T09:15:30.123Z", body.path("createdAt").asText());
            assertEquals("2026-10-30T09:15:30.123Z", body.path("updatedAt").asText());
        });
    }

    @Test
    void listFindAndStatusRoutesUseTheSqliteRepository() {
        JavalinTest.test(app(), (server, client) -> {
            var created = post(client, "/api/requests", requestJson());
            String reference = extractReference(created.body().string());

            var list = client.get("/api/requests");
            assertEquals(200, list.code());
            assertTrue(list.body().string().contains("\"count\":1"));

            var found = client.get("/api/requests/" + reference);
            assertEquals(200, found.code());
            assertTrue(found.body().string().contains("\"reference\":\"" + reference + "\""));

            var updated = put(client, "/api/requests/" + reference + "/status", "{\"status\":\"In Progress\"}");
            assertEquals(200, updated.code());
            assertTrue(updated.body().string().contains("\"status\":\"In Progress\""));
            assertTrue(updated.body().string().contains("\"updatedAt\":\"2026-10-30T09:15:30.124Z\""));

            var repeated = put(client, "/api/requests/" + reference + "/status", "{\"status\":\"In Progress\"}");
            assertError(repeated, 409, "STATUS_TRANSITION_CONFLICT");

            var resolved = put(client, "/api/requests/" + reference + "/status", "{\"status\":\"Resolved\"}");
            assertEquals(200, resolved.code());
            assertTrue(resolved.body().string().contains("\"updatedAt\":\"2026-10-30T09:15:30.125Z\""));
        });
    }

    @Test
    void malformedAndInvalidRequestBodiesAndReferencesUseContractErrors() {
        JavalinTest.test(app(), (server, client) -> {
            assertError(post(client, "/api/requests", "{"), 400, "MALFORMED_JSON");
            assertError(post(client, "/api/requests", ""), 400, "MALFORMED_JSON");
            assertError(post(client, "/api/requests", requestJson() + " garbage"), 400, "MALFORMED_JSON");
            assertError(post(client, "/api/requests", "null"), 400, "VALIDATION_ERROR");
            assertError(post(client, "/api/requests", "[]"), 400, "VALIDATION_ERROR");
            var validation = post(client, "/api/requests", "{\"hostelBlock\":\" \",\"roomNumber\":\"A-1\",\"description\":\"x\"}");
            assertError(validation, 400, "VALIDATION_ERROR");
            assertErrorDetails(validation, "hostelBlock", "Must not be blank.");
            assertError(post(client, "/api/requests", "{\"hostelBlock\":null,\"roomNumber\":\"A-1\",\"description\":\"x\"}"),
                    400, "VALIDATION_ERROR");
            assertError(post(client, "/api/requests", "{\"hostelBlock\":\"Maple\",\"roomNumber\":\"A-1\","
                    + "\"description\":\"x\",\"status\":\"Resolved\"}"), 400, "VALIDATION_ERROR");
            var valid = post(client, "/api/requests", "{\"hostelBlock\":\"Maple\",\"roomNumber\":\"A-1\",\"description\":\"x\"}");
            assertEquals(201, valid.code());
            assertError(client.get("/api/requests/mm-123"), 400, "VALIDATION_ERROR");
            assertError(client.get("/api/requests/MM-000000000099"), 404, "REQUEST_NOT_FOUND");
        });
    }

    @Test
    void statusBodyDistinguishesUnknownValueAndDisallowedTransition() {
        JavalinTest.test(app(), (server, client) -> {
            String reference = extractReference(post(client, "/api/requests", requestJson()).body().string());
            assertErrorDetails(put(client, "/api/requests/" + reference + "/status", "{\"status\":\"unknown\"}"),
                    400, "VALIDATION_ERROR", "status", "Must be one of Received, In Progress, or Resolved.");
            var missingStatus = put(client, "/api/requests/" + reference + "/status", "{}");
            assertErrorDetails(missingStatus, 400, "VALIDATION_ERROR", "status", "This field is required.");
            assertError(put(client, "/api/requests/" + reference + "/status", "{\"status\":\"Resolved\"}"),
                    409, "STATUS_TRANSITION_CONFLICT");
            assertError(put(client, "/api/requests/MM-000000000099/status", "{\"status\":\"In Progress\"}"),
                    404, "REQUEST_NOT_FOUND");
        });
    }

    @Test
    void healthIsBackedByTheRepositoryAndErrorEnvelopeNeverLeaksStorageDetails() {
        var brokenRepository = new SqliteRequestRepository(temporaryDirectory.resolve("health.db")) {
            @Override
            public boolean checkHealth() {
                throw new StorageUnavailableException("secret path /tmp/private.db", null);
            }
        };
        var failingApp = Main.createApp(new RequestService(brokenRepository,
                Clock.fixed(Instant.parse("2026-10-30T09:15:30.123Z"), ZoneOffset.UTC)));

        JavalinTest.test(failingApp, (server, client) -> {
            var response = client.get("/api/health");
            String body = response.body().string();
            assertError(response, 503, "STORAGE_UNAVAILABLE");
            assertFalse(body.contains("/tmp/private.db"));
        });
    }

    @Test
    void unexpectedServiceFailureReturnsGenericSafeServerError() {
        var failingService = new RequestService(repository(), fixedClock()) {
            @Override
            public java.util.List<vibecoders101.example.domain.MaintenanceRequest> list() {
                throw new IllegalStateException("secret SQL and /tmp/private.db");
            }
        };
        JavalinTest.test(Main.createApp(failingService), (server, client) -> {
            var response = client.get("/api/requests");
            String body = response.body().string();
            assertError(response, 500, "INTERNAL_ERROR");
            assertFalse(body.contains("secret SQL"));
            assertFalse(body.contains("/tmp/private.db"));
        });
    }

    @Test
    void referenceExhaustionAndStorageFailureAreMappedCentrally() {
        var referenceCollisionRepository = repository();
        referenceCollisionRepository.save(new vibecoders101.example.domain.MaintenanceRequest(
                "MM-000000000321",
                new vibecoders101.example.domain.Resident("DEMO-MM-000000000321", "Demo Resident", "Maple", "A-1"),
                "Existing request", null, Instant.parse("2026-10-30T09:15:30.123Z")));
        var collidingService = new RequestService(referenceCollisionRepository, fixedClock(), () -> "MM-000000000321");
        var storageRepository = new SqliteRequestRepository(temporaryDirectory.resolve("unavailable.db")) {
            @Override
            public java.util.List<vibecoders101.example.domain.MaintenanceRequest> list() {
                throw new StorageUnavailableException("SQL /private/path.db", null);
            }
        };

        JavalinTest.test(Main.createApp(collidingService), (server, client) ->
                assertError(post(client, "/api/requests", requestJson()), 409, "REFERENCE_CONFLICT"));
        JavalinTest.test(Main.createApp(new RequestService(storageRepository, fixedClock())), (server, client) -> {
            var response = client.get("/api/requests");
            String body = response.body().string();
            assertErrorFromBody(response, body, 503, "STORAGE_UNAVAILABLE");
            assertFalse(body.contains("/private/path.db"));
            assertFalse(body.contains("SQL"));
        });
    }

    private SqliteRequestRepository repository() {
        return new SqliteRequestRepository(temporaryDirectory.resolve("maintenancemate.db"));
    }

    private io.javalin.Javalin app() {
        return Main.createApp(new RequestService(repository(), fixedClock()));
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-10-30T09:15:30.123Z"), ZoneOffset.UTC);
    }

    private static String requestJson() {
        return "{\"hostelBlock\":\" Maple Hostel, Block B \",\"roomNumber\":\" A-12 \",\"description\":\" Leaking tap \"}";
    }

    private static String extractReference(String body) {
        return body.replaceAll(".*\\\"reference\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private static Response post(HttpClient client, String path, String json) {
        return client.request(path, builder -> builder.header("Content-Type", "application/json")
                .post(HttpRequest.BodyPublishers.ofString(json)));
    }

    private static Response put(HttpClient client, String path, String json) {
        return client.request(path, builder -> builder.header("Content-Type", "application/json")
                .put(HttpRequest.BodyPublishers.ofString(json)));
    }

    private static void assertError(Response response, int expectedCode, String code) {
        String body = response.body().string();
        assertErrorFromBody(response, body, expectedCode, code);
    }

    private static void assertErrorFromBody(Response response, String body, int expectedCode, String code) {
        assertEquals(expectedCode, response.code());
        JsonNode root = readJson(body);
        assertTrue(root.path("error").isObject(), "Unexpected error envelope: " + root);
        assertEquals(code, root.path("error").path("code").asText());
        assertTrue(root.path("error").path("details").isObject(), "Unexpected error details: " + root);
    }

    private static void assertErrorDetails(Response response, String field, String message) {
        assertErrorDetails(response, 400, "VALIDATION_ERROR", field, message);
    }

    private static void assertErrorDetails(Response response, int status, String code, String field, String message) {
        String body = response.body().string();
        assertEquals(status, response.code());
        JsonNode root = readJson(body);
        assertTrue(root.path("error").isObject(), "Unexpected error envelope: " + root);
        assertEquals(code, root.path("error").path("code").asText());
        assertTrue(root.path("error").path("details").isObject(), "Unexpected error details: " + root);
        assertEquals(message, root.path("error").path("details").path("fields").path(field).asText());
    }

    private static JsonNode readJson(String value) {
        try {
            return JSON.readTree(value);
        } catch (Exception exception) {
            throw new AssertionError("Expected JSON response", exception);
        }
    }
}
