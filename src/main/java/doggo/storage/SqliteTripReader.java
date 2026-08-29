package doggo.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;

/**
 * Reads Trip aggregates and their child Plans from SQLite.
 */
final class SqliteTripReader {
    private static final String SELECT_TRIPS_SQL = """
            SELECT t.id AS trip_id, t.title AS trip_title, t.start_date AS trip_start_date,
                   t.end_date AS trip_end_date, t.review_rating AS trip_review_rating,
                   t.review_text AS trip_review_text, p.id AS plan_id,
                   p.destination AS plan_destination, p.plan_date AS plan_date,
                   p.plan_time AS plan_time, p.review_rating AS plan_review_rating,
                   p.review_text AS plan_review_text
            FROM trips t
            LEFT JOIN plans p ON p.trip_id = t.id
            """;
    private static final String ORDER_BY_SQL =
            " ORDER BY t.id, p.plan_date, p.plan_time, p.id";
    private final SqliteDatabase database;

    /**
     * Creates a reader backed by the specified SQLite database.
     *
     * @param database SQLite database connection manager.
     * @throws NullPointerException If database is null.
     */
    SqliteTripReader(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database, "Database cannot be null.");
    }

    /**
     * Returns all Trip aggregates in deterministic identity order.
     *
     * @return All stored Trips.
     * @throws RepositoryException If the rows cannot be read or rehydrated.
     */
    List<Trip> findAll() {
        return readTrips(null);
    }

    /**
     * Returns the Trip with the specified identity, if it exists.
     *
     * @param id Trip identity.
     * @return Matching Trip, if one exists.
     * @throws NullPointerException If id is null.
     * @throws RepositoryException If the row cannot be read or rehydrated.
     */
    Optional<Trip> findById(UUID id) {
        Objects.requireNonNull(id, "Trip ID cannot be null.");
        List<Trip> trips = readTrips(id);
        return trips.stream().findFirst();
    }

    /**
     * Reads and rehydrates Trip aggregates using one joined query.
     *
     * @param tripId Optional Trip identity filter.
     * @return Rehydrated Trips.
     * @throws RepositoryException If the rows cannot be read or rehydrated.
     */
    private List<Trip> readTrips(UUID tripId) {
        String sql = SELECT_TRIPS_SQL + (tripId == null ? "" : " WHERE t.id = ?") + ORDER_BY_SQL;
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (tripId != null) {
                statement.setString(1, tripId.toString());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return rehydrate(resultSet);
            }
        } catch (RepositoryException exception) {
            throw exception;
        } catch (SQLException | RuntimeException exception) {
            throw new RepositoryException("Unable to read Trip aggregates.", exception);
        }
    }

    /**
     * Groups joined rows and restores each Trip aggregate once.
     *
     * @param resultSet Joined Trip and Plan rows.
     * @return Rehydrated Trips.
     * @throws SQLException If a column cannot be read.
     */
    private static List<Trip> rehydrate(ResultSet resultSet) throws SQLException {
        Map<UUID, TripRows> rowsByTrip = new LinkedHashMap<>();
        while (resultSet.next()) {
            UUID tripId = parseUuid(resultSet.getString("trip_id"));
            TripRows tripRows = rowsByTrip.computeIfAbsent(tripId,
                    ignored -> createTripRows(resultSet));
            tripRows.addPlan(resultSet);
        }
        return rowsByTrip.values().stream()
                .map(TripRows::toTrip)
                .toList();
    }

    /**
     * Creates the Trip portion of an aggregate accumulator from the current row.
     *
     * @param resultSet Joined Trip and Plan rows positioned at a Trip.
     * @return Accumulator for the current Trip.
     */
    private static TripRows createTripRows(ResultSet resultSet) {
        try {
            return new TripRows(
                    parseUuid(resultSet.getString("trip_id")),
                    resultSet.getString("trip_title"),
                    parseDate(resultSet.getString("trip_start_date")),
                    parseDate(resultSet.getString("trip_end_date")),
                    readReview(resultSet, "trip_review_rating", "trip_review_text"));
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to read Trip aggregates.", exception);
        }
    }

    /**
     * Parses a persisted UUID value.
     *
     * @param value Persisted UUID text.
     * @return Parsed UUID.
     */
    private static UUID parseUuid(String value) {
        return UUID.fromString(value);
    }

    /**
     * Parses a persisted ISO local date value.
     *
     * @param value Persisted date text.
     * @return Parsed local date.
     */
    private static LocalDate parseDate(String value) {
        return LocalDate.parse(value);
    }

    /**
     * Parses a persisted ISO local time value.
     *
     * @param value Persisted time text.
     * @return Parsed local time.
     */
    private static LocalTime parseTime(String value) {
        return LocalTime.parse(value);
    }

    /**
     * Restores an optional Review from nullable persisted columns.
     *
     * @param resultSet Joined Trip and Plan rows.
     * @param ratingColumn Rating column label.
     * @param textColumn Review-text column label.
     * @return Restored Review, if at least one field is present.
     * @throws SQLException If a review column cannot be read.
     */
    private static Optional<Review> readReview(ResultSet resultSet, String ratingColumn,
                                               String textColumn) throws SQLException {
        int rating = resultSet.getInt(ratingColumn);
        OptionalInt optionalRating = resultSet.wasNull()
                ? OptionalInt.empty()
                : OptionalInt.of(rating);
        Optional<String> text = Optional.ofNullable(resultSet.getString(textColumn));
        if (optionalRating.isEmpty() && text.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Review(optionalRating, text));
    }

    /**
     * Accumulates the rows belonging to one Trip before restoring its aggregate.
     */
    private static final class TripRows {
        private final UUID id;
        private final String title;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Optional<Review> review;
        private final List<Plan> plans = new ArrayList<>();

        private TripRows(UUID id, String title, LocalDate startDate, LocalDate endDate,
                         Optional<Review> review) {
            this.id = id;
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.review = review;
        }

        /**
         * Adds the Plan on the current joined row, if one exists.
         *
         * @param resultSet Joined Trip and Plan rows.
         * @throws SQLException If a Plan column cannot be read.
         */
        private void addPlan(ResultSet resultSet) throws SQLException {
            String planId = resultSet.getString("plan_id");
            if (planId == null) {
                return;
            }
            plans.add(Plan.restore(
                    parseUuid(planId),
                    resultSet.getString("plan_destination"),
                    parseDate(resultSet.getString("plan_date")),
                    parseTime(resultSet.getString("plan_time")),
                    readReview(resultSet, "plan_review_rating", "plan_review_text")));
        }

        /**
         * Restores the accumulated Trip and its Plans.
         *
         * @return Rehydrated Trip.
         */
        private Trip toTrip() {
            return Trip.restore(id, title, startDate, endDate, plans, review);
        }
    }
}
