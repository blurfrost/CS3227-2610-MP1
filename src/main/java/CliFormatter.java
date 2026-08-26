import java.time.format.DateTimeFormatter;
import java.util.List;

final class CliFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");

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
        }
        message.append("Type \"new\" to create a new Trip.\n")
                .append("Type \"back\" to go back to the Main Menu.");
        return message.toString();
    }

    String error(String message) {
        return "Error: " + message;
    }
}
