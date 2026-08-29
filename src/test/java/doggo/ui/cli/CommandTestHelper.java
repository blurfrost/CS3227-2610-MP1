package doggo.ui.cli;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.storage.InMemoryTripRepository;

final class CommandTestHelper {
    private CommandTestHelper() {
    }

    static CliContext context() {
        return context(service(), "");
    }

    static CliContext context(DoggoService service, String input) {
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output);
        return new CliContext(service, new CliSession(),
                new CliPrompter(new BufferedReader(new StringReader(input)), writer),
                new CliFormatter(), writer);
    }

    static DoggoService service() {
        return new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
    }
}
