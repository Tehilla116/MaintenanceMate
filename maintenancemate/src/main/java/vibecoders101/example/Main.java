package vibecoders101.example;

import io.javalin.Javalin;

public class Main {
    private Main() {
    }

    public static Javalin createApp() {
        return Javalin.create(config -> config.routes.get("/api/health", context ->
                context.json(java.util.Map.of("status", "ok"))));
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getProperty("port", "7070"));
        createApp().start(port);
    }
}
