package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import doggo.application.DashboardEntry;
import doggo.domain.Plan;
import doggo.domain.Trip;

import org.junit.jupiter.api.Test;

class CliFormatterTest {
    @Test
    void tripView_planTimeIncludesSeconds_displaysHoursAndMinutesOnly() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 9));
        Plan plan = new Plan(UUID.randomUUID(), "Mount Fuji", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0, 30));

        String output = new CliFormatter().tripView(trip, List.of(plan));

        assertTrue(output.contains("Mount Fuji (05/01/2027 at 09:00)"));
        assertFalse(output.contains("09:00:30"));
    }

    @Test
    void dashboardMenu_emptyEntries_displaysEmptyState() {
        String output = new CliFormatter().dashboardMenu(List.of());

        assertTrue(output.contains("[MODE: DASHBOARD]"));
        assertTrue(output.contains("There are no Plans scheduled for today."));
        assertTrue(output.contains("Type \"new\" to create a new Trip."));
        assertTrue(output.contains("Type \"back\" to go back to the Main Menu."));
    }

    @Test
    void dashboardMenu_entries_displaysTimeDestinationAndTrip() {
        Plan plan = new Plan(UUID.randomUUID(), "Tokyo", LocalDate.of(2027, 1, 5),
                LocalTime.of(9, 0));
        DashboardEntry entry = new DashboardEntry(UUID.randomUUID(), "Japan", plan);

        String output = new CliFormatter().dashboardMenu(List.of(entry));

        assertTrue(output.contains("Today's itinerary:"));
        assertTrue(output.contains("1. 09:00 - Tokyo (Trip: Japan)"));
        assertTrue(output.contains("Edit a Plan with \"edit NUMBER\"."));
        assertTrue(output.contains("Delete a Plan with \"delete NUMBER\"."));
    }

    @Test
    void galleryMenu_pastTrips_displaysTripCreationAndReadOnlyNavigation() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));

        String output = new CliFormatter().galleryMenu(List.of(trip));

        assertTrue(output.contains("[MODE: GALLERY]"));
        assertTrue(output.contains("1. Japan (from 01/01/2027 to 04/01/2027)"));
        assertTrue(output.contains("View a past Trip with \"view NUMBER\"."));
        assertTrue(output.contains("Type \"new\" to create a new Trip."));
        assertFalse(output.contains("edit NUMBER"));
        assertFalse(output.contains("delete NUMBER"));
    }

    @Test
    void galleryTripView_plans_displaysReadOnlyItinerary() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan plan = new Plan(UUID.randomUUID(), "Tokyo", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));

        String output = new CliFormatter().galleryTripView(trip, List.of(plan));

        assertTrue(output.contains("Viewing past Trip: Japan"));
        assertTrue(output.contains("Tokyo (02/01/2027 at 09:00)"));
        assertTrue(output.contains("Type \"back\" to go back to the Gallery."));
        assertFalse(output.contains("create a new Plan"));
        assertFalse(output.contains("edit NUMBER"));
    }
}
