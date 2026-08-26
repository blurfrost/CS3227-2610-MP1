import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
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

    @Test
    void selectTripAndCreatePlan_displaysPlanAndReturnsToMenus() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "1", "new", "Mount Fuji", "05/01/2027", "09:00", "back",
                "back", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Viewing: Japan trip (from 01/01/2027 to 09/01/2027)"));
        assertTrue(output.contains("Plan created!"));
        assertTrue(output.contains("Mount Fuji (05/01/2027 at 09:00)"));
        assertTrue(output.contains("Type \"back\" to go back to the Organise Menu."));
    }

    @Test
    void selectTrip_invalidIndex_displaysError() {
        String input = String.join("\n", "organise", "2", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip index must refer to a listed Trip."));
    }

    @Test
    void createPlan_invalidDateAndTime_reprompts() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "1", "new", "back", "back", "05/01/2027", "back", "09:00",
                "exit")
                + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Date must use the DD/MM/YYYY format"));
        assertTrue(output.contains("Time must use the HH:mm format"));
        assertTrue(output.contains("1. back (05/01/2027 at 09:00)"));
    }

    @Test
    void createPlan_dateOutsideTrip_repromptsBeforeTime() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "1", "new", "Mount Fuji", "10/01/2027", "05/01/2027",
                "09:00", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Plan date must fall within the Trip dates."));
        assertTrue(output.contains("Plan created!"));
        assertTrue(output.contains("Mount Fuji (05/01/2027 at 09:00)"));
    }

    private static String runCli(String input) {
        StringWriter output = new StringWriter();
        Cli cli = new Cli(new BufferedReader(new StringReader(input)), new PrintWriter(output),
                new DoggoService(new InMemoryTripRepository()));
        cli.run();
        return output.toString();
    }
}
