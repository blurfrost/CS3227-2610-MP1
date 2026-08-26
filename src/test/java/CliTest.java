import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

class CliTest {
    @Test
    void createTripFlow_displaysTripAndExits() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "back", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip successfully added!"));
        assertTrue(output.contains("Japan trip (from 01/01/2027 to 09/01/2027)"));
        assertTrue(output.endsWith("Bye!\n"));
    }

    @Test
    void invalidTripDate_repromptsAndDoesNotCrash() {
        String input = String.join("\n", "organise", "new", "Japan trip", "2027-01-01",
                "01/01/2027", "09/01/2027", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Date must use the DD/MM/YYYY format"));
        assertTrue(output.contains("Trip successfully added!"));
    }

    private static String runCli(String input) {
        StringWriter output = new StringWriter();
        Cli cli = new Cli(new BufferedReader(new StringReader(input)), new PrintWriter(output),
                new DoggoService(new InMemoryTripRepository()));
        cli.run();
        return output.toString();
    }
}
