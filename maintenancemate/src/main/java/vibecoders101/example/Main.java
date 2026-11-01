package vibecoders101.example;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import vibecoders101.example.domain.MaintenanceRequest;
import vibecoders101.example.repository.SqliteRequestRepository;
import vibecoders101.example.service.InvalidRequestException;
import vibecoders101.example.service.InvalidStatusTransitionException;
import vibecoders101.example.service.ReferenceGenerationException;
import vibecoders101.example.service.RequestNotFoundException;
import vibecoders101.example.service.RequestService;
import vibecoders101.example.service.StorageUnavailableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);
    private Main() { }

    public static Javalin createApp() {
        return createApp(new RequestService(new SqliteRequestRepository(Path.of(".", "data", "maintenancemate.db")),
                Clock.systemUTC()));
    }

    public static Javalin createApp(RequestService service) {
        Javalin app = Javalin.create(config -> {
            config.routes.exception(InvalidRequestException.class, (exception, context) ->
                    error(context, 400, "VALIDATION_ERROR", "Request data is invalid.", exception.getDetails()));
            config.routes.exception(JsonProcessingException.class, (exception, context) ->
                    error(context, 400, "MALFORMED_JSON", "Request body must be valid JSON.", Map.of()));
            config.routes.exception(BadRequestResponse.class, (exception, context) ->
                    error(context, 400, "MALFORMED_JSON", "Request body must be valid JSON.", Map.of()));
            config.routes.exception(HttpResponseException.class, (exception, context) -> {
                if (exception.getStatus() == 400) {
                    error(context, 400, "MALFORMED_JSON", "Request body must be valid JSON.", Map.of());
                } else {
                    error(context, exception.getStatus(), "INTERNAL_ERROR", "An unexpected error occurred.", Map.of());
                }
            });
            config.routes.exception(RequestNotFoundException.class, (exception, context) ->
                    error(context, 404, "REQUEST_NOT_FOUND", "Maintenance request was not found.", Map.of()));
            config.routes.exception(InvalidStatusTransitionException.class, (exception, context) ->
                    error(context, 409, "STATUS_TRANSITION_CONFLICT", "Request status cannot be changed that way.", Map.of()));
            config.routes.exception(ReferenceGenerationException.class, (exception, context) ->
                    error(context, 409, "REFERENCE_CONFLICT", "A unique request reference could not be generated.", Map.of()));
            config.routes.exception(StorageUnavailableException.class, (exception, context) ->
                    error(context, 503, "STORAGE_UNAVAILABLE", "Request storage is unavailable.", Map.of()));
            config.routes.exception(Exception.class, (exception, context) -> {
                System.err.println("Unhandled request failure: " + exception.getClass().getSimpleName());
                error(context, 500, "INTERNAL_ERROR", "An unexpected error occurred.", Map.of());
            });

            config.routes.get("/", context -> servePage(context));
            config.routes.get("/api/health", context -> checkHealth(service, context));
            config.routes.post("/api/requests", context -> createRequest(service, context));
            config.routes.get("/api/requests", context -> listRequests(service, context));
            config.routes.get("/api/requests/{reference}", context -> getRequest(service, context));
            config.routes.put("/api/requests/{reference}/status", context -> updateRequestStatus(service, context));
        });
        return app;
    }

    private static void servePage(Context context) {
        InputStream page = Main.class.getResourceAsStream("/vibecoders101/example/index.html");
        if (page == null) {
            context.status(500).result("Browser workflow page is unavailable.");
            return;
        }
        try (InputStream html = page) {
            context.contentType("text/html; charset=utf-8").result(html.readAllBytes());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static void checkHealth(RequestService service, Context context) {
        service.checkStorageHealth();
        context.json(Map.of("status", "ok"));
    }

    private static void createRequest(RequestService service, Context context) throws JsonProcessingException {
        JsonNode json = parseObject(context.body());
        requireOnlyFields(json, List.of("hostelBlock", "roomNumber", "description"));
        String hostelBlock = requiredString(json, "hostelBlock");
        String roomNumber = requiredString(json, "roomNumber");
        String description = requiredString(json, "description");
        MaintenanceRequest created = service.create(hostelBlock, roomNumber, description);
        context.status(201).json(requestRepresentation(created));
    }

    private static void listRequests(RequestService service, Context context) {
        List<MaintenanceRequest> requests = service.list();
        List<Map<String, Object>> representations = new ArrayList<>();
        for (MaintenanceRequest request : requests) {
            representations.add(requestRepresentation(request));
        }
        context.json(Map.of("requests", representations, "count", representations.size()));
    }

    private static void getRequest(RequestService service, Context context) {
        String reference = context.pathParam("reference");
        MaintenanceRequest request = service.requireByReference(reference);
        context.json(requestRepresentation(request));
    }

    private static void updateRequestStatus(RequestService service, Context context) throws JsonProcessingException {
        JsonNode json = parseObject(context.body());
        requireOnlyFields(json, List.of("status"));
        String reference = context.pathParam("reference");
        String status = requiredString(json, "status");
        MaintenanceRequest updated = service.updateStatus(reference, status);
        context.json(requestRepresentation(updated));
    }

    private static JsonNode parseObject(String body) throws JsonProcessingException {
        if (body == null || body.isBlank()) {
            throw new JsonParseException(null, "Request body must be valid JSON.");
        }
        try {
            JsonNode json = MAPPER.readTree(body);
            if (json == null || !json.isObject()) {
                throw new InvalidRequestException("Request body must be a JSON object");
            }
            return json;
        } catch (MismatchedInputException exception) {
            throw new JsonParseException(null, "Request body must be valid JSON.", exception);
        }
    }

    private static void requireOnlyFields(JsonNode json, List<String> fields) {
        Iterator<String> fieldNames = json.fieldNames();
        while (fieldNames.hasNext()) {
            String field = fieldNames.next();
            if (!fields.contains(field)) {
                throw new InvalidRequestException(field, "Unknown field.");
            }
        }
        for (String field : fields) {
            if (!json.has(field)) {
                throw new InvalidRequestException(field, "This field is required.");
            }
        }
    }

    private static String requiredString(JsonNode json, String field) {
        JsonNode value = json.get(field);
        if (value == null || !value.isTextual()) {
            throw new InvalidRequestException(field, "Must be a string.");
        }
        return value.textValue();
    }

    private static Map<String, Object> requestRepresentation(MaintenanceRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reference", request.getReference());
        result.put("hostelBlock", request.getHostelBlock());
        result.put("roomNumber", request.getRoomNumber());
        result.put("description", request.getDescription());
        result.put("status", request.getStatus());
        String assignedStaffId = null;
        if (request.getAssignedStaff() != null) {
            assignedStaffId = request.getAssignedStaff().getUserId();
        }
        result.put("assignedStaffId", assignedStaffId);
        result.put("createdAt", TIMESTAMP_FORMAT.format(request.getCreatedAt()));
        result.put("updatedAt", TIMESTAMP_FORMAT.format(request.getUpdatedAt()));
        return result;
    }

    private static void error(Context context, int status, String code, String message, Map<String, Object> details) {
        context.status(status).json(Map.of("error", Map.of("code", code, "message", message, "details", details)));
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getProperty("port", "7070"));
        createApp().start(port);
    }
}
