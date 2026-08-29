package doggo.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import doggo.application.RepositoryException;

/**
 * Manages SQLite database connections and the versioned doggo schema.
 */
final class SqliteDatabase {
    private static final int SCHEMA_VERSION = 1;
    private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";
    private static final String CREATE_TRIPS_SQL = """
            CREATE TABLE trips (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL CHECK (trim(title) <> ''),
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                review_rating INTEGER,
                review_text TEXT,
                CHECK (end_date >= start_date),
                CHECK (review_rating IS NULL OR review_rating BETWEEN 1 AND 5),
                CHECK (review_text IS NULL OR trim(review_text) <> '')
            ) STRICT
            """;
    private static final String CREATE_PLANS_SQL = """
            CREATE TABLE plans (
                id TEXT PRIMARY KEY NOT NULL,
                trip_id TEXT NOT NULL,
                destination TEXT NOT NULL CHECK (trim(destination) <> ''),
                plan_date TEXT NOT NULL,
                plan_time TEXT NOT NULL,
                review_rating INTEGER,
                review_text TEXT,
                FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
                CHECK (review_rating IS NULL OR review_rating BETWEEN 1 AND 5),
                CHECK (review_text IS NULL OR trim(review_text) <> '')
            ) STRICT
            """;
    private static final String CREATE_PLANS_TRIP_INDEX_SQL =
            "CREATE INDEX plans_by_trip ON plans(trip_id)";
    private final Path databasePath;

    /**
     * Creates or opens the database at the specified path.
     *
     * @param databasePath Database file path.
     * @throws NullPointerException If databasePath is null.
     * @throws RepositoryException If the directory or database schema cannot be initialized.
     */
    SqliteDatabase(Path databasePath) {
        this.databasePath = Objects.requireNonNull(databasePath, "Database path cannot be null.")
                .toAbsolutePath();
        createParentDirectories();
        initializeSchema();
    }

    /**
     * Opens a new connection with foreign-key enforcement enabled.
     *
     * @return Newly opened SQLite connection.
     * @throws RepositoryException If the connection or foreign-key configuration cannot be created.
     */
    Connection openConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(JDBC_URL_PREFIX + databasePath);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException exception) {
            closeAfterOpenFailure(connection, exception);
            throw new RepositoryException("Unable to open SQLite connection.", exception);
        }
    }

    /**
     * Creates the database parent directories when they do not exist.
     */
    private void createParentDirectories() {
        Path parent = databasePath.getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (java.io.IOException | SecurityException exception) {
            throw new RepositoryException("Unable to create SQLite database directory.", exception);
        }
    }

    /**
     * Checks the schema version and creates the schema for a fresh database.
     */
    private void initializeSchema() {
        try (Connection connection = openConnection()) {
            int schemaVersion = readSchemaVersion(connection);
            if (schemaVersion > SCHEMA_VERSION) {
                throw new RepositoryException("Unsupported SQLite schema version: " + schemaVersion);
            }
            if (schemaVersion < 0) {
                throw new RepositoryException("Unsupported SQLite schema version: " + schemaVersion);
            }
            if (schemaVersion == 0) {
                createSchema(connection);
            }
        } catch (RepositoryException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to initialize SQLite database.", exception);
        }
    }

    /**
     * Reads the SQLite user-version marker from an open connection.
     *
     * @param connection Open database connection.
     * @return Stored schema version.
     * @throws SQLException If the version cannot be read.
     */
    private static int readSchemaVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA user_version")) {
            if (!resultSet.next()) {
                throw new SQLException("SQLite user_version returned no value.");
            }
            return resultSet.getInt(1);
        }
    }

    /**
     * Creates all version-one schema objects in one transaction.
     *
     * @param connection Open database connection.
     */
    private static void createSchema(Connection connection) {
        boolean originalAutoCommit = true;
        SQLException failure = null;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(CREATE_TRIPS_SQL);
                statement.executeUpdate(CREATE_PLANS_SQL);
                statement.executeUpdate(CREATE_PLANS_TRIP_INDEX_SQL);
                statement.executeUpdate("PRAGMA user_version = " + SCHEMA_VERSION);
            }
            connection.commit();
        } catch (SQLException exception) {
            failure = exception;
            rollback(connection, failure);
        }

        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException exception) {
            if (failure == null) {
                failure = exception;
            } else {
                failure.addSuppressed(exception);
            }
        }

        if (failure != null) {
            throw new RepositoryException("Unable to initialize SQLite schema.", failure);
        }
    }

    /**
     * Rolls back schema creation and preserves a rollback failure on the original exception.
     *
     * @param connection Open database connection.
     * @param failure Original schema-creation failure.
     */
    private static void rollback(Connection connection, SQLException failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            failure.addSuppressed(rollbackException);
        }
    }

    /**
     * Closes a connection whose setup failed without hiding the original failure.
     *
     * @param connection Connection that may need closing.
     * @param failure Original connection-setup failure.
     */
    private static void closeAfterOpenFailure(Connection connection, SQLException failure) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException closeException) {
            failure.addSuppressed(closeException);
        }
    }
}
