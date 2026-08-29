package doggo.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import doggo.application.RepositoryException;
import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteTripRepositoryTest {
    private static final UUID TRIP_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID FIRST_PLAN_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");
    private static final UUID SECOND_PLAN_ID = UUID.fromString("00000000-0000-0000-0000-000000000112");

    @TempDir
    Path temporaryDirectory;

    @Test
    void findAll_emptyDatabase_returnsEmptyList() {
        SqliteTripRepository repository = createRepository("empty.db");

        assertEquals(List.of(), repository.findAll());
    }

    @Test
    void findById_missingId_returnsEmptyOptional() {
        SqliteTripRepository repository = createRepository("missing.db");

        assertEquals(Optional.empty(), repository.findById(TRIP_ID));
    }

    @Test
    void save_completeAggregate_roundTripsAllFields() {
        SqliteTripRepository repository = createRepository("round-trip.db");
        Trip expected = completeTrip();

        repository.save(expected);

        assertTripEquals(expected, repository.findById(TRIP_ID).orElseThrow());
    }

    @Test
    void save_existingAggregate_replacesPlansAndReviewFields() {
        SqliteTripRepository repository = createRepository("replace.db");
        repository.save(completeTrip());
        Trip replacement = Trip.restore(
                TRIP_ID,
                "Updated Japan",
                LocalDate.of(2027, 1, 2),
                LocalDate.of(2027, 1, 8),
                List.of(new Plan(SECOND_PLAN_ID, "Kyoto", LocalDate.of(2027, 1, 4),
                        LocalTime.of(14, 30))),
                Optional.of(new Review(OptionalInt.of(2), Optional.empty())));

        repository.save(replacement);

        assertTripEquals(replacement, repository.findById(TRIP_ID).orElseThrow());
    }

    @Test
    void delete_existingTrip_cascadesToPlans() throws SQLException {
        Path databasePath = temporaryDirectory.resolve("delete.db");
        SqliteTripRepository repository = new SqliteTripRepository(databasePath);
        repository.save(completeTrip());

        repository.delete(TRIP_ID);

        assertTrue(repository.findById(TRIP_ID).isEmpty());
        try (Connection connection = new SqliteDatabase(databasePath).openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM plans")) {
            assertTrue(resultSet.next());
            assertEquals(0, resultSet.getInt(1));
        }
    }

    @Test
    void delete_missingTrip_leavesRepositoryUnchanged() {
        SqliteTripRepository repository = createRepository("missing-delete.db");
        Trip expected = completeTrip();
        repository.save(expected);

        repository.delete(UUID.fromString("00000000-0000-0000-0000-000000000199"));

        assertTripEquals(expected, repository.findById(TRIP_ID).orElseThrow());
    }

    @Test
    void save_thenNewRepository_readsPersistedAggregate() {
        Path databasePath = temporaryDirectory.resolve("reopen.db");
        Trip expected = completeTrip();
        new SqliteTripRepository(databasePath).save(expected);

        SqliteTripRepository reopenedRepository = new SqliteTripRepository(databasePath);

        assertTripEquals(expected, reopenedRepository.findById(TRIP_ID).orElseThrow());
    }

    @Test
    void save_failedPlanInsert_rollsBackRootPlansAndReviews() throws SQLException {
        Path databasePath = temporaryDirectory.resolve("rollback.db");
        SqliteTripRepository repository = new SqliteTripRepository(databasePath);
        Trip expected = completeTrip();
        repository.save(expected);
        installFailingPlanTrigger(databasePath);
        Trip replacement = Trip.restore(
                TRIP_ID,
                "Replaced title",
                LocalDate.of(2027, 1, 2),
                LocalDate.of(2027, 1, 8),
                List.of(
                        new Plan(FIRST_PLAN_ID, "Valid replacement", LocalDate.of(2027, 1, 3),
                                LocalTime.of(8, 0)),
                        new Plan(SECOND_PLAN_ID, "Reject this Plan", LocalDate.of(2027, 1, 4),
                                LocalTime.of(9, 0))),
                Optional.of(new Review(OptionalInt.of(1), Optional.of("Should roll back"))));

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> repository.save(replacement));

        assertInstanceOf(SQLException.class, exception.getCause());
        assertTripEquals(expected, repository.findById(TRIP_ID).orElseThrow());
        SqliteTripRepository reopenedRepository = new SqliteTripRepository(databasePath);
        assertTripEquals(expected, reopenedRepository.findById(TRIP_ID).orElseThrow());
    }

    @Test
    void repository_nullInputs_rejectWithNullPointerException() {
        SqliteTripRepository repository = createRepository("null-input.db");

        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.delete(null));
    }

    private SqliteTripRepository createRepository(String fileName) {
        return new SqliteTripRepository(temporaryDirectory.resolve(fileName));
    }

    private static Trip completeTrip() {
        Plan firstPlan = Plan.restore(
                FIRST_PLAN_ID,
                "Mount Fuji",
                LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0),
                Optional.of(new Review(OptionalInt.of(4), Optional.empty())));
        Plan secondPlan = Plan.restore(
                SECOND_PLAN_ID,
                "Tokyo",
                LocalDate.of(2027, 1, 6),
                LocalTime.of(10, 30),
                Optional.of(new Review(OptionalInt.empty(), Optional.of("Great food"))));
        return Trip.restore(
                TRIP_ID,
                "Japan",
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9),
                List.of(firstPlan, secondPlan),
                Optional.of(new Review(OptionalInt.of(5), Optional.of("Excellent journey"))));
    }

    private static void assertTripEquals(Trip expected, Trip actual) {
        assertEquals(expected.id(), actual.id());
        assertEquals(expected.title(), actual.title());
        assertEquals(expected.startDate(), actual.startDate());
        assertEquals(expected.endDate(), actual.endDate());
        assertEquals(expected.review(), actual.review());
        assertEquals(expected.plans().size(), actual.plans().size());
        for (int index = 0; index < expected.plans().size(); index++) {
            assertPlanEquals(expected.plans().get(index), actual.plans().get(index));
        }
    }

    private static void assertPlanEquals(Plan expected, Plan actual) {
        assertEquals(expected.id(), actual.id());
        assertEquals(expected.destination(), actual.destination());
        assertEquals(expected.date(), actual.date());
        assertEquals(expected.time(), actual.time());
        assertEquals(expected.review(), actual.review());
    }

    private static void installFailingPlanTrigger(Path databasePath) throws SQLException {
        try (Connection connection = new SqliteDatabase(databasePath).openConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TRIGGER reject_plan_insert
                    BEFORE INSERT ON plans
                    WHEN NEW.destination = 'Reject this Plan'
                    BEGIN
                        SELECT RAISE(ABORT, 'forced Plan insert failure');
                    END
                    """);
        }
    }
}
