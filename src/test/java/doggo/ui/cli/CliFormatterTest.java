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
    }
}
