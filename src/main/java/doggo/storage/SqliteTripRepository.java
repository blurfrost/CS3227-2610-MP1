package doggo.storage;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import doggo.application.RepositoryException;
import doggo.application.TripRepository;
import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;

/**
 * Persists Trip aggregates in a versioned SQLite database.
 */
public final class SqliteTripRepository implements TripRepository {
    private static final String UPSERT_TRIP_SQL = """
            INSERT INTO trips (id, title, start_date, end_date, review_rating, review_text)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                title = excluded.title,
                start_date = excluded.start_date,
                end_date = excluded.end_date,
                review_rating = excluded.review_rating,
                review_text = excluded.review_text
            """;
    private static final String DELETE_PLANS_SQL = "DELETE FROM plans WHERE trip_id = ?";
    private static final String INSERT_PLAN_SQL = """
            INSERT INTO plans (id, trip_id, destination, plan_date, plan_time, review_rating, review_text)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String DELETE_TRIP_SQL = "DELETE FROM trips WHERE id = ?";
    private final SqliteDatabase database;
    private final SqliteTripReader reader;

    /**
     * Creates a repository backed by the SQLite database at the specified path.
     *
     * @param databasePath SQLite database file path.
     * @throws NullPointerException If databasePath is null.
     * @throws RepositoryException If the database cannot be initialized.
     */
    public SqliteTripRepository(Path databasePath) {
        this.database = new SqliteDatabase(databasePath);
        this.reader = new SqliteTripReader(database);
    }

    /**
     * Returns all persisted Trip aggregates.
     *
     * @return All stored Trips.
     * @throws RepositoryException If the Trips cannot be read.
     */
    @Override
    public List<Trip> findAll() {
        return reader.findAll();
    }

    /**
     * Returns the persisted Trip with the specified identity, if present.
     *
     * @param id Trip identity.
     * @return Matching Trip, if one exists.
     * @throws NullPointerException If id is null.
     * @throws RepositoryException If the Trip cannot be read.
     */
    @Override
    public Optional<Trip> findById(UUID id) {
        return reader.findById(id);
    }

    /**
     * Stores the complete Trip aggregate in one transaction.
     *
     * @param trip Trip aggregate to store.
     * @throws NullPointerException If trip is null.
     * @throws RepositoryException If the Trip cannot be stored.
     */
    @Override
    public void save(Trip trip) {
        Objects.requireNonNull(trip, "Trip cannot be null.");
        try (Connection connection = database.openConnection()) {
            saveAggregate(connection, trip);
        } catch (RepositoryException exception) {
            throw exception;
        } catch (SQLException | RuntimeException exception) {
            throw new RepositoryException("Unable to save Trip.", exception);
        }
    }

    /**
     * Deletes the Trip with the specified identity and its cascading Plans.
     *
     * @param id Trip identity.
     * @throws NullPointerException If id is null.
     * @throws RepositoryException If the Trip cannot be deleted.
     */
    @Override
    public void delete(UUID id) {
        Objects.requireNonNull(id, "Trip ID cannot be null.");
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_TRIP_SQL)) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (RepositoryException exception) {
            throw exception;
        } catch (SQLException | RuntimeException exception) {
            throw new RepositoryException("Unable to delete Trip.", exception);
        }
    }

    /**
     * Persists one complete aggregate and restores the connection autocommit mode safely.
     *
     * @param connection Open SQLite connection.
     * @param trip Trip aggregate to store.
     * @throws SQLException If the connection transaction state cannot be read.
     */
    private static void saveAggregate(Connection connection, Trip trip) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        Throwable failure = null;
        try {
            connection.setAutoCommit(false);
            upsertTrip(connection, trip);
            deletePlans(connection, trip.id());
            insertPlans(connection, trip);
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
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
            throw new RepositoryException("Unable to save Trip.", failure);
        }
    }

    /**
     * Upserts the aggregate root and its optional review fields.
     *
     * @param connection Open SQLite connection.
     * @param trip Trip aggregate to upsert.
     * @throws SQLException If the root cannot be upserted.
     */
    private static void upsertTrip(Connection connection, Trip trip) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_TRIP_SQL)) {
            statement.setString(1, trip.id().toString());
            statement.setString(2, trip.title());
            statement.setString(3, trip.startDate().toString());
            statement.setString(4, trip.endDate().toString());
            setReviewParameters(statement, 5, 6, trip.review());
            statement.executeUpdate();
        }
    }

    /**
     * Deletes the existing Plans belonging to a Trip before replacement.
     *
     * @param connection Open SQLite connection.
     * @param tripId Trip identity.
     * @throws SQLException If the Plans cannot be deleted.
     */
    private static void deletePlans(Connection connection, UUID tripId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_PLANS_SQL)) {
            statement.setString(1, tripId.toString());
            statement.executeUpdate();
        }
    }

    /**
     * Inserts the aggregate's current Plans in their domain order.
     *
     * @param connection Open SQLite connection.
     * @param trip Trip aggregate containing Plans to insert.
     * @throws SQLException If a Plan cannot be inserted.
     */
    private static void insertPlans(Connection connection, Trip trip) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_PLAN_SQL)) {
            for (Plan plan : trip.plans()) {
                statement.setString(1, plan.id().toString());
                statement.setString(2, trip.id().toString());
                statement.setString(3, plan.destination());
                statement.setString(4, plan.date().toString());
                statement.setString(5, plan.time().toString());
                setReviewParameters(statement, 6, 7, plan.review());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * Binds nullable Review fields as SQL NULL or their stored values.
     *
     * @param statement Prepared statement receiving Review parameters.
     * @param ratingIndex Rating parameter index.
     * @param textIndex Review-text parameter index.
     * @param review Optional Review value.
     * @throws SQLException If a Review parameter cannot be bound.
     */
    private static void setReviewParameters(PreparedStatement statement, int ratingIndex, int textIndex,
                                             Optional<Review> review) throws SQLException {
        if (review.isEmpty()) {
            statement.setNull(ratingIndex, Types.INTEGER);
            statement.setNull(textIndex, Types.VARCHAR);
            return;
        }
        Review reviewValue = review.orElseThrow();
        if (reviewValue.rating().isPresent()) {
            statement.setInt(ratingIndex, reviewValue.rating().getAsInt());
        } else {
            statement.setNull(ratingIndex, Types.INTEGER);
        }
        if (reviewValue.text().isPresent()) {
            statement.setString(textIndex, reviewValue.text().orElseThrow());
        } else {
            statement.setNull(textIndex, Types.VARCHAR);
        }
    }

    /**
     * Rolls back a failed transaction without hiding its primary failure.
     *
     * @param connection Open SQLite connection.
     * @param failure Primary persistence failure.
     */
    private static void rollback(Connection connection, Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            failure.addSuppressed(rollbackException);
        }
    }
}
