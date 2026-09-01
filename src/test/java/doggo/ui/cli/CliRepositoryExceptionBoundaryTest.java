package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.application.TripRepository;
import doggo.domain.Trip;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class CliRepositoryExceptionBoundaryTest {
    private static final String INFRASTRUCTURE_DETAILS = "SQLITE_FAILURE_DETAILS";
    private static final String GENERIC_ERROR =
            "Error: Stored data could not be accessed. Existing data was not changed.";

    @Test
    void run_listReadFails_reportsGenericErrorAndRemainsAlive() {
        ConfigurableTripRepository repository = new ConfigurableTripRepository();
        repository.failOnceOn(FailingOperation.FIND_ALL);

        String output = runCli(repository, "organise\nview 1\nexit\n");

        assertRepositoryFailureIsRecoverable(output);
        assertEquals(1, repository.failureCount());
        assertTrue(output.contains("There are no trips to view."));
        assertFalse(output.contains("Unknown command \"view 1\"."));
    }

    @Test
    void run_tripSaveFailsAfterPrompts_reportsGenericErrorAndPreservesData() {
        ConfigurableTripRepository repository = new ConfigurableTripRepository();
        repository.failOnceOn(FailingOperation.SAVE);

        String output = runCli(repository, String.join("\n", "new", "Japan", "01/01/2027",
                "04/01/2027", "exit") + "\n");

        assertRepositoryFailureIsRecoverable(output);
        assertEquals(1, repository.failureCount());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void run_confirmedTripDeleteFails_reportsGenericErrorAndPreservesData() {
        ConfigurableTripRepository repository = new ConfigurableTripRepository();
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        repository.save(trip);
        repository.failOnceOn(FailingOperation.DELETE);

        String output = runCli(repository, "organise\ndelete 1\nyes\nexit\n");

        assertRepositoryFailureIsRecoverable(output);
        assertEquals(1, repository.failureCount());
        assertEquals(Optional.of(trip), repository.findById(trip.id()));
    }

    private static String runCli(ConfigurableTripRepository repository, String input) {
        StringWriter output = new StringWriter();
        Cli cli = new Cli(new BufferedReader(new StringReader(input)), new PrintWriter(output),
                new DoggoService(repository, TestClock.fixed()));
        cli.run();
        return output.toString();
    }

    private static void assertRepositoryFailureIsRecoverable(String output) {
        assertTrue(output.contains(GENERIC_ERROR));
        assertFalse(output.contains(INFRASTRUCTURE_DETAILS));
        assertTrue(output.endsWith("Bye!" + System.lineSeparator()));
    }

    private enum FailingOperation {
        FIND_ALL,
        SAVE,
        DELETE
    }

    private static final class ConfigurableTripRepository implements TripRepository {
        private final InMemoryTripRepository delegate = new InMemoryTripRepository();
        private FailingOperation failingOperation;
        private int failureCount;

        @Override
        public List<Trip> findAll() {
            failIfConfigured(FailingOperation.FIND_ALL);
            return delegate.findAll();
        }

        @Override
        public Optional<Trip> findById(UUID id) {
            return delegate.findById(id);
        }

        @Override
        public void save(Trip trip) {
            failIfConfigured(FailingOperation.SAVE);
            delegate.save(trip);
        }

        @Override
        public void delete(UUID id) {
            failIfConfigured(FailingOperation.DELETE);
            delegate.delete(id);
        }

        void failOnceOn(FailingOperation operation) {
            failingOperation = operation;
        }

        int failureCount() {
            return failureCount;
        }

        private void failIfConfigured(FailingOperation operation) {
            if (failingOperation == operation) {
                failingOperation = null;
                failureCount++;
                throw new RepositoryException(INFRASTRUCTURE_DETAILS);
            }
        }
    }
}
