package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class ViewTripCommandTest {
    @Test
    void execute_repositoryOrderChangesAfterDisplay_selectsDisplayedTrip() {
        DoggoService service = new DoggoService(new InMemoryTripRepository(), TestClock.fixed());
        service.createTrip("Japan", LocalDate.of(2027, 1, 6), LocalDate.of(2027, 1, 7));
        service.createTrip("Korea", LocalDate.of(2028, 1, 1), LocalDate.of(2028, 1, 2));
        CliContext context = createContext(service);

        context.organiseMenu();
        service.createTrip("Earlier", LocalDate.of(2027, 1, 5), LocalDate.of(2027, 1, 5));

        CommandResult result = new ViewTripCommand(1).execute(context);

        assertTrue(result.message().startsWith("Viewing: Japan"));
    }

    private static CliContext createContext(DoggoService service) {
        StringWriter output = new StringWriter();
        return new CliContext(service, new CliSession(),
                new CliPrompter(new BufferedReader(new StringReader("")), new PrintWriter(output)),
                new CliFormatter(), new PrintWriter(output));
    }
}
