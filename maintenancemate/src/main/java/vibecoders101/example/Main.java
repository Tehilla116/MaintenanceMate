package vibecoders101.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

            config.routes.get("/api/health", context -> {
                service.checkStorageHealth();
                context.json(Map.of("status", "ok"));
            });
            config.routes.post("/api/requests", context -> {
                JsonNode json = parseObject(context.body());
                requireOnlyFields(json, List.of("hostelBlock", "roomNumber", "description"));
                String hostel = requiredString(json, "hostelBlock");
                String room = requiredString(json, "roomNumber");
                String description = requiredString(json, "description");
                MaintenanceRequest created = service.create(hostel, room, description);
                context.status(201).json(requestRepresentation(created));
            });
            config.routes.get("/api/requests", context -> {
                List<Map<String, Object>> requests = service.list().stream().map(Main::requestRepresentation).toList();
                context.json(Map.of("requests", requests, "count", requests.size()));
            });
            config.routes.get("/api/requests/{reference}", context -> {
                MaintenanceRequest request = service.requireByReference(context.pathParam("reference"));
                context.json(requestRepresentation(request));
            });
            config.routes.put("/api/requests/{reference}/status", context -> {
                JsonNode json = parseObject(context.body());
                requireOnlyFields(json, List.of("status"));
                MaintenanceRequest updated = service.updateStatus(context.pathParam("reference"), requiredString(json, "status"));
                context.json(requestRepresentation(updated));
            });
        });
        return app;
    }

    private static JsonNode parseObject(String body) throws JsonProcessingException {
        if (body == null || body.isBlank()) {
            throw new com.fasterxml.jackson.core.JsonParseException(null, "Request body must be valid JSON.");
        }
        try {
            JsonNode json = MAPPER.readTree(body);
            if (json == null || !json.isObject()) throw new InvalidRequestException("Request body must be a JSON object");
            return json;
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException exception) {
            throw new com.fasterxml.jackson.core.JsonParseException(null, "Request body must be valid JSON.", exception);
        }
    }

    private static void requireOnlyFields(JsonNode json, List<String> fields) {
        json.fieldNames().forEachRemaining(field -> {
            if (!fields.contains(field)) throw new InvalidRequestException(field, "Unknown field.");
        });
        for (String field : fields) {
            if (!json.has(field)) throw new InvalidRequestException(field, "This field is required.");
        }
    }

    private static String requiredString(JsonNode json, String field) {
        JsonNode value = json.get(field);
        if (value == null || !value.isTextual()) throw new InvalidRequestException(field, "Must be a string.");
        return value.textValue();
    }

    private static Map<String, Object> requestRepresentation(MaintenanceRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reference", request.getReference());
        result.put("hostelBlock", request.getHostelBlock());
        result.put("roomNumber", request.getRoomNumber());
        result.put("description", request.getDescription());
        result.put("status", request.getStatus());
        result.put("assignedStaffId", request.getAssignedStaff() == null ? null : request.getAssignedStaff().getUserId());
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
