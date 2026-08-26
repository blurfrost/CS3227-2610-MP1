import java.time.LocalDate;

final class NewTripCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        String title = promptText(context, "Enter trip name:");
        if (title == null) {
            return new CommandResult("Bye!", true);
        }
        if (title.isEmpty()) {
            return new CommandResult(context.formatter().error("Trip title cannot be blank."), false);
        }

        LocalDate startDate = promptDate(context, "Enter trip start date:");
        if (startDate == null) {
            return new CommandResult("Bye!", true);
        }
        LocalDate endDate = promptDate(context, "Enter trip end date:");
        if (endDate == null) {
            return new CommandResult("Bye!", true);
        }
        while (endDate.isBefore(startDate)) {
            context.output().println(context.formatter().error(
                    "Trip end date cannot be before its start date."));
            endDate = promptDate(context, "Enter trip end date:");
            if (endDate == null) {
                return new CommandResult("Bye!", true);
            }
        }

        context.service().createTrip(title, startDate, endDate);
        return new CommandResult("Trip successfully added!\n\n"
                + context.formatter().organiseMenu(context.service().getTrips()), false);
    }

    private static String promptText(CliContext context, String message) {
        String value = context.prompter().prompt(message);
        if (value == null) {
            return null;
        }
        return value.trim();
    }

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
}
