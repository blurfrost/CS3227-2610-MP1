import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class SelectTripCommandTest {
    @Test
    void execute_repositoryOrderChangesAfterDisplay_selectsDisplayedTrip() {
        DoggoService service = new DoggoService(new InMemoryTripRepository());
        service.createTrip("Japan", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2));
        service.createTrip("Korea", LocalDate.of(2028, 1, 1), LocalDate.of(2028, 1, 2));
        CliContext context = createContext(service);

        context.organiseMenu();
        service.createTrip("Earlier", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));

        CommandResult result = new SelectTripCommand(1).execute(context);

        assertTrue(result.message().startsWith("Viewing: Japan"));
    }

    private static CliContext createContext(DoggoService service) {
        StringWriter output = new StringWriter();
        return new CliContext(service, new CliSession(),
                new CliPrompter(new BufferedReader(new StringReader("")), new PrintWriter(output)),
                new CliFormatter(), new PrintWriter(output));
    }
}
