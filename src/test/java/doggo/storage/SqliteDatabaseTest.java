package doggo.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import doggo.application.RepositoryException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteDatabaseTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void create_nestedDatabase_createsParentSchemaAndVersion() throws SQLException {
        Path databasePath = temporaryDirectory.resolve("nested").resolve("doggo.db");

        SqliteDatabase database = new SqliteDatabase(databasePath);

        assertTrue(Files.isDirectory(databasePath.getParent()));
        try (Connection connection = database.openConnection()) {
            assertEquals(Set.of("trips", "plans"), tableNames(connection));
            assertEquals(Set.of("plans_by_trip"), indexNames(connection));
            assertEquals(1, userVersion(connection));
            assertEquals(List.of("id", "title", "start_date", "end_date", "review_rating",
                    "review_text"), columnNames(connection, "trips"));
            assertEquals(List.of(true, true, true, true, false, false),
                    columnNotNulls(connection, "trips"));
            assertEquals(List.of("id", "trip_id", "destination", "plan_date", "plan_time",
                    "review_rating", "review_text"), columnNames(connection, "plans"));
            assertEquals(List.of(true, true, true, true, true, false, false),
                    columnNotNulls(connection, "plans"));
            assertEquals(List.of("trip_id"), indexedColumns(connection, "plans_by_trip"));
            assertEquals("CASCADE", foreignKeyDeleteAction(connection, "plans"));
            assertTrue(tableDefinition(connection, "trips").contains("STRICT"));
            assertTrue(tableDefinition(connection, "plans").contains("STRICT"));
        }
    }

    @Test
    void open_existingVersionOneDatabase_preservesData() throws SQLException {
        Path databasePath = temporaryDirectory.resolve("doggo.db");
        SqliteDatabase database = new SqliteDatabase(databasePath);
        String tripId = UUID.randomUUID().toString();

        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO trips (id, title, start_date, end_date) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, tripId);
            statement.setString(2, "Japan");
            statement.setString(3, "2027-01-01");
            statement.setString(4, "2027-01-09");
            statement.executeUpdate();
        }

        new SqliteDatabase(databasePath);

        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT title FROM trips WHERE id = ?")) {
            statement.setString(1, tripId);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals("Japan", resultSet.getString(1));
            }
        }
    }

    @Test
    void openConnection_eachConnection_enablesAndEnforcesForeignKeys() throws SQLException {
        SqliteDatabase database = new SqliteDatabase(temporaryDirectory.resolve("doggo.db"));

        try (Connection firstConnection = database.openConnection();
             Connection secondConnection = database.openConnection()) {
            assertEquals(1, foreignKeysPragma(firstConnection));
            assertEquals(1, foreignKeysPragma(secondConnection));
            assertThrows(SQLException.class, () -> insertOrphanPlan(firstConnection));
        }
    }

    @Test
    void open_newerSchemaVersion_rejectsWithoutChangingSchema() throws SQLException {
        Path databasePath = temporaryDirectory.resolve("doggo.db");
        SqliteDatabase database = new SqliteDatabase(databasePath);
        Set<String> tablesBefore;
        try (Connection connection = database.openConnection()) {
            tablesBefore = tableNames(connection);
        }

        try (Connection connection = database.openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("PRAGMA user_version = 2");
        }

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> new SqliteDatabase(databasePath));

        assertTrue(exception.getMessage().contains("2"));
        try (Connection connection = database.openConnection()) {
            assertEquals(tablesBefore, tableNames(connection));
            assertEquals(2, userVersion(connection));
        }
    }

    @Test
    void createDatabase_representativeConstraints_rejectInvalidRows() throws SQLException {
        SqliteDatabase database = new SqliteDatabase(temporaryDirectory.resolve("doggo.db"));

        try (Connection connection = database.openConnection()) {
            assertThrows(SQLException.class, () -> insertTrip(connection, " ", "2027-01-01",
                    "2027-01-02"));
            assertThrows(SQLException.class, () -> insertTrip(connection, "Invalid dates",
                    "2027-01-02", "2027-01-01"));
            insertTrip(connection, "Valid trip", "2027-01-01", "2027-01-02");
            String tripId = "valid-trip";
            insertTripWithId(connection, tripId, "Another trip", "2027-01-01", "2027-01-02");
            assertThrows(SQLException.class, () -> insertPlan(connection, UUID.randomUUID().toString(),
                    tripId, " ", null, null));
            assertThrows(SQLException.class, () -> insertPlan(connection, UUID.randomUUID().toString(),
                    tripId, "Destination", 0, null));
            assertThrows(SQLException.class, () -> insertPlan(connection, UUID.randomUUID().toString(),
                    tripId, "Destination", null, " "));
            insertPlan(connection, UUID.randomUUID().toString(), tripId, "Destination", 5,
                    "Worth visiting");
        }
    }

    @Test
    void createDatabase_existingUnversionedTable_failsWithoutReplacingIt() throws SQLException, IOException {
        Path databasePath = temporaryDirectory.resolve("doggo.db");
        Files.createDirectories(databasePath.getParent());
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE trips (marker TEXT NOT NULL)");
            statement.executeUpdate("INSERT INTO trips VALUES ('preserve me')");
        }

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> new SqliteDatabase(databasePath));

        assertTrue(exception.getCause() instanceof SQLException);
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT marker FROM trips")) {
            assertTrue(resultSet.next());
            assertEquals("preserve me", resultSet.getString(1));
            assertEquals(0, userVersion(connection));
        }
    }

    @Test
    void createDatabase_nullPath_rejectsPath() {
        assertThrows(NullPointerException.class, () -> new SqliteDatabase(null));
    }

    @Test
    void createDatabase_unusableParentDirectory_reportsRepositoryFailure() throws IOException {
        Path blockingFile = temporaryDirectory.resolve("blocking-file");
        Files.writeString(blockingFile, "not a directory");

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> new SqliteDatabase(blockingFile.resolve("doggo.db")));

        assertTrue(exception.getMessage().contains("directory"));
        assertTrue(exception.getCause() instanceof IOException);
    }

    private static Set<String> tableNames(Connection connection) throws SQLException {
        return namesFromQuery(connection,
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'");
    }

    private static Set<String> indexNames(Connection connection) throws SQLException {
        return namesFromQuery(connection,
                "SELECT name FROM sqlite_master WHERE type = 'index' AND name NOT LIKE 'sqlite_autoindex_%'");
    }

    private static Set<String> namesFromQuery(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            Set<String> names = new HashSet<>();
            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }
            return names;
        }
    }

    private static List<String> columnNames(Connection connection, String tableName) throws SQLException {
        List<String> names = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info('" + tableName + "')")) {
            while (resultSet.next()) {
                names.add(resultSet.getString("name"));
            }
        }
        return names;
    }

    private static List<Boolean> columnNotNulls(Connection connection, String tableName) throws SQLException {
        List<Boolean> notNulls = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info('" + tableName + "')")) {
            while (resultSet.next()) {
                notNulls.add(resultSet.getBoolean("notnull"));
            }
        }
        return notNulls;
    }

    private static List<String> indexedColumns(Connection connection, String indexName) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name FROM pragma_index_info(?) ORDER BY seqno")) {
            statement.setString(1, indexName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    columns.add(resultSet.getString(1));
                }
            }
        }
        return columns;
    }

    private static String foreignKeyDeleteAction(Connection connection, String tableName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA foreign_key_list('" + tableName + "')")) {
            assertTrue(resultSet.next());
            return resultSet.getString("on_delete");
        }
    }

    private static String tableDefinition(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getString(1);
            }
        }
    }

    private static int userVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA user_version")) {
            assertTrue(resultSet.next());
            return resultSet.getInt(1);
        }
    }

    private static int foreignKeysPragma(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA foreign_keys")) {
            assertTrue(resultSet.next());
            return resultSet.getInt(1);
        }
    }

    private static void insertOrphanPlan(Connection connection) throws SQLException {
        insertPlan(connection, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                "Destination", null, null);
    }

    private static void insertTrip(Connection connection, String title, String startDate,
                                   String endDate) throws SQLException {
        insertTripWithId(connection, UUID.randomUUID().toString(), title, startDate, endDate);
    }

    private static void insertTripWithId(Connection connection, String id, String title,
                                         String startDate, String endDate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO trips (id, title, start_date, end_date) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, id);
            statement.setString(2, title);
            statement.setString(3, startDate);
            statement.setString(4, endDate);
            statement.executeUpdate();
        }
    }

    private static void insertPlan(Connection connection, String id, String tripId, String destination,
                                   Integer rating, String reviewText) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO plans (id, trip_id, destination, plan_date, plan_time, review_rating, "
                        + "review_text) VALUES (?, ?, ?, '2027-01-01', '09:00', ?, ?)")) {
            statement.setString(1, id);
            statement.setString(2, tripId);
            statement.setString(3, destination);
            if (rating == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, rating);
            }
            statement.setString(5, reviewText);
            statement.executeUpdate();
        }
    }
}
