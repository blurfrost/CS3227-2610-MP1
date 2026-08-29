package doggo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Clock;

import doggo.application.DoggoService;
import doggo.application.RepositoryException;
import doggo.storage.SqliteTripRepository;
import doggo.ui.cli.Cli;

/**
 * Starts the doggo command-line application.
 */
public final class Doggo {
    private static final Path DEFAULT_DATABASE_PATH = Path.of("data", "doggo.db");

    private Doggo() {
    }

    /**
     * Starts a doggo CLI session backed by the default SQLite database.
     *
     * @param args Command-line arguments, which are currently ignored.
     */
    public static void main(String[] args) {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter output = new PrintWriter(System.out, true);
        PrintWriter error = new PrintWriter(System.err, true);
        run(DEFAULT_DATABASE_PATH, input, output, error, Clock.systemDefaultZone());
    }

    /**
     * Starts a doggo CLI session with the specified application dependencies.
     *
     * @param databasePath SQLite database file path.
     * @param input CLI input source.
     * @param output CLI output destination.
     * @param error Startup-error destination.
     * @param clock Clock used for date-sensitive behavior.
     */
    static void run(Path databasePath, BufferedReader input, PrintWriter output,
                    PrintWriter error, Clock clock) {
        DoggoService service;
        try {
            service = new DoggoService(new SqliteTripRepository(databasePath), clock);
        } catch (RepositoryException exception) {
            error.println("Unable to start doggo. Check that the database location is accessible.");
            error.flush();
            return;
        }
        new Cli(input, output, service).run();
    }
}
