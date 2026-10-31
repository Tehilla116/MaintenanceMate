package maintenancemate;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Creates, finds and lists maintenance requests, following the API and data contract.
 * Today's version keeps requests in an in-memory Map keyed by reference; the
 * persistent RequestRepository (SQLite) replaces this on 30 Oct.
 * updateStatus and checkStorageHealth are added later (transition rules: Bright; storage: 30 Oct).
 */
public class RequestService {
    private static final String REFERENCE_PATTERN = "^MM-[0-9A-F]{12}$";
    private static final int MAX_REFERENCE_ATTEMPTS = 5;

    private final Map<String, MaintenanceRequest> requests = new HashMap<>();
    private final Supplier<String> referenceGenerator;
    private final Clock clock;

    public RequestService() {
        this(() -> UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(), Clock.systemUTC());
    }

    /** Generator returns 12 uppercase hex characters (no "MM-" prefix). */
    public RequestService(Supplier<String> referenceGenerator, Clock clock) {
        this.referenceGenerator = referenceGenerator;
        this.clock = clock;
    }

    /** Validates, trims and saves a new request. Throws InvalidRequestException on bad input. */
    public MaintenanceRequest create(String hostelBlock, String roomNumber, String description) {
        Map<String, String> problems = new LinkedHashMap<>();
        String block = clean("hostelBlock", hostelBlock, 100, problems);
        String room = clean("roomNumber", roomNumber, 30, problems);
        String text = clean("description", description, 1000, problems);
        if (!problems.isEmpty()) {
            throw new InvalidRequestException("Request data is invalid.", problems);
        }

        String reference = uniqueReference();
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Resident resident = new Resident("demo-resident", "Demo Resident", block, room); // synthetic identity
        MaintenanceRequest request = new MaintenanceRequest(reference, resident, text, now);
        requests.put(reference, request);
        return request;
    }

    /** Finds by reference. Malformed -> InvalidRequestException; well-formed but absent -> RequestNotFoundException. */
    public MaintenanceRequest findByReference(String reference) {
        if (reference == null || !reference.matches(REFERENCE_PATTERN)) {
            throw new InvalidRequestException("Request data is invalid.",
                    Map.of("reference", "Must look like MM- followed by 12 uppercase hex characters."));
        }
        MaintenanceRequest found = requests.get(reference);
        if (found == null) {
            throw new RequestNotFoundException("Request not found.");
        }
        return found;
    }

    /** All requests, newest first, ties by reference ascending. */
    public List<MaintenanceRequest> list() {
        List<MaintenanceRequest> all = new ArrayList<>(requests.values());
        all.sort(Comparator.comparing(MaintenanceRequest::getCreatedAt).reversed()
                .thenComparing(MaintenanceRequest::getReference));
        return all;
    }

    private String uniqueReference() {
        for (int attempt = 0; attempt < MAX_REFERENCE_ATTEMPTS; attempt++) {
            String candidate = "MM-" + referenceGenerator.get();
            if (!requests.containsKey(candidate)) {
                return candidate;
            }
        }
        throw new ReferenceConflictException("Could not generate a unique reference.");
    }

    /** Returns the trimmed value, or records a problem for the field and returns null. */
    private String clean(String field, String value, int max, Map<String, String> problems) {
        if (value == null || value.isBlank()) {
            problems.put(field, "Must not be blank.");
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            problems.put(field, "Must be at most " + max + " characters.");
            return null;
        }
        return trimmed;
    }
}
