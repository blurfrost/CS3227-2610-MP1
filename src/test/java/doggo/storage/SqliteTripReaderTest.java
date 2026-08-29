package doggo.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteTripReaderTest {
    private static final UUID FIRST_TRIP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_TRIP_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @TempDir
    Path temporaryDirectory;
    private SqliteDatabase database;

    @Test
    void findAll_emptyDatabase_returnsEmptyList() {
        SqliteTripReader reader = createReader("empty.db");

        assertEquals(List.of(), reader.findAll());
    }

    @Test
    void findAll_tripWithoutPlans_restoresTripWithoutPlans() throws SQLException {
        SqliteTripReader reader = createReaderWithTrip(
                "trip-without-plans.db", FIRST_TRIP_ID, "Japan", "2027-01-01", "2027-01-09");

        Trip trip = reader.findAll().get(0);

        assertEquals(FIRST_TRIP_ID, trip.id());
        assertEquals("Japan", trip.title());
        assertEquals("2027-01-01", trip.startDate().toString());
        assertEquals("2027-01-09", trip.endDate().toString());
        assertTrue(trip.plans().isEmpty());
        assertTrue(trip.review().isEmpty());
    }

    @Test
    void findById_completeAggregate_restoresPlansAndPartialReviews() throws SQLException {
        SqliteTripReader reader = createReader("complete.db");
        insertTrip(FIRST_TRIP_ID, "Japan", "2027-01-01", "2027-01-09", 5,
                "Excellent journey");
        insertPlan(UUID.fromString("00000000-0000-0000-0000-000000000011"), FIRST_TRIP_ID,
                "Mount Fuji", "2027-01-05", "09:00", null, "Worth revisiting");
        insertPlan(UUID.fromString("00000000-0000-0000-0000-000000000012"), FIRST_TRIP_ID,
                "Tokyo", "2027-01-06", "10:30", 4, "Great food");

        Optional<Trip> result = reader.findById(FIRST_TRIP_ID);

        assertTrue(result.isPresent());
        Trip trip = result.orElseThrow();
        assertEquals(OptionalInt.of(5), trip.review().orElseThrow().rating());
        assertEquals(Optional.of("Excellent journey"), trip.review().orElseThrow().text());
        assertEquals(2, trip.plans().size());
        Plan firstPlan = trip.plans().get(0);
        assertEquals("Mount Fuji", firstPlan.destination());
        assertEquals(OptionalInt.empty(), firstPlan.review().orElseThrow().rating());
        assertEquals(Optional.of("Worth revisiting"), firstPlan.review().orElseThrow().text());
        Plan secondPlan = trip.plans().get(1);
        assertEquals(OptionalInt.of(4), secondPlan.review().orElseThrow().rating());
        assertEquals(Optional.of("Great food"), secondPlan.review().orElseThrow().text());
    }

    @Test
    void findAll_multipleTripsAndUnorderedPlans_returnsDeterministicOrder() throws SQLException {
        SqliteTripReader reader = createReader("ordered.db");
        insertTrip(SECOND_TRIP_ID, "Korea", "2027-02-01", "2027-02-09", null, null);
        insertTrip(FIRST_TRIP_ID, "Japan", "2027-01-01", "2027-01-09", null, null);
        insertPlan(UUID.fromString("00000000-0000-0000-0000-000000000022"), FIRST_TRIP_ID,
                "Late", "2027-01-05", "09:00", null, null);
        insertPlan(UUID.fromString("00000000-0000-0000-0000-000000000021"), FIRST_TRIP_ID,
                "Early", "2027-01-04", "12:00", null, null);
        insertPlan(UUID.fromString("00000000-0000-0000-0000-000000000023"), FIRST_TRIP_ID,
                "Same time, higher ID", "2027-01-05", "09:00", null, null);

        List<Trip> trips = reader.findAll();

        assertEquals(List.of(FIRST_TRIP_ID, SECOND_TRIP_ID), trips.stream().map(Trip::id).toList());
        assertEquals(List.of("Early", "Late", "Same time, higher ID"),
                trips.get(0).plans().stream().map(Plan::destination).toList());
    }

    @Test
    void findById_missingId_returnsEmptyOptional() {
        SqliteTripReader reader = createReader("missing.db");

        assertEquals(Optional.empty(), reader.findById(FIRST_TRIP_ID));
    }

    @Test
    void findAll_malformedPersistedDate_wrapsCauseInRepositoryException() throws SQLException {
        SqliteTripReader reader = createReader("malformed.db");
        insertTrip(FIRST_TRIP_ID, "Broken", "not-a-date", "not-a-date", null, null);

        RepositoryException exception = assertThrows(RepositoryException.class, reader::findAll);

        assertEquals("Unable to read Trip aggregates.", exception.getMessage());
        assertInstanceOf(java.time.format.DateTimeParseException.class, exception.getCause());
    }

    @Test
    void findAll_invalidAggregateState_wrapsDomainCauseInRepositoryException() throws SQLException {
        SqliteTripReader reader = createReader("invalid-aggregate.db");
        insertTripIgnoringChecks(FIRST_TRIP_ID, "Broken", "2027-01-02", "2027-01-01");

        RepositoryException exception = assertThrows(RepositoryException.class, reader::findAll);

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    private SqliteTripReader createReader(String fileName) {
        database = new SqliteDatabase(temporaryDirectory.resolve(fileName));
        return new SqliteTripReader(database);
    }

    private SqliteTripReader createReaderWithTrip(String fileName, UUID id, String title,
                                                  String startDate, String endDate) throws SQLException {
        SqliteTripReader reader = createReader(fileName);
        insertTrip(id, title, startDate, endDate, null, null);
        return reader;
    }

    private void insertTrip(UUID id, String title, String startDate,
                            String endDate, Integer rating, String reviewText) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO trips (id, title, start_date, end_date, review_rating, review_text) "
                             + "VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, id.toString());
            statement.setString(2, title);
            statement.setString(3, startDate);
            statement.setString(4, endDate);
            if (rating == null) {
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setInt(5, rating);
            }
            statement.setString(6, reviewText);
            statement.executeUpdate();
        }
    }

    private void insertTripIgnoringChecks(UUID id, String title, String startDate,
                                          String endDate) throws SQLException {
        try (Connection connection = database.openConnection();
             Statement pragmaStatement = connection.createStatement();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO trips (id, title, start_date, end_date) VALUES (?, ?, ?, ?)")) {
            pragmaStatement.execute("PRAGMA ignore_check_constraints = ON");
            statement.setString(1, id.toString());
            statement.setString(2, title);
            statement.setString(3, startDate);
            statement.setString(4, endDate);
            statement.executeUpdate();
        }
    }

    private void insertPlan(UUID id, UUID tripId, String destination,
                            String date, String time, Integer rating, String reviewText)
            throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO plans (id, trip_id, destination, plan_date, plan_time, review_rating, "
                             + "review_text) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, id.toString());
            statement.setString(2, tripId.toString());
            statement.setString(3, destination);
            statement.setString(4, date);
            statement.setString(5, time);
            if (rating == null) {
                statement.setNull(6, java.sql.Types.INTEGER);
            } else {
                statement.setInt(6, rating);
            }
            statement.setString(7, reviewText);
            statement.executeUpdate();
        }
    }
}
