package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import doggo.TestClock;
import doggo.application.DoggoService;
import doggo.storage.InMemoryTripRepository;

import org.junit.jupiter.api.Test;

class CliTest {
    @Test
    void galleryNavigation_viewsPastTripAndReturnsToMain() {
        String input = String.join("\n", "new", "Japan", "01/01/2027", "04/01/2027",
                "back", "gallery", "view 1", "back", "back", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("[MODE: GALLERY]"));
        assertTrue(output.contains("1. Japan (from 01/01/2027 to 04/01/2027)"));
        assertTrue(output.contains("Viewing past Trip: Japan"));
        assertTrue(output.contains("Type \"new\" to create a new Plan."));
        assertTrue(output.contains("Type \"back\" to go back to the Gallery."));
        assertTrue(output.contains("Welcome! Available commands are"));
        assertTrue(output.endsWith("Bye!\n"));
    }

    @Test
    void galleryAndOrganise_partitionTripsAtInclusiveDateBoundary() {
        String input = String.join("\n", "new", "Past", "01/01/2027", "04/01/2027",
                "back", "new", "Current", "05/01/2027", "05/01/2027", "back",
                "gallery", "exit") + "\n";
        String output = runCli(input);
        int galleryStart = output.lastIndexOf("[MODE: GALLERY]");
        String galleryOutput = output.substring(galleryStart);

        assertTrue(output.contains("[MODE: ORGANISE]"));
        assertTrue(output.contains("Current (from 05/01/2027 to 05/01/2027)"));
        assertTrue(galleryOutput.contains("Past (from 01/01/2027 to 04/01/2027)"));
        assertFalse(galleryOutput.contains("Current"));
    }

    @Test
    void galleryMaintenance_endToEnd_updatesPlansAndCancelsTripDeletion() {
        String input = String.join("\n", "new", "Past trip", "01/01/2027", "04/01/2027",
                "edit 1", "Historical trip", "", "", "view 1", "new", "Museum",
                "02/01/2027", "09:00", "new", "Dinner", "03/01/2027", "19:00", "edit 1",
                "Museum updated", "02/01/2027", "10:00", "new", "Park", "01/01/2027",
                "08:00", "delete 2", "yes", "back", "delete 1", "no", "exit") + "\n";
        String output = runCli(input);
        int planDeletionMarker = output.indexOf("Plan deleted.");
        String afterPlanDeletion = output.substring(planDeletionMarker);

        assertTrue(output.contains("Trip updated."));
        assertTrue(output.contains("Plan created!"));
        assertTrue(output.contains("Plan updated."));
        assertTrue(output.contains("Plan deleted."));
        assertTrue(output.contains("Trip deletion cancelled."));
        assertTrue(afterPlanDeletion.contains("1. Park (01/01/2027 at 08:00)"));
        assertTrue(afterPlanDeletion.contains("2. Dinner (03/01/2027 at 19:00)"));
        assertFalse(afterPlanDeletion.contains("Museum updated (02/01/2027 at 10:00)"));
        assertTrue(output.endsWith("Bye!\n"));
    }

    @Test
    void galleryReview_endToEnd_addsEditsRemovesTripReviewAndKeepsPlan() {
        String input = String.join("\n", "new", "Past trip", "01/01/2027", "04/01/2027",
                "view 1", "new", "Museum", "02/01/2027", "09:00", "back", "review 1",
                "5", "Wonderful journey", "review 1", "", "Updated journey", "review 1",
                "-", "-", "view 1", "exit") + "\n";
        String output = runCli(input);
        String afterRemoval = output.substring(output.lastIndexOf("Review removed."));

        assertTrue(output.contains("Plan created!"));
        assertTrue(output.contains("Review added."));
        assertTrue(output.contains("Rating: 5/5"));
        assertTrue(output.contains("Review: Wonderful journey"));
        assertTrue(output.contains("Review updated."));
        assertTrue(output.contains("Review: Updated journey"));
        assertTrue(output.contains("Review removed."));
        assertTrue(afterRemoval.contains("Museum (02/01/2027 at 09:00)"));
        assertFalse(afterRemoval.contains("Rating: 5/5"));
        assertFalse(afterRemoval.contains("Review: Updated journey"));
    }

    @Test
    void organiseReview_endToEnd_addsEditsRemovesTripReviewAndStaysInOrganise() {
        String input = String.join("\n", "organise", "new", "Current trip", "05/01/2027",
                "09/01/2027", "review 1", "5", "Upcoming journey", "review 1", "",
                "Updated journey", "review 1", "-", "-", "exit") + "\n";
        String output = runCli(input);
        String afterRemoval = output.substring(output.lastIndexOf("Review removed."));

        assertTrue(output.contains("Review added."));
        assertTrue(output.contains("Rating: 5/5"));
        assertTrue(output.contains("Review: Upcoming journey"));
        assertTrue(output.contains("Review updated."));
        assertTrue(output.contains("Review: Updated journey"));
        assertTrue(output.contains("Review removed."));
        assertTrue(afterRemoval.contains("[MODE: ORGANISE]"));
        assertFalse(afterRemoval.contains("Review: Updated journey"));
        assertTrue(output.endsWith("Bye!\n"));
    }

