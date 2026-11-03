package vibecoders101.example.repository;

import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import vibecoders101.example.domain.MaintenanceRequest;
import vibecoders101.example.domain.MaintenanceStaff;
import vibecoders101.example.domain.Resident;
import vibecoders101.example.service.StorageUnavailableException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SqliteRequestRepository implements RequestRepository {
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
    private final Path databasePath;
    private final String jdbcUrl;

    public SqliteRequestRepository(Path databasePath) {
        this.databasePath = Objects.requireNonNull(databasePath, "databasePath is required").toAbsolutePath();
        this.jdbcUrl = "jdbc:sqlite:" + this.databasePath;
        initializeSchema();
    }

    @Override
    public void save(MaintenanceRequest request) {
        String sql = "INSERT INTO maintenance_requests "
                + "(reference, hostel_block, room_number, description, status, assigned_staff_id, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindRequest(statement, request);
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (isPrimaryKeyCollision(exception)) {
                throw new DuplicateReferenceException(request.getReference(), exception);
            }
            throw storageUnavailable(exception);
        }
    }

    @Override
    public Optional<MaintenanceRequest> findByReference(String reference) {
        String sql = "SELECT reference, hostel_block, room_number, description, status, assigned_staff_id, created_at, updated_at "
                + "FROM maintenance_requests WHERE reference = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reference);
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return Optional.of(mapRequest(rows));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw storageUnavailable(exception);
        }
    }

    @Override
    public List<MaintenanceRequest> list() {
        String sql = "SELECT reference, hostel_block, room_number, description, status, assigned_staff_id, created_at, updated_at "
                + "FROM maintenance_requests ORDER BY created_at DESC, reference ASC";
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            List<MaintenanceRequest> requests = new ArrayList<>();
            while (rows.next()) {
                requests.add(mapRequest(rows));
            }
            requests.sort((first, second) -> {
                int createdComparison = second.getCreatedAt().compareTo(first.getCreatedAt());
                if (createdComparison != 0) {
                    return createdComparison;
                }
                return first.getReference().compareTo(second.getReference());
            });
            return List.copyOf(requests);
        } catch (SQLException exception) {
            throw storageUnavailable(exception);
        }
    }

    @Override
    public boolean updateStatus(String reference, String expectedCurrentStatus, String newStatus, Instant updatedAt) {
        String sql = "UPDATE maintenance_requests SET status = ?, updated_at = ? WHERE reference = ? AND status = ?";
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newStatus);
                statement.setString(2, format(updatedAt));
                statement.setString(3, reference);
                statement.setString(4, expectedCurrentStatus);
                int changed = statement.executeUpdate();
                connection.commit();
                return changed == 1;
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw storageUnavailable(exception);
        }
    }

    @Override
    public boolean checkHealth() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT 1 FROM maintenance_requests LIMIT 1")) {
            return true;
        } catch (SQLException exception) {
            throw storageUnavailable(exception);
        }
    }

    private void initializeSchema() {
        try {
            Path parent = databasePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Connection connection = openConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS maintenance_requests ("
                        + "reference TEXT PRIMARY KEY NOT NULL CHECK (length(reference) = 15 "
                        + "AND substr(reference, 1, 3) = 'MM-' AND substr(reference, 4) NOT GLOB '*[^0-9A-F]*'), "
                        + "hostel_block TEXT NOT NULL CHECK (length(trim(hostel_block)) BETWEEN 1 AND 100), "
                        + "room_number TEXT NOT NULL CHECK (length(trim(room_number)) BETWEEN 1 AND 30), "
                        + "description TEXT NOT NULL CHECK (length(trim(description)) BETWEEN 1 AND 1000), "
                        + "status TEXT NOT NULL CHECK (status IN ('Received', 'In Progress', 'Resolved')), "
                        + "assigned_staff_id TEXT NULL, created_at TEXT NOT NULL, updated_at TEXT NOT NULL)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_maintenance_requests_created_at "
                        + "ON maintenance_requests(created_at DESC, reference ASC)");
            }
        } catch (Exception exception) {
            throw storageUnavailable(exception);
        }
    }

    private static void rollback(Connection connection, SQLException failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    private static void bindRequest(PreparedStatement statement, MaintenanceRequest request) throws SQLException {
        statement.setString(1, request.getReference());
        statement.setString(2, request.getHostelBlock());
        statement.setString(3, request.getRoomNumber());
        statement.setString(4, request.getDescription());
        statement.setString(5, request.getStatus());
        if (request.getAssignedStaff() == null) {
            statement.setNull(6, Types.VARCHAR);
        } else {
            statement.setString(6, request.getAssignedStaff().getUserId());
        }
        statement.setString(7, format(request.getCreatedAt()));
        statement.setString(8, format(request.getUpdatedAt()));
    }

    private static MaintenanceRequest mapRequest(ResultSet row) throws SQLException {
        String reference = row.getString("reference");
        String hostelBlock = row.getString("hostel_block");
        String roomNumber = row.getString("room_number");
        String description = row.getString("description");
        String status = row.getString("status");
        String staffId = row.getString("assigned_staff_id");
        MaintenanceStaff staff = null;
        if (staffId != null) {
            staff = new MaintenanceStaff(staffId, "Demo Staff");
        }
        Resident resident = new Resident("DEMO-" + reference, "Demo Resident", hostelBlock, roomNumber);
        Instant createdAt = parse(row.getString("created_at"));
        Instant updatedAt = parse(row.getString("updated_at"));
        return MaintenanceRequest.restore(reference, resident, description, staff, status, createdAt, updatedAt);
    }

    private static String format(Instant instant) {
        return TIMESTAMP_FORMAT.format(instant.truncatedTo(ChronoUnit.MILLIS));
    }

    private static Instant parse(String value) {
        return Instant.parse(value);
    }

    private static boolean isPrimaryKeyCollision(SQLException exception) {
        if (!(exception instanceof SQLiteException)) {
            return false;
        }
        SQLiteException sqliteException = (SQLiteException) exception;
        return sqliteException.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_PRIMARYKEY
                || sqliteException.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE;
    }

    private static StorageUnavailableException storageUnavailable(Throwable cause) {
        return new StorageUnavailableException("SQLite storage is unavailable", cause);
    }
}
