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

import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrowserWorkflowTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @TempDir
    Path temporaryDirectory;

    @Test
    void rootServesTheBrowserWorkflowPage() {
        JavalinTest.test(app(temporaryDirectory.resolve("page.db")), (server, client) -> {
            var response = client.get("/");

            assertEquals(200, response.code());
            assertTrue(response.headers().get("Content-Type").get(0).startsWith("text/html"));
            String page = response.body().string();
            assertTrue(page.contains("Maintenance Mate"));
            assertTrue(page.contains("id=\"resident-form\""));
            assertTrue(page.contains("id=\"staff-load\""));
            assertTrue(page.contains("id=\"lookup-form\""));
        });
    }

    @Test
    void browserWorkflowPageLinksItsThreePhasesInSequence() {
        JavalinTest.test(app(temporaryDirectory.resolve("phases.db")), (server, client) -> {
            var response = client.get("/");

            assertEquals(200, response.code());
            String page = response.body().string();
            assertTrue(page.contains("<nav class=\"workflow-nav\" aria-label=\"Demo workflow\">"));
            int submit = page.indexOf("href=\"#resident-heading\"");
            int staff = page.indexOf("href=\"#staff-heading\"");
            int lookup = page.indexOf("href=\"#lookup-heading\"");
            assertTrue(submit >= 0 && submit < staff && staff < lookup);
        });
    }

    @Test
    void browserWorkflowUsesApiAndPersistsRequestsAcrossApplicationRestart() {
        Path database = temporaryDirectory.resolve("workflow.db");
        AtomicReference<String> reference = new AtomicReference<>();

        JavalinTest.test(app(database), (server, client) -> {
            var created = post(client, "/api/requests",
                    "{\"hostelBlock\":\"Demo Hall, Block C\",\"roomNumber\":\"C-12\","
                            + "\"description\":\"Synthetic browser workflow request\"}");
            assertEquals(201, created.code());
            JsonNode createdRequest = readJson(created.body().string());
            reference.set(createdRequest.path("reference").asText());
            assertTrue(reference.get().matches("MM-[0-9A-F]{12}"));
            assertEquals("Received", createdRequest.path("status").asText());

            var listed = client.get("/api/requests");
            assertEquals(200, listed.code());
            JsonNode list = readJson(listed.body().string());
            assertEquals(1, list.path("count").asInt());
            assertEquals(reference.get(), list.path("requests").get(0).path("reference").asText());

            var updated = put(client, "/api/requests/" + reference.get() + "/status",
                    "{\"status\":\"In Progress\"}");
            assertEquals(200, updated.code());
            assertEquals("In Progress", readJson(updated.body().string()).path("status").asText());

            var lookedUp = client.get("/api/requests/" + reference.get());
            assertEquals(200, lookedUp.code());
            assertEquals("Synthetic browser workflow request", readJson(lookedUp.body().string())
                    .path("description").asText());
        });

        JavalinTest.test(app(database), (server, client) -> {
            var lookedUpAfterRestart = client.get("/api/requests/" + reference.get());
            assertEquals(200, lookedUpAfterRestart.code());
            JsonNode persistedRequest = readJson(lookedUpAfterRestart.body().string());
            assertEquals(reference.get(), persistedRequest.path("reference").asText());
            assertEquals("In Progress", persistedRequest.path("status").asText());
        });
    }

    private io.javalin.Javalin app(Path database) {
        return Main.createApp(new RequestService(new SqliteRequestRepository(database),
                Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC)));
    }

    private static Response post(HttpClient client, String path, String json) {
        return client.request(path, builder -> builder.header("Content-Type", "application/json")
                .post(HttpRequest.BodyPublishers.ofString(json)));
    }

    private static Response put(HttpClient client, String path, String json) {
        return client.request(path, builder -> builder.header("Content-Type", "application/json")
                .put(HttpRequest.BodyPublishers.ofString(json)));
    }

    private static JsonNode readJson(String body) {
        try {
            return JSON.readTree(body);
        } catch (Exception exception) {
            throw new AssertionError("Expected JSON response", exception);
        }
    }
}
