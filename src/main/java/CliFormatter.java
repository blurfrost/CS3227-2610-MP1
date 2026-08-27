import java.time.format.DateTimeFormatter;
import java.util.List;

final class CliFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    String mainMenu() {
        return "Welcome! Available commands are: \"organise\", \"exit\"";
    }

    String organiseMenu(List<Trip> trips) {
        StringBuilder message = new StringBuilder("[MODE: ORGANISE]\n");
        if (trips.isEmpty()) {
            message.append("There are no Trips planned.\n\n");
        } else {
            message.append("Here are your trips:\n");
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
            }
            message.append("\n");
            message.append("View a trip with \"view NUMBER\".\n\n");
            message.append("Delete a trip with \"delete NUMBER\".\n\n");
        }
        message.append("Type \"new\" to create a new Trip.\n")
                .append("Type \"back\" to go back to the Main Menu.");
        return message.toString();
    }

    String tripView(Trip trip, List<Plan> plans) {
        StringBuilder message = new StringBuilder("Viewing: ")
                .append(trip.title())
                .append(" (from ")
                .append(DATE_FORMATTER.format(trip.startDate()))
                .append(" to ")
                .append(DATE_FORMATTER.format(trip.endDate()))
                .append(")\n");
        if (plans.isEmpty()) {
            message.append("There are no plans!\n\n");
        } else {
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
            }
            message.append("\n");
        }
        return message.append("Type \"new\" to create a new Plan.\n")
                .append("Type \"delete NUMBER\" to delete a Plan.\n")
                .append("Type \"back\" to go back to the Organise Menu.")
                .toString();
    }

    String error(String message) {
        return "Error: " + message;
    }
}
