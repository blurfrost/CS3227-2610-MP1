package doggo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.Clock;

import doggo.application.DoggoService;
import doggo.storage.InMemoryTripRepository;
import doggo.ui.cli.Cli;

/**
 * Starts the doggo command-line application.
 */
public final class Doggo {
    private Doggo() {
    }

    /**
     * Starts a new in-memory doggo CLI session.
     *
     * @param args Command-line arguments, which are currently ignored.
     */
    public static void main(String[] args) {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter output = new PrintWriter(System.out, true);
        DoggoService service = new DoggoService(new InMemoryTripRepository(),
                Clock.systemDefaultZone());
        new Cli(input, output, service).run();
    }
}
