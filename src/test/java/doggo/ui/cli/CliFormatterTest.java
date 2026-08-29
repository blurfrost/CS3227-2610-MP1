package doggo.ui.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import doggo.application.DashboardEntry;
import doggo.domain.Plan;
import doggo.domain.Review;
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
    void galleryMenu_pastTrips_displaysTripCreationNavigationAndEditing() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));

        String output = new CliFormatter().galleryMenu(List.of(trip));

        assertTrue(output.contains("[MODE: GALLERY]"));
        assertTrue(output.contains("1. Japan (from 01/01/2027 to 04/01/2027)"));
        assertTrue(output.contains("View a past Trip with \"view NUMBER\"."));
        assertTrue(output.contains("Type \"new\" to create a new Trip."));
        assertTrue(output.contains("Edit a past Trip with \"edit NUMBER\"."));
        assertTrue(output.contains("Delete a past Trip with \"delete NUMBER\"."));
        assertTrue(output.contains("Review a past Trip with \"review NUMBER\"."));
    }

    @Test
    void galleryMenu_emptyTrips_doesNotAdvertiseDeletion() {
        String output = new CliFormatter().galleryMenu(List.of());

        assertTrue(output.contains("There are no past Trips."));
        assertFalse(output.contains("delete NUMBER"));
    }

    @Test
    void galleryTripView_plans_advertisesPlanEditing() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));
        Plan plan = new Plan(UUID.randomUUID(), "Tokyo", LocalDate.of(2027, 1, 2),
                LocalTime.of(9, 0));

        String output = new CliFormatter().galleryTripView(trip, List.of(plan));

        assertTrue(output.contains("Viewing past Trip: Japan"));
        assertTrue(output.contains("Tokyo (02/01/2027 at 09:00)"));
        assertTrue(output.contains("Type \"new\" to create a new Plan."));
        assertTrue(output.contains("Type \"back\" to go back to the Gallery."));
        assertTrue(output.contains("Type \"edit NUMBER\" to edit a Plan."));
        assertTrue(output.contains("Type \"delete NUMBER\" to delete a Plan."));
    }

    @Test
    void galleryTripView_withoutPlans_doesNotAdvertisePlanEditing() {
        Trip trip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4));

        String output = new CliFormatter().galleryTripView(trip, List.of());

        assertFalse(output.contains("edit NUMBER"));
        assertFalse(output.contains("delete NUMBER"));
    }

    @Test
    void galleryViews_presentReviewFieldsOnIndentedLines() {
        Trip reviewedTrip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4)).withReview(
                        new Review(OptionalInt.of(5), Optional.of("Wonderful trip.")));

        String galleryOutput = new CliFormatter().galleryMenu(List.of(reviewedTrip));
        String detailOutput = new CliFormatter().galleryTripView(reviewedTrip, List.of());

        assertTrue(galleryOutput.contains("   Rating: 5/5\n"));
        assertTrue(galleryOutput.contains("   Review: Wonderful trip.\n"));
        assertTrue(detailOutput.contains("   Rating: 5/5\n"));
        assertTrue(detailOutput.contains("   Review: Wonderful trip.\n"));
    }

    @Test
    void galleryViews_omitAbsentReviewFields() {
        Trip ratingOnlyTrip = new Trip(UUID.randomUUID(), "Japan", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4)).withReview(
                        new Review(OptionalInt.of(4), Optional.empty()));
        Trip textOnlyTrip = new Trip(UUID.randomUUID(), "Korea", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 4)).withReview(
                        new Review(OptionalInt.empty(), Optional.of("Good food.")));

        String ratingOutput = new CliFormatter().galleryMenu(List.of(ratingOnlyTrip));
        String textOutput = new CliFormatter().galleryMenu(List.of(textOnlyTrip));

        assertTrue(ratingOutput.contains("   Rating: 4/5\n"));
        assertFalse(ratingOutput.contains("   Review:"));
        assertTrue(textOutput.contains("   Review: Good food.\n"));
        assertFalse(textOutput.contains("   Rating:"));
    }
}
