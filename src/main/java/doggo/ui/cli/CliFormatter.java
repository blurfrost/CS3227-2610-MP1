package doggo.ui.cli;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import doggo.application.DashboardEntry;
import doggo.domain.Plan;
import doggo.domain.Review;
import doggo.domain.Trip;

final class CliFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    String mainMenu() {
        return "Welcome! Available commands are: \"new\", \"organise\", \"dashboard\", "
                + "\"gallery\", \"exit\"";
    }

    String dashboardMenu(List<DashboardEntry> entries) {
        StringBuilder message = new StringBuilder("[MODE: DASHBOARD]\n");
        if (entries.isEmpty()) {
            message.append("There are no Plans scheduled for today.\n\n");
        } else {
            message.append("Today's itinerary:\n");
            for (int index = 0; index < entries.size(); index++) {
                DashboardEntry entry = entries.get(index);
                message.append(index + 1)
                        .append(". ")
                        .append(TIME_FORMATTER.format(entry.plan().time()))
                        .append(" - ")
                        .append(entry.plan().destination())
                        .append(" (Trip: ")
                        .append(entry.tripTitle())
                        .append(")\n");
                entry.plan().review().ifPresent(review -> appendReview(message, review));
            }
            message.append("\n");
            message.append("Edit a Plan with \"edit NUMBER\".\n\n");
            message.append("Delete a Plan with \"delete NUMBER\".\n\n");
            message.append("Review a Plan with \"review NUMBER\".\n\n");
        }
        return message.append("Type \"new\" to create a new Trip.\n")
                .append("Type \"back\" to go back to the Main Menu.").toString();
    }

    /**
     * Formats a date for CLI prompts.
     *
     * @param date Date to format.
     * @return Formatted date.
     */
    static String formatDate(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    /**
     * Formats a time for CLI prompts.
     *
     * @param time Time to format.
     * @return Formatted time.
     */
    static String formatTime(LocalTime time) {
        return TIME_FORMATTER.format(time);
    }

    String organiseMenu(List<Trip> trips) {
        StringBuilder message = new StringBuilder("[MODE: ORGANISE]\n");
        if (trips.isEmpty()) {
            message.append("There are no Trips planned.\n\n");
        } else {
            message.append("Here are your trips:\n");
            appendTrips(message, trips);
            message.append("\n");
            message.append("View a trip with \"view NUMBER\".\n\n");
            message.append("Edit a trip with \"edit NUMBER\".\n\n");
            message.append("Delete a trip with \"delete NUMBER\".\n\n");
        }
        message.append("Type \"new\" to create a new Trip.\n")
                .append("Type \"back\" to go back to the Main Menu.");
        return message.toString();
    }

    String galleryMenu(List<Trip> trips) {
        StringBuilder message = new StringBuilder("[MODE: GALLERY]\n");
        if (trips.isEmpty()) {
            message.append("There are no past Trips.\n\n");
        } else {
            message.append("Past trips:\n");
            appendTrips(message, trips);
            message.append("\nView a past Trip with \"view NUMBER\".\n\n");
            message.append("Review a past Trip with \"review NUMBER\".\n\n");
            message.append("Edit a past Trip with \"edit NUMBER\".\n\n");
            message.append("Delete a past Trip with \"delete NUMBER\".\n\n");
        }
        return message.append("Type \"new\" to create a new Trip.\n")
                .append("Type \"back\" to go back to the Main Menu.").toString();
    }

    String galleryTripView(Trip trip, List<Plan> plans) {
        StringBuilder message = new StringBuilder("[MODE: GALLERY]\nViewing past Trip: ")
                .append(trip.title())
                .append(" (from ")
                .append(DATE_FORMATTER.format(trip.startDate()))
                .append(" to ")
                .append(DATE_FORMATTER.format(trip.endDate()))
                .append(")\n");
        trip.review().ifPresent(review -> appendReview(message, review));
        appendPlans(message, plans);
        message.append("Type \"new\" to create a new Plan.\n");
        if (!plans.isEmpty()) {
            message.append("Type \"edit NUMBER\" to edit a Plan.\n");
            message.append("Type \"delete NUMBER\" to delete a Plan.\n");
            message.append("Type \"review NUMBER\" to review a Plan.\n");
        }
        return message
                .append("Type \"back\" to go back to the Gallery.").toString();
    }

    String tripView(Trip trip, List<Plan> plans) {
        StringBuilder message = new StringBuilder("Viewing: ")
                .append(trip.title())
                .append(" (from ")
                .append(DATE_FORMATTER.format(trip.startDate()))
                .append(" to ")
                .append(DATE_FORMATTER.format(trip.endDate()))
                .append(")\n");
        appendPlans(message, plans);
        return message.append("Type \"new\" to create a new Plan.\n")
                .append("Type \"edit NUMBER\" to edit a Plan.\n")
                .append("Type \"delete NUMBER\" to delete a Plan.\n")
                .append("Type \"review NUMBER\" to review a Plan.\n")
                .append("Type \"back\" to go back to the Organise Menu.")
                .toString();
    }

    /**
     * Appends numbered Trip summaries to a message.
     *
     * @param message Message receiving the summaries.
     * @param trips Trips to append in display order.
     */
    private static void appendTrips(StringBuilder message, List<Trip> trips) {
        for (int index = 0; index < trips.size(); index++) {
            Trip trip = trips.get(index);
            message.append(index + 1)
                    .append(". ")
                    .append(trip.title())
                    .append(" (from ")
                    .append(DATE_FORMATTER.format(trip.startDate()))
                    .append(" to ")
                    .append(DATE_FORMATTER.format(trip.endDate()))
                    .append(")\n");
            trip.review().ifPresent(review -> appendReview(message, review));
        }
    }

    /**
     * Appends present Review fields on indented lines.
     *
     * @param message Message receiving the Review fields.
     * @param review Review to append.
     */
    private static void appendReview(StringBuilder message, Review review) {
        review.rating().ifPresent(rating -> message.append("   Rating: ")
                .append(rating)
                .append("/5\n"));
        review.text().ifPresent(text -> message.append("   Review: ")
                .append(text)
                .append("\n"));
    }

    /**
     * Appends numbered Plan summaries or an empty state to a message.
     *
     * @param message Message receiving the summaries.
     * @param plans Plans to append in display order.
     */
    private static void appendPlans(StringBuilder message, List<Plan> plans) {
        if (plans.isEmpty()) {
            message.append("There are no plans!\n\n");
            return;
        }
        message.append("Plans:\n");
        for (int index = 0; index < plans.size(); index++) {
            Plan plan = plans.get(index);
            message.append(index + 1)
                    .append(". ")
                    .append(plan.destination())
                    .append(" (")
                    .append(DATE_FORMATTER.format(plan.date()))
                    .append(" at ")
                    .append(TIME_FORMATTER.format(plan.time()))
                    .append(")\n");
            plan.review().ifPresent(review -> appendReview(message, review));
        }
        message.append("\n");
    }

    String error(String message) {
        return "Error: " + message;
    }

    String invalidIndex(String action, IndexedEntity entity, int displayedCount) {
        String itemName = entity == IndexedEntity.TRIP ? "Trip" : "Plan";
        String itemNamePlural = entity == IndexedEntity.TRIP ? "trips" : "plans";
        if (displayedCount == 0) {
            return "There are no " + itemNamePlural + " to " + action + ".";
        }
        if (displayedCount == 1) {
            return itemName + " number should be 1.";
        }
        return itemName + " number should be from 1 to " + displayedCount + ".";
    }
}
