package vibecoders101.example;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {
    @Test
    void healthRouteReturnsJsonOk() {
        JavalinTest.test(Main.createApp(), (server, client) -> {
            var response = client.get("/api/health");
            assertEquals(200, response.code());
            assertEquals("{\"status\":\"ok\"}", response.body().string());
        });
    }
}
