package vibecoders101.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {
    @Test
    void healthRouteReturnsJsonOkWhenStorageIsAvailable() {
        io.javalin.testtools.JavalinTest.test(Main.createApp(new vibecoders101.example.service.RequestService()),
                (server, client) -> {
                    var response = client.get("/api/health");
                    assertEquals(200, response.code());
                    assertEquals("{\"status\":\"ok\"}", response.body().string());
                });
    }
}