    @Test
    void dashboardReview_endToEnd_rendersInSelectedTripView() {
        String input = String.join("\n", "organise", "new", "Current trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Early plan", "05/01/2027", "00:00", "back",
                "back", "dashboard", "review 1", "4", "Excellent plan", "back", "organise",
                "view 1", "exit") + "\n";
        String output = runCli(input);
        int selectedTripView = output.lastIndexOf("Viewing: Current trip");
        String selectedTripOutput = output.substring(selectedTripView);

        assertTrue(output.contains("Review added."));
        assertTrue(output.contains("1. 00:00 - Early plan (Trip: Current trip)"));
        assertTrue(selectedTripOutput.contains("Early plan (05/01/2027 at 00:00)"));
        assertTrue(selectedTripOutput.contains("Rating: 4/5"));
        assertTrue(selectedTripOutput.contains("Review: Excellent plan"));
    }

    @Test
    void selectedTripPlanReview_endToEnd_supportsOrganiseAndGallery() {
        String input = String.join("\n", "organise", "new", "Current trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Tokyo", "04/01/2027", "09:00", "review 1",
                "5", "Organise plan", "back", "back", "new", "Past trip", "01/01/2027",
                "04/01/2027", "view 1", "new", "Seoul", "02/01/2027", "09:00", "review 1",
                "4", "Gallery plan", "back", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Tokyo (04/01/2027 at 09:00)"));
        assertTrue(output.contains("Rating: 5/5"));
        assertTrue(output.contains("Review: Organise plan"));
        assertTrue(output.contains("Seoul (02/01/2027 at 09:00)"));
        assertTrue(output.contains("Rating: 4/5"));
        assertTrue(output.contains("Review: Gallery plan"));
        assertTrue(output.endsWith("Bye!\n"));
    }

    @Test
    void dashboardNavigation_returnsToMain() {
        String input = String.join("\n", "DaShBoArD", "back", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("[MODE: DASHBOARD]"));
        assertTrue(output.contains("There are no Plans scheduled for today."));
        assertTrue(output.contains("Welcome! Available commands are"));
        assertTrue(output.endsWith("Bye!\n"));
    }

    @Test
    void dashboard_displaysPlansScheduledForCurrentDate() {
        String input = String.join("\n", "organise", "new", "Japan", "05/01/2027",
                "05/01/2027", "view 1", "new", "Tokyo", "05/01/2027", "09:00", "back",
                "back", "dashboard", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Today's itinerary:"));
        assertTrue(output.contains("1. 09:00 - Tokyo (Trip: Japan)"));
    }

    @Test
    void editPlanFromDashboard_updatesOwningTripAndOrganiseView() {
        String input = String.join("\n", "organise", "new", "Japan", "01/01/2027",
                "09/01/2027", "view 1", "new", "Tokyo", "05/01/2027", "09:00", "back",
                "back", "dashboard", "edit 1", "Kyoto", "05/01/2027", "10:00", "back",
                "organise", "view 1", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Plan updated."));
        assertTrue(output.contains("1. 10:00 - Kyoto (Trip: Japan)"));
        assertTrue(output.contains("Kyoto (05/01/2027 at 10:00)"));
    }

    @Test
    void createTripAndPlanFlow_displaysCreatedRecords() {
        String input = String.join("\n", "organise", "new", "Japan trip", "01/01/2027",
                "09/01/2027", "view 1", "new", "Mount Fuji", "05/01/2027", "09:00",
                "back", "back", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip successfully added!"));
        assertTrue(output.contains("Viewing: Japan trip (from 01/01/2027 to 09/01/2027)"));
        assertTrue(output.contains("Plan created!"));
        assertTrue(output.contains("Mount Fuji (05/01/2027 at 09:00)"));
        assertTrue(output.endsWith("Bye!\n"));
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
    void invalidInput_repromptsAndKeepsCliRunning() {
        String input = String.join("\n", "organise", "new", "", "Japan trip", "2027-01-01",
                "01/01/2027", "09/01/2027", "view abc", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Trip title cannot be blank."));
        assertTrue(output.contains("Date must use the DD/MM/YYYY format"));
        assertTrue(output.contains("Trip successfully added!"));
        assertTrue(output.contains("Trip number should be 1."));
        assertTrue(output.endsWith("Bye!\n"));
    }

    @Test
    void unknownDashboardCommand_keepsDashboardView() {
        String input = String.join("\n", "dashboard", "unknown", "exit") + "\n";
        String output = runCli(input);

        assertTrue(output.contains("Error: Unknown command \"unknown\"."));
        assertTrue(output.contains("[MODE: DASHBOARD]"));
        assertFalse(output.contains("[MODE: ORGANISE]"));
    }

    private static String runCli(String input) {
        StringWriter output = new StringWriter();
        Cli cli = new Cli(new BufferedReader(new StringReader(input)), new PrintWriter(output),
                new DoggoService(new InMemoryTripRepository(), TestClock.fixed()));
        cli.run();
        return output.toString();
    }
}
