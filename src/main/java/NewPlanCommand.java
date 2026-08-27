import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

final class NewPlanCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        String destination = promptDestination(context);
        if (destination == null) {
            return new CommandResult("Bye!", true);
        }

        Trip selectedTrip = context.session().selectedTripId()
                .flatMap(context.service()::getTrip)
                .orElse(null);
        if (selectedTrip == null) {
            return new CommandResult(context.formatter().error("Selected Trip could not be found."), false);
        }

        LocalDate date = promptDate(context, "Enter plan date:");
        if (date == null) {
            return new CommandResult("Bye!", true);
        }
        while (date.isBefore(selectedTrip.startDate()) || date.isAfter(selectedTrip.endDate())) {
            context.output().println(context.formatter().error(
                    "Plan date must fall within the Trip dates."));
            date = promptDate(context, "Enter plan date:");
            if (date == null) {
                return new CommandResult("Bye!", true);
            }
        }
        LocalTime time = promptTime(context, "Enter plan time:");
        if (time == null) {
            return new CommandResult("Bye!", true);
        }

        try {
            UUID selectedTripId = context.session().selectedTripId()
                    .orElseThrow(() -> new IllegalStateException("No Trip is selected."));
            context.service().addPlan(selectedTripId, destination, date, time);
        } catch (IllegalArgumentException exception) {
            context.output().println(context.formatter().error(exception.getMessage()));
            return new CommandResult(selectedTripView(context), false);
        }
        return new CommandResult("Plan created!\n" + selectedTripView(context), false);
    }

    /**
     * Prompts for a trimmed text value.
     *
     * @param context CLI dependencies.
     * @param message Prompt message.
     * @return Trimmed input, or null when input ends.
     */
    private static String promptText(CliContext context, String message) {
        String value = context.prompter().prompt(message);
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    /**
     * Prompts until a non-blank Plan destination is entered.
     *
     * @param context CLI dependencies.
     * @return Trimmed destination, or null when input ends.
     */
    private static String promptDestination(CliContext context) {
        while (true) {
            String destination = promptText(context, "Enter plan destination:");
            if (destination == null) {
                return null;
            }
            if (!destination.isBlank()) {
                return destination;
            }
            context.output().println(context.formatter().error(
                    "Plan destination cannot be blank."));
        }
    }

    /**
     * Prompts until a valid date is entered.
     *
     * @param context CLI dependencies.
     * @param message Prompt message.
     * @return Parsed date, or null when input ends.
     */
    private static LocalDate promptDate(CliContext context, String message) {
        while (true) {
            String value = context.prompter().prompt(message);
            if (value == null) {
                return null;
            }
            try {
                return InputParser.parseDate(value);
            } catch (IllegalArgumentException exception) {
                context.output().println(context.formatter().error(
                        "Date must use the DD/MM/YYYY format and be a real date."));
            }
        }
    }

    /**
     * Prompts until a valid time is entered.
     *
     * @param context CLI dependencies.
     * @param message Prompt message.
     * @return Parsed time, or null when input ends.
     */
    private static LocalTime promptTime(CliContext context, String message) {
        while (true) {
            String value = context.prompter().prompt(message);
            if (value == null) {
                return null;
            }
            try {
                return InputParser.parseTime(value);
            } catch (IllegalArgumentException exception) {
                context.output().println(context.formatter().error(
                        "Time must use the HH:mm format and be a real time."));
            }
        }
    }

    /**
     * Formats the currently selected Trip.
     *
     * @param context CLI dependencies.
     * @return Selected Trip view, or an error when it is unavailable.
     */
    private static String selectedTripView(CliContext context) {
        return context.session().selectedTripId()
                .flatMap(context.service()::getTrip)
                .map(context::selectedTripView)
                .orElse(context.formatter().error("Selected Trip could not be found."));
    }
}
