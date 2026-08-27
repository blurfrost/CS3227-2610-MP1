import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void blankTripName_repromptsUntilValidName() {
        String input = String.join("\n", "organise", "new", "", "   ", "Japan trip",
                "01/01/2027", "09/01/2027", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip title cannot be blank."));
        assertTrue(output.contains("Trip successfully added!"));
        assertTrue(output.contains("Japan trip (from 01/01/2027 to 09/01/2027)"));
    }

    @Test
    void viewTripAndCreatePlan_displaysPlanAndReturnsToMenus() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Mount Fuji", "05/01/2027", "09:00", "back",
                "back", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Viewing: Japan trip (from 01/01/2027 to 09/01/2027)"));
        assertTrue(output.contains("Plan created!"));
        assertTrue(output.contains("Mount Fuji (05/01/2027 at 09:00)"));
        assertTrue(output.contains("Type \"back\" to go back to the Organise Menu."));
    }

    @Test
    void blankPlanDestination_repromptsUntilValidDestination() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "", "   ", "Tokyo", "05/01/2027", "09:00",
                "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Plan destination cannot be blank."));
        assertTrue(output.contains("Plan created!"));
        assertTrue(output.contains("Tokyo (05/01/2027 at 09:00)"));
    }

    @Test
    void viewTrip_invalidIndex_displaysError() {
        String input = String.join("\n", "organise", "view 2", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip index must refer to a listed Trip."));
    }

    @Test
    void deleteTrip_confirmed_removesTripAndPlans() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Mount Fuji", "05/01/2027", "09:00",
                "back", "delete 1", "yes", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip deleted."));
        assertTrue(output.contains("There are no Trips planned."));
        int deletionResult = output.indexOf("Trip deleted.");
        assertFalse(output.substring(deletionResult)
                .contains("Mount Fuji (05/01/2027 at 09:00)"));
    }

    @Test
    void deleteTrip_cancelled_keepsTripAndPlans() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Mount Fuji", "05/01/2027", "09:00",
                "back", "delete 1", "no", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip deletion cancelled."));
        assertTrue(output.contains("Japan trip (from 01/01/2027 to 09/01/2027)"));
        assertTrue(output.contains("Mount Fuji (05/01/2027 at 09:00)"));
    }

    @Test
    void deletePlan_confirmed_removesOnlySelectedPlan() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Mount Fuji", "05/01/2027", "09:00",
                "new", "Osaka", "06/01/2027", "09:00", "delete 1", "yes", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Plan deleted."));
        int deletionResult = output.indexOf("Plan deleted.");
        String refreshedView = output.substring(deletionResult);
        assertFalse(refreshedView.contains("Mount Fuji (05/01/2027 at 09:00)"));
        assertTrue(refreshedView.contains("Osaka (06/01/2027 at 09:00)"));
    }

    @Test
    void editTrip_updatesFieldsAndRefreshesList() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "edit 1", "Korea trip", "02/01/2027", "10/01/2027", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip updated."));
        assertTrue(output.contains("Korea trip (from 02/01/2027 to 10/01/2027)"));
    }

    @Test
    void editTrip_blankFields_preservesExistingValues() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "edit 1", "   ", "", "  ", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("No changes made."));
        assertTrue(output.contains("Japan trip (from 01/01/2027 to 09/01/2027)"));
    }

    @Test
    void editPlan_updatesOnlySelectedPlan() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Tokyo", "05/01/2027", "09:00", "new",
                "Osaka", "06/01/2027", "09:00", "edit 1", "Kyoto", "07/01/2027", "10:00",
                "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Plan updated."));
        assertTrue(output.contains("Kyoto (07/01/2027 at 10:00)"));
        assertTrue(output.contains("Osaka (06/01/2027 at 09:00)"));
    }

    @Test
    void editPlan_blankDestination_preservesExistingDestination() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Tokyo", "05/01/2027", "09:00", "edit 1",
                "", "", "", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("No changes made."));
        assertTrue(output.contains("Tokyo (05/01/2027 at 09:00)"));
    }

    @Test
    void editTrip_startBeforePlanDate_repromptsStartBeforeEnd() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Tokyo", "05/01/2027", "09:00", "back",
                "edit 1", "Korea", "06/01/2027", "01/01/2027", "10/01/2027",
                "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip start date cannot be after an existing Plan date."));
        assertTrue(output.contains("Korea (from 01/01/2027 to 10/01/2027)"));
    }

    @Test
    void editTrip_startAfterCurrentEnd_repromptsStartOnly() {
        String input = String.join("\n", "organise", "new", "Japan", "01/01/2027",
                "09/01/2027", "edit 1", "Korea", "10/01/2027", "02/01/2027", "", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip start date cannot be after its current end date."));
        assertTrue(output.contains("Korea (from 02/01/2027 to 09/01/2027)"));
    }

    @Test
    void editTrip_startAfterPlanDate_repromptsStartOnly() {
        String input = String.join("\n", "organise", "new", "Japan", "01/01/2027",
                "09/01/2027", "view 1", "new", "Tokyo", "05/01/2027", "09:00", "back",
                "edit 1", "Korea", "06/01/2027", "02/01/2027", "", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip start date cannot be after an existing Plan date."));
        assertTrue(output.contains("Korea (from 02/01/2027 to 09/01/2027)"));
    }

    @Test
    void editTrip_endBeforeProposedStart_repromptsEndOnly() {
        String input = String.join("\n", "organise", "new", "Japan", "01/01/2027",
                "09/01/2027", "edit 1", "Korea", "03/01/2027", "02/01/2027", "", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip end date cannot be before its start date."));
        assertTrue(output.contains("Korea (from 03/01/2027 to 09/01/2027)"));
    }

    @Test
    void editTrip_endBeforePlanDate_repromptsEndOnly() {
        String input = String.join("\n", "organise", "new", "Japan", "01/01/2027",
                "09/01/2027", "view 1", "new", "Tokyo", "05/01/2027", "09:00", "back",
                "edit 1", "Korea", "", "04/01/2027", "", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Plan date must fall within the Trip dates."));
        assertTrue(output.contains("Korea (from 01/01/2027 to 09/01/2027)"));
    }

    @Test
    void malformedEditAndDelete_showUsageAndCurrentView() {
        String input = String.join("\n", "organise", "edit", "delete 0", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Usage: edit NUMBER"));
        assertTrue(output.contains("Usage: delete NUMBER"));
        assertTrue(output.contains("[MODE: ORGANISE]"));
    }

    @Test
    void bareTripIndex_isRejected() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "1", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Unknown command \"1\""));
    }

    @Test
    void createPlan_invalidDateAndTime_reprompts() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "back", "back", "05/01/2027", "back", "09:00",
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
                "09/01/2027", "view 1", "new", "Mount Fuji", "10/01/2027", "05/01/2027",
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
