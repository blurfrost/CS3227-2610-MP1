package doggo.ui.cli;

import java.time.LocalDate;

import doggo.domain.Trip;

/**
 * Creates a Trip from values entered in the CLI.
 */
final class NewTripCommand implements Command {
    @Override
    public CommandResult execute(CliContext context) {
        String title = promptTitle(context);
        if (title == null) {
            return new CommandResult("Bye!", true);
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

        Trip createdTrip = context.service().createTrip(title, startDate, endDate);
        return new CommandResult("Trip successfully added!\n\n"
                + context.enterTripListFor(createdTrip), false);
    }

    private static String promptText(CliContext context, String message) {
        String value = context.prompter().prompt(message);
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    /**
     * Prompts until a non-blank Trip title is entered.
     *
     * @param context CLI dependencies.
     * @return Trimmed title, or null when input ends.
     */
    private static String promptTitle(CliContext context) {
        while (true) {
            String title = promptText(context, "Enter trip name:");
            if (title == null) {
                return null;
            }
            String validationMessage = CliTextValidator.validateTripTitle(title);
            if (validationMessage.isEmpty()) {
                return title;
            }
            context.output().println(context.formatter().error(validationMessage));
        }
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
